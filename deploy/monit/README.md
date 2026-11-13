# MONIT setup checklist

Do the steps in order. Part A can be done today; part B when the tenant ticket is resolved; part C when the Grafana
organization ticket is resolved. The service itself needs no further code: it starts sending data at step B2.

Replace `<you>` with your CERN username.

---

## A. Today

### A1. Remote probe (down alert)

1. Clone the probes repository and create a branch:
   ```bash
   git clone https://gitlab.cern.ch/monitoring/monit-remote-probes.git
   cd monit-remote-probes
   git checkout -b add-rade-probes
   ```
2. Open `whitelist.yaml`. Add a group `rade` with `<you>` under it, copying the format of the existing entries.
3. Create the folder and copy the probe file from this repository:
   ```bash
   mkdir rade
   cp <path to japc-new>/deploy/monit/probes.yaml rade/probes.yaml
   ```
4. Commit and push:
   ```bash
   git add whitelist.yaml rade/probes.yaml
   git commit -m "Add RADE JAPC service probe"
   git push -u origin add-rade-probes
   ```
   If the push is refused (no access): open https://gitlab.cern.ch/monitoring/monit-remote-probes, click **Fork**,
   push the branch to your fork instead and continue.
5. Open https://gitlab.cern.ch/monitoring/monit-remote-probes/-/merge_requests/new, select your branch as source,
   click **Compare branches and continue**, then **Create merge request**. The pipeline validates and merges it.
6. After the merge, open https://monit-grafana.cern.ch/d/w57luRvnz/remote-probes-overview?orgId=1 and search for
   `rade-services-japc`: it should show as up within a few minutes.

### A2. Tenant ticket

1. Open https://cern.ch/monit-support (MONIT's Service Now form).
2. Paste:

   > **Subject:** New MONIT tenant "rade" (OTLP metrics and logs)
   >
   > Please create a MONIT tenant named **rade**. Responsible e-group: **rade-admins**. We do not manage Puppet nodes,
   > so please create the Teigi service and share the credentials with the e-group.
   >
   > New flow: metrics and logs from a Spring Boot service on PaaS (OpenShift project rade-services), sent via
   > OTLP/HTTP to monit-otlp.cern.ch:4319.
   > - Metrics: about 500–1,500 series per deployment, 30 s interval, 2–3 deployments.
   > - Logs: about 1–2 GB/day.
   >
   > Access: Grafana and OpenSearch. Please also create the read-only user **rade_ro**, give the e-group
   > **rade-admins** read access to our logs in monit-timberprivate OpenSearch, and allow the tenant to receive shared
   > metrics (monit_shared) from Remote Probes.

3. Submit. Keep the ticket number.

### A3. Grafana organization ticket

1. Open https://cern.ch/monit-support again.
2. Paste:

   > **Subject:** New Grafana organization "RADE"
   >
   > Please create a Grafana organization in monit-grafana.cern.ch:
   > - Name: **RADE**
   > - Admin e-group: **rade-admins**
   > - Access level: **E-Group** (dashboards show user names and client addresses)
   >
   > Purpose: dashboards and alerts for RADE services on PaaS, data from MONIT tenant "rade" (requested in ticket
   > `<number from A2>`).

3. Submit.

### A4. Token for pod metrics (needed in C4)

In a terminal logged in with `oc`:

```bash
oc create serviceaccount monit-grafana-metrics-reader -n rade-services
oc adm policy add-role-to-user view -z monit-grafana-metrics-reader -n rade-services
oc create token monit-grafana-metrics-reader -n rade-services --duration=8760h
```

Save the printed token in your password manager. It expires after one year: rerun the last command then and update
the header in C4.

---

## B. When the tenant ticket (A2) is resolved

### B1. Get the passwords

MONIT shares the password of `rade` (and `rade_ro`) as described in the ticket's resolution. Save both.

### B2. Turn on the export

```bash
oc create secret generic monit-tenant -n rade-services --from-literal=MONIT_ENABLED=true --from-literal=MONIT_CREDENTIALS=$(printf 'rade:<rade password>' | base64 -w0)
oc rollout restart deployment/japc -n rade-services
oc rollout restart deployment/japc-master -n rade-services
```

The restart drops open subscriptions; clients reconnect.

### B3. Add the probe to the tenant

In `monit-remote-probes`, add `monit_shared: "rade"` under `labels` in `rade/probes.yaml` and open another merge
request as in A1 steps 4–5.

---

## C. When the Grafana organization ticket (A3) is resolved

### C1. Open the organization

1. Open https://monit-grafana.cern.ch and log in.
2. Click your profile picture (top right) → **Switch organization** → **RADE**.

### C2. Data source: MONIT metrics

1. Open https://monit-grafana.cern.ch/connections/datasources/new and click **Prometheus**.
2. **Name:** `MONIT metrics`
3. **Prometheus server URL:** `https://monit-prom-lts.cern.ch/prometheus/`
4. **Authentication method:** Basic authentication. **User:** `rade`. **Password:** the `rade` password.
5. If there is a switch **Manage alerts via Alerting UI**, turn it off.
6. Click **Save & test** at the bottom. Expect a green "Successfully queried the Prometheus API".

### C3. Data source: MONIT logs

1. Open https://monit-grafana.cern.ch/connections/datasources/new and click **OpenSearch** (if it is not in the list,
   reply on the org ticket asking for the OpenSearch plugin).
