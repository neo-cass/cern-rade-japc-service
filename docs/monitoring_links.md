# Monitoring links

Everything needs a CERN login. Grafana access: `rade-admins` and team members. The service dashboard is also linked
from the admin panel.

## Dashboards

| Link | What |
|---|---|
| [Service](https://monit-grafana.cern.ch/d/rade-japc/rade-japc-service?orgId=209) | Is it reachable from outside, requests, errors, subscriptions, RBAC sessions, heap, CPU |
| [Pods](https://monit-grafana.cern.ch/d/rade-japc-pods/rade-japc-pods?orgId=209) | Pod state, restarts, memory and CPU against limits, network |
| [Logs](https://monit-grafana.cern.ch/d/rade-japc-logs/rade-japc-logs?orgId=209) | Error and warning counts, log volume, log lines, top loggers |

## Metrics

| Link | What |
|---|---|
| [Explore](https://monit-grafana.cern.ch/explore?orgId=209) | Query any service or pod metric |
| [Data sources](https://monit-grafana.cern.ch/connections/datasources?orgId=209) | MONIT and PaaS metric sources |
| [Teams](https://monit-grafana.cern.ch/org/teams?orgId=209) | Who has Grafana access |

## Logs

| Link | What |
|---|---|
| [OpenSearch Dashboards](https://monit-timberprivate.cern.ch) | Service logs in `monit-logs-ss4o_rade` (CERN SSO) |

## Probe

| Link | What |
|---|---|
| [Probe status](https://monit-grafana.cern.ch/d/w57luRvnz/remote-probes-overview?orgId=1&var-producer=rade) | Is `/readyz` reachable from outside |
| [Probe config](https://gitlab.cern.ch/monitoring/monit-remote-probes/-/tree/master/rade) | Probe definition in monit-remote-probes |

## Alerts

| Link | What |
|---|---|
| [Alert rules](https://monit-grafana.cern.ch/alerting/list?orgId=209) | Rules and their current state |
| [Contact points](https://monit-grafana.cern.ch/alerting/notifications?orgId=209) | Where alerts are sent |
| [Alert history](https://monit-opensearch.cern.ch) | Index `monit_prod_alarm_raw_gni*`, search `japc` |
| [Tickets](https://cern.service-now.com/service-portal?id=service_element&name=labview-support) | LabVIEW Support: tickets opened by alerts |

## Service and cluster

| Link | What |
|---|---|
| [Admin panel](https://rade-services-japc.web.cern.ch/admin) | RBAC sessions, subscriptions, clients |
| [Readiness](https://rade-services-japc.web.cern.ch/readyz) | Service status as JSON |
| [OpenShift project](https://paas.cern.ch/k8s/cluster/projects/rade-services) | Deployments, routes, secrets |
| [Pods](https://paas.cern.ch/k8s/ns/rade-services/pods) | Pod state, restarts, live logs |

## Help

| Link | What |
|---|---|
| [Setup guide](../deploy/monit/README.md) | How the monitoring was set up |
| [MONIT docs](https://monit.docs.cern.ch) | Platform documentation |
| [MONIT support](https://cern.ch/monit-support) | Ticket form for MONIT |
