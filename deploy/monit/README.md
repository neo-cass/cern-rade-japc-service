# MONIT setup: what is left to do

The service already exports metrics and logs over OTLP once the `monit-tenant` secret exists (`application-monit.yml`).
Everything below is manual, one-off setup. Docs: https://monit.docs.cern.ch (source:
https://gitlab.cern.ch/monitoring/resources/docs/public). Tickets: https://cern.ch/monit-support

## 1. Tenant (SNOW ticket)

- Tenant name `rade`, responsible e-group `rade-admins`. No Teigi service of our own (no Puppet nodes): MONIT creates one
  and shares the credentials with the e-group. No Prometheus TSDB stats (we push OTLP, no Prometheus in between).
- Announce the new flow: metrics and logs from a Spring Boot service on PaaS (project `rade-services`) via OTLP/HTTP to
  `monit-otlp.cern.ch:4319`.
- Volume: metrics about 500–1,500 series per deployment at 30 s, 2–3 deployments; logs one record (~1 KB) per request.
- Access: Grafana and OpenSearch. Ask for the read-only user `rade_ro` and for `rade-admins` to read our logs in the
  monit-timberprivate OpenSearch interface.
- Optional: 5-year metric retention (default 40 days); accepting shared metrics (`monit_shared`, for the remote probe).

## 2. Grafana organization (SNOW ticket)

- Name `RADE`, admin e-group `rade-admins`, access level **E-Group** (dashboards show RBAC users and client addresses).

## 3. Secret (once the tenant exists)

```bash
oc create secret generic monit-tenant -n rade-services --from-literal=MONIT_ENABLED=true --from-literal=MONIT_CREDENTIALS=$(printf 'rade:<password>' | base64 -w0)
```

Export starts at the next pod restart (`oc rollout restart deployment/japc`, likewise for `japc-master`).

## 4. Grafana datasources (as org admin)

| Name | Type | Settings |
|---|---|---|
| MONIT metrics | Prometheus | URL `https://monit-prom-lts.cern.ch/prometheus/`, basic auth `rade` / tenant password, "Manage alerts via Alerting UI" off |
| MONIT logs | OpenSearch | URL `https://monit-timberprivate.cern.ch:443/os`, basic auth `rade_ro`, index `monit-logs-ss4o_rade_*`, time field `@timestamp`, daily pattern |
| PaaS metrics | Prometheus | URL `https://user-project-metrics.paas.cern.ch`, header `Authorization: Bearer <token>`, custom query parameter `namespace=rade-services`, method GET |

Token for PaaS metrics (valid one year, renew yearly):

```bash
oc create serviceaccount monit-grafana-metrics-reader -n rade-services
oc adm policy add-role-to-user view -z monit-grafana-metrics-reader -n rade-services
oc create token monit-grafana-metrics-reader -n rade-services --duration=8760h
```

Then import `deploy/grafana/japc-dashboard.json` (Dashboards → Import, pick "MONIT metrics"). The `japc_*` panels should
work as is; JVM/CPU/latency metric names may need adjusting once real data is in Mimir.

## 5. Down alert: remote probe (can be done now)

Merge `probes.yaml` (this folder) into https://gitlab.cern.ch/monitoring/monit-remote-probes, see the header of that file.
Results: https://monit-grafana.cern.ch/d/w57luRvnz/remote-probes-overview?orgId=1

## 6. Load alerts: Grafana alert rules (in our org)

1. Contact point `monit-alarms`: Webhook, URL `http://monit-alarms.cern.ch:10016`. Route the notification policy to it.
2. Labels on every rule: `gni_entities: japc` (required, otherwise nothing is sent), `snow_functional_element:
   LabVIEW Support`, `snow_auto_closing: "true"`. Changing values go in annotations, e.g.
   `gni_correlation: {{ $values.A.Value }}`. For email instead of SNOW, `email_to` is documented for Alertmanager;
   test it from Grafana first.
3. Rules (thresholds are starting points):

| Alert | Datasource | Query | Fires when |
|---|---|---|---|
| App not reporting | MONIT metrics | `absent(japc_subscriptions_active{job="japc"})` | 5 min; "no data" → Alerting |
| CPU high | MONIT metrics | `process_cpu_usage{job="japc"}` | > 0.8 for 10 min |
| Heap nearly full | MONIT metrics | `sum(jvm_memory_used_bytes{job="japc",area="heap"}) / sum(jvm_memory_max_bytes{job="japc",area="heap"})` | > 0.9 for 10 min |
| Clients rate limited | MONIT metrics | `sum(rate(japc_requests_total{job="japc",status="429"}[5m]))` | > 0 for 10 min |
| Updates dropped | MONIT metrics | `sum(rate(japc_subscription_updates_dropped_total{job="japc"}[5m]))` | > 0 for 15 min |
| Internal errors | MONIT metrics | `sum(rate(japc_errors_total{job="japc",code="5004"}[5m]))` | > 0.1 for 15 min |
| Pod restarted | PaaS metrics | `increase(kube_pod_container_status_restarts_total{container="japc"}[15m])` | > 0 |

Check delivery in https://monit-opensearch.cern.ch: `monit_prod_alertmanager_raw_alerts` (raw) and
`monit_prod_alarm_raw_gni` (only when `gni_entities` is set).

## 7. Before pushing to `refactor`

`refactor` serves `rade-services-japc.web.cern.ch`, so the rate limits apply to real users on the next deploy:
50 requests/s and 200 open subscriptions per RBAC user (`service-properties.rate-limit`, overrides per user under
`requests-per-second-by-user`), and per IP on the Routes 600 requests per 3 s and 1000 connections. Add overrides for
accounts shared by many LabVIEW machines first.
