# Configuration reference

Configuration is standard Spring Boot: `src/main/resources/application.yml` plus one file per active profile. Any
property can be overridden without rebuilding, by an environment variable (upper case, dots and dashes as
underscores, e.g. `SERVICE_PROPERTIES_RATE_LIMIT_REQUESTS_PER_SECOND=100`) or a command-line argument
(`--service-properties.rate-limit.requests-per-second=100`).

Service-specific settings are immutable records with defaults and validation: a wrong value stops the service at
startup with a message naming the property.

## Profiles

| Profile | File | Purpose |
|---|---|---|
| `prod` (default) | `application-prod.yml`, `log4j2-prod.yml` | Production roles and logging. |
| `debug` | `application-debug.yml`, `log4j2-debug.yml` | Local runs: DEBUG logging, one role for everything. |
| `sso` | `application-sso.yml` | CERN SSO login for the admin panel. Needs `SSO_CLIENT_ID`, `SSO_CLIENT_SECRET`. |
| `monit` | `application-monit.yml` | OTLP export of metrics and logs to MONIT. Stays off until `MONIT_ENABLED=true`. |

OpenShift runs `prod,sso,monit` (`deploy/app.yaml`).

## RBAC: `service-properties.rbac` (`RbacProperties`)

| Property | Default | Meaning |
|---|---|---|
| `required-role` | none, must be set | RBAC role needed for the API and subscriptions. |
| `admin-role` | none, must be set | RBAC role needed for the admin panel and admin API. |
| `token-lifetime` | `12h` (maximum) | Lifetime of application tokens. Also how long a removed role keeps working. |
| `session-lifetime` | `365d` (maximum) | Lifetime of master tokens: how long a cached session can be renewed. |
| `session-idle-timeout` | `24h` | Sessions unused for this long are dropped. `0` disables. |
| `sessions-per-user` | `20` | Cached sessions per user; the least recently used are revoked beyond it. `0` disables. |

## Rate limits: `service-properties.rate-limit` (`RateLimitProperties`)

Per RBAC user. `0` disables a limit.

| Property | Default | Meaning |
|---|---|---|
| `requests-per-second` | `50` | GET, SET and subscription handshakes together. |
| `burst` | `100` | Requests allowed at once before the rate applies. |
| `subscriptions-per-user` | `200` | Open subscriptions per user. |
| `requests-per-second-by-user.<user>` | none | Override of `requests-per-second` for one user, e.g. an account shared by many machines. |
| `login-failures-per-minute` | `10` | Failed logins allowed per client address per minute. Beyond it, every login from that address gets 429 until it recovers. Not per user. |

The router adds per-IP limits (`deploy/route.yaml`); keep them above the per-user limits so that clients get the
service's 429 with `Retry-After`.

## Subscriptions: `service-properties.subscription` (`SubscriptionProperties`)

| Property | Default | Meaning |
|---|---|---|
| `ping-interval` | `5s` | How often the server pings each client. |
| `socket-idle-timeout` | `10s` | A client that sends nothing, not even a pong, for this long is closed. |
| `first-update-timeout` | `5s` | How long the handshake waits for the first value. |
| `update-wait-timeout` | `1s` | How long the sender waits for an update before checking the connection again. |
| `queue-size` | `100` | Updates queued per connection; the oldest is dropped when full. |

These values are part of the published WebSocket contract (`docs/asyncapi.yml`): change both together.

## Requests: `service-properties`

| Property | Default | Meaning |
|---|---|---|
| `max-request-body` | `16MB` | Largest request body accepted; a larger one gets 413. Each request in progress can hold this much memory. |

## Fixed limits

| Limit | Value | Where |
|---|---|---|
| WebSocket message from a client | 8 KiB | `SubscriptionWebSocketConfigurator` |
| Request headers (including `Lv-Type-Descriptor`) | 8 KiB | Tomcat default |

## Server

| Property | Value | Meaning |
|---|---|---|
| `server.port` | `8443` | API, WebSocket, admin panel, `/readyz`. Plain HTTP. |
| `management.server.port` | `8081` | Actuator health for the Kubernetes probes. Not routed outside the cluster. |
| `server.forward-headers-strategy` | `framework` | Trust the router's `X-Forwarded-*` for client addresses and redirects. |
| `management.endpoints.web.exposure.include` | `health` | Only health is exposed. |

## Environment variables

| Variable | Needed | Meaning |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | yes in OpenShift | Profiles, see above. |
| `SSO_CLIENT_ID`, `SSO_CLIENT_SECRET` | with `sso` | Credentials of the SSO application of the admin panel. |
| `MONIT_ENABLED`, `MONIT_CREDENTIALS` | optional | Turn on OTLP export; `base64("<tenant>:<password>")`. |
| `OTEL_SERVICE_NAME` | optional | Name under which metrics and logs appear in MONIT. |
| `JAVA_TOOL_OPTIONS` | optional | JVM options; OpenShift sets `-XX:MaxRAMPercentage=75`. |
| `LOGGING_LEVEL_<LOGGER>` | optional | Log level of one logger, see [operations.md](operations.md). |

The CMW directory and RBAC environments are chosen by the CERN libraries (`-Dcmw.directory.env=…`, default PRO). CI
uses `PRIVATE` with its own test servers.

## Tests

`EndpointTest` runs only when `APP_URL` is set: `APP_URL` (e.g. `https://host/api/v1`), `SUB_URL`
(`wss://host/ws/v1`) and `PMA_PASSWORD` (password of the test account). It writes to the `RADE.TEST` devices and
must never point at production.
