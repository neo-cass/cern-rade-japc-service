# Operations

## Where it runs

| Environment | Deployment | URL | Updated by |
|---|---|---|---|
| Production | `japc` in OpenShift project `rade-services` | https://rade-services-japc.web.cern.ch | Manual job `deploy_production` on a tag pipeline |
| Staging | `japc-master` | generated route host | Every pipeline of the default branch |
| Review | `japc-<branch>` | generated route host | Every pipeline of a branch; removed after a week |

One replica each. State is in memory ([ADR 5](adr/0005-in-memory-state-single-replica.md)): a restart logs every
client out and closes every subscription. Clients log in again and reconnect.

## Release

1. Merge to the default branch; check staging (`test_deployed` runs `EndpointTest` against it).
2. Tag the commit. The tag pipeline refuses `-SNAPSHOT` dependencies, builds, tests and builds the image.
3. Run `deploy_production` manually. It applies `deploy/app.yaml`, waits for the rollout, then points the production
   route at it. `smoke_production` checks `/readyz` and `/openapi.yaml`.
4. The war is also published to the CBNG Maven repository (`publish_war`).

**Rollback:** re-run `deploy_production` in the pipeline of the previous tag.

## Health

| Check | URL | Meaning |
|---|---|---|
| Liveness | `:8081/actuator/health/liveness` | The process works. Kubernetes restarts the pod when it fails. |
| Readiness | `:8081/actuator/health/readiness`, public copy at `/readyz` | The service accepts requests. |
| Remote probe | MONIT probes `https://rade-services-japc.web.cern.ch/readyz` | Opens a SNOW ticket when down, or 30 days before the certificate expires. |

The home page (`/`) shows the readiness state.

## Observing

- **Admin panel** `/admin` (role `admin-role`, CERN SSO): version and settings, cached RBAC sessions (revoke one, a
  user's, or all), open subscriptions (close), requests per user.
- **Metrics** (MONIT, dashboard `deploy/grafana/japc-dashboard.json`):

  | Metric | Meaning |
  |---|---|
  | `japc.requests` (operation, user, status) | Every GET, SET and subscription handshake. |
  | `japc.errors` (code, status, channel) | Every error answered. |
  | `japc.subscriptions.active` | Open subscriptions. |
  | `japc.rbac.sessions.cached` | Cached RBAC sessions. |
  | `japc.subscription.updates.sent`, `.dropped` | Updates delivered, and dropped because a client was too slow. |

- **Logs**: console (`oc logs deployment/japc`), `logs/` in the pod, and MONIT OpenSearch. Every SET, subscription
  and failed request is one INFO line with user, address, target, status and duration. Successful GETs are DEBUG.
  Errors are a WARN line; internal errors (500) an ERROR with stack trace.

Log every request for a while (restarts the pod):

```bash
oc set env deployment/japc LOGGING_LEVEL_CERN_RADE_SERVICES_JAPC_STATS_REQUESTMETRICS=DEBUG -n rade-services
```

Undo:

```bash
oc set env deployment/japc LOGGING_LEVEL_CERN_RADE_SERVICES_JAPC_STATS_REQUESTMETRICS- -n rade-services
```

Setup of MONIT, Grafana and the alerts: [deploy/monit/README.md](../deploy/monit/README.md). Where to look at
metrics, logs, probes and alerts: [monitoring_links.md](monitoring_links.md).

## Runbook

| Symptom | Likely cause | Action |
|---|---|---|
| All clients get 401 with code 5002 | Service restarted; sessions are gone | Nothing: clients log in again. If they do not, the client does not handle 401. |
| One user gets 401 | Session idle for 24 h, revoked, above the per-user limit, or a `Token` expired (every request with it gets 401) | Client logs in again, or obtains a new `Token`. Log line "exceeded … RBAC sessions" means a client logs in on every call instead of reusing its id. |
| 403 with code 5002 | User lacks the RBAC role, or the device refuses the user | Message says which: "RBAC role … required" is this service, anything else is the device's access map. |
| 429 with code 5005 | Per-user rate or subscription limit | Check "Clients" in the admin panel. Fix the client, or set `requests-per-second-by-user.<user>`. |
| 429 with code 5005 on `PUT /rbac/session`, or on any call with `Rbac: Token` | Too many failed logins or failed `Token` requests from that address | Wait a minute. A client with a wrong or expired password, or an expired `Token`, is retrying in a loop; find it in the log (401 lines from that address). |
| 413 with code 5003 | Request body above `max-request-body` | Raise `service-properties.max-request-body` if the value is legitimate. |
| 502 or 504 with code 5000 | Device, front-end or middleware problem | Not this service. Message carries the JAPC error; check the device. |
| 500 with code 5004 | Bug | Find the ERROR with stack trace in the log at that time. Alert "Internal errors". |
| 400 with code 5001 from LabVIEW | Type descriptor does not match data or parameter | Message names the field. Client-side. |
| Subscription closes with 1001 | Client stopped answering pings for 10 s | Client or network. |
| "Updates dropped" alert | A client reads slower than the device publishes | Identify the subscription in the admin panel; client-side. |
| Pod restarts, memory | Too many or too large subscriptions, or large requests | Check `japc.subscriptions.active`; raise memory in `deploy/app.yaml`. |
| Admin panel login loops or fails | SSO secret expired or wrong; user lacks the admin role | Check `rade-services-japc-admin-sso`; check the user's roles. |
| `/docs` is 404 | War built without the `build_docs` artifacts | Rebuild through CI. |
| Suspected leak of a cached id | | Admin panel → RBAC sessions → revoke the session, the user's sessions, or all. |

## Recurring maintenance

| What | When | How |
|---|---|---|
| Dependencies (Spring Boot, CBNG, bucket4j, springdoc, OpenTelemetry appender) | At least yearly, and on security advisories | Update `build.gradle.kts`, run the tests, deploy a review app, run `EndpointTest`. |
| CERN libraries (JAPC, CMW, RBAC) | With the dependency update, and when CERN announces the end of a version | Pinned in `build.gradle.kts`. Set them together to the current PRO versions; CI refuses a release with a floating `PRO`. |
| Images: Java base image (`Dockerfile`), CI images (`.gitlab-ci.yml`) | With the dependency update | Pinned by tag. A newer patch tag of the Java image brings security fixes: do not let it age. Move to the next Java LTS when CBNG and the CERN libraries support it. |
| SSO client secret | When the Application Portal says it expires | Update the OpenShift secret, restart. |
| Grafana pod-metrics token | Never with the default non-expiring token; yearly only if the expiring alternative was used | `deploy/monit/README.md`, step A4. |
| RBAC roles | When people change | E-groups / RBAC, not this service. |
| TLS certificate | Automatic (router) | The MONIT probe warns 30 days before expiry. |