2. **Name:** `MONIT logs`
3. **URL:** `https://monit-timberprivate.cern.ch:443/os`
4. **Authentication:** Basic authentication. **User:** `rade_ro`. **Password:** the `rade_ro` password.
5. **Index name:** `monit-logs-ss4o_rade_*`. **Pattern:** Daily. **Time field name:** `@timestamp`.
6. Click **Save & test**. Expect a green success message.

### C4. Data source: PaaS metrics

1. Open https://monit-grafana.cern.ch/connections/datasources/new and click **Prometheus**.
2. **Name:** `PaaS metrics`
3. **Prometheus server URL:** `https://user-project-metrics.paas.cern.ch`
4. **Authentication method:** No authentication.
5. **HTTP headers** → **Add header**: header `Authorization`, value `Bearer <token from A4>`.
6. Scroll to **Other**: **HTTP method** GET, **Custom query parameters** `namespace=rade-services`.
7. Click **Save & test**.
8. Open https://monit-grafana.cern.ch/explore, choose `PaaS metrics`, switch the query editor to **Code**, enter
   `kube_pod_container_status_restarts_total{container="japc"}` and click **Run query**. It should return one line
   per pod. If it returns nothing, skip the "Pod restarted" alert in C7 and ask PaaS support.

### C5. Dashboard

1. Open https://monit-grafana.cern.ch/dashboard/import
2. Click **Upload dashboard JSON file** and choose `deploy/grafana/japc-dashboard.json` from this repository.
3. Click **Import**. At the top of the dashboard choose **Datasource:** `MONIT metrics`, **Deployment:** `japc`.
   Panels fill once B2 is done.

### C6. Alert contact point and policy

1. Open https://monit-grafana.cern.ch/alerting/notifications and click **+ Add contact point**.
2. **Name:** `monit-alarms`. **Integration:** Webhook. **URL:** `http://monit-alarms.cern.ch:10016`. Click
   **Save contact point**.
3. Open https://monit-grafana.cern.ch/alerting/routes. On the **Default policy**, click **More → Edit**, set
   **Default contact point** to `monit-alarms`, click **Update default policy**.

### C7. Alert rules

For each row of the table below:

1. Open https://monit-grafana.cern.ch/alerting/new/alerting
2. **Rule name:** the Alert column.
3. **Define query and alert condition:** choose the Data source column, switch to **Code**, paste the Query column.
   Under **Expressions**, set the **Threshold** to the "Is above" value of the Condition column.
4. **Folder:** create `JAPC` the first time. **Evaluation group:** create `japc` with interval `1m` the first time.
   **Pending period:** the "for" value of the Condition column.
5. **Labels** → **Add labels**: `gni_entities` = `japc`, `snow_functional_element` = `LabVIEW Support`,
   `snow_auto_closing` = `true`.
6. **Annotations** → **Add custom annotation**: `gni_correlation` = `{{ $values.A.Value }}`.
7. For "App not reporting" only: under **Configure no data and error handling**, set **Alert state if no data** to
   **Alerting**.
8. Click **Save rule and exit**.

| Alert | Data source | Query | Condition |
|---|---|---|---|
| App not reporting | MONIT metrics | `absent(japc_subscriptions_active{job="japc"})` | is above 0, for 5m |
| CPU high | MONIT metrics | `process_cpu_usage{job="japc"}` | is above 0.8, for 10m |
| Heap nearly full | MONIT metrics | `sum(jvm_memory_used_bytes{job="japc",area="heap"}) / sum(jvm_memory_max_bytes{job="japc",area="heap"})` | is above 0.9, for 10m |
| Clients rate limited | MONIT metrics | `sum(rate(japc_requests_total{job="japc",status="429"}[5m]))` | is above 0, for 10m |
| Updates dropped | MONIT metrics | `sum(rate(japc_subscription_updates_dropped_total{job="japc"}[5m]))` | is above 0, for 15m |
| Internal errors | MONIT metrics | `sum(rate(japc_errors_total{job="japc",code="5004"}[5m]))` | is above 0.1, for 15m |
| Pod restarted | PaaS metrics | `increase(kube_pod_container_status_restarts_total{container="japc"}[15m])` | is above 0, for 0s |

Thresholds are starting points. If a rule shows "No data" for CPU or heap once B2 is done, look up the real metric
name in Explore (`MONIT metrics`, Metrics browser) and adjust the query.

### C8. Check alerts arrive

1. On one rule, temporarily set the threshold to `-1`, save, and wait 2–3 minutes.
2. Open https://monit-opensearch.cern.ch and search the index pattern `monit_prod_alarm_raw_gni*` for `japc`.
   The alert should be there, and a SNOW ticket for LabVIEW Support should open.
3. Set the threshold back and save.

---

## Request log level

Successful GETs are logged at DEBUG and therefore not sent to MONIT; their counts are in the `japc_requests` metric.
To log every request for a while (restarts the pod):

```bash
oc set env deployment/japc LOGGING_LEVEL_CERN_RADE_SERVICES_JAPC_STATS_REQUESTMETRICS=DEBUG -n rade-services
```

Undo:

```bash
oc set env deployment/japc LOGGING_LEVEL_CERN_RADE_SERVICES_JAPC_STATS_REQUESTMETRICS- -n rade-services
```
