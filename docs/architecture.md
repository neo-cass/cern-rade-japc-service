# Architecture

One Spring Boot application, one process, no database. It translates HTTP and WebSocket requests into JAPC calls made
with the caller's RBAC token.

```
client ──TLS──► OpenShift router ──HTTP :8443──► security filters ──► controller ──► service ──► JAPC ──► device
                                                      │                                  ▲
                                                      └─► RBAC session (token) ──────────┘
```

## Packages

All under `cern.rade.services.japc`. One package per feature; a feature owns its controller, service and types.

| Package | Responsibility | Start reading at |
|---|---|---|
| (root) | Application entry point, route constants, static pages. | `InitialiseService`, `ApiPaths` |
| `parameter` | GET and SET of a device parameter. | `ParameterEndpoint`, `ParameterService` |
| `subscription` | WebSocket subscriptions. | `SubscriptionHandshakeInterceptor`, `SubscriptionManager` |
| `security` | Filter chains, `Rbac` header authentication, `/rbac/session`, SSO for the admin panel. | `SecurityConfiguration`, `RbacHeaderFilter` |
| `rbac` | RBAC sessions, login schemes, the gateway to the CERN RBAC libraries. | `RbacSession`, `RbacLoginService`, `RbacGateway` |
| `format` | Wire formats (JSON, LabVIEW) and content negotiation. | `Format`, `Formats` |
| `error` | Error codes, HTTP status mapping, the global exception handler. | `ErrorCode`, `ErrorHandler` |
| `ratelimit` | Per-user request and subscription limits. | `RateLimiter` |
| `stats` | Request metrics and log, gauges. | `RequestInterceptor`, `RequestMetrics` |
| `admin` | Admin API behind the admin panel. | `AdminEndpoint`, `AdminService` |

Dependencies point one way: controllers → services → `rbac` / JAPC. `format` and `error` are used by everything and
depend on nothing but rade-codec. Only `RbacGateway` calls the CERN RBAC client; only `ParameterProvider` creates JAPC
parameters.

## A GET or SET request

1. **Security** (`SecurityConfiguration`, API chain). `RbacHeaderFilter` reads `Rbac: Cached <id>` and finds the
   `RbacSession`, or reads `Rbac: Token`, checks it through `TokenValidationCache` and builds an unregistered
   `RbacSession` for this request only. It puts the session in the security context with its RBAC roles as authorities. No header, unknown id or
   missing role ends here with 401 or 403, written by `SecurityErrorWriter` in the client's format.
2. **Rate limit and metrics** (`RequestInterceptor`). One token bucket per RBAC user; over the limit gives 429.
3. **Controller** (`ParameterEndpoint`). Spring's message converters (`JSONConverter`, `LabVIEWConverter`) decode the
   body. The controller builds a `ParameterAddress`, which rejects malformed names.
4. **Service** (`ParameterService`). Creates the JAPC parameter and runs the call inside `RbacSession.callAs`, which
   puts the session's token where the JAPC libraries look for it, for the duration of the call.
5. **Response**. The controller returns a value object; the converter for the negotiated format encodes it.
6. **Errors**. Any exception reaches `ErrorHandler`, which asks `ErrorCode` for the status and code, logs, counts
   and writes the error in the client's format.

## A subscription

1. The WebSocket handshake is an HTTP GET and passes the same security chain.
2. `SubscriptionHandshakeInterceptor` rate-limits, starts a `SubscriptionService` (one JAPC subscription) and waits
   for the first value. A failure is answered as an HTTP error; no socket is opened.
3. `SubscriptionManager` creates a `SubscriptionWebSocketHandler` for the connection. It sends the held value, then
   every update, from a bounded queue that drops the oldest update when the client is too slow.
4. The manager pings every client; Tomcat closes a connection from which nothing arrives within the idle timeout.
5. Closing, by either side or by an admin, stops the JAPC subscription.

## Login

`PUT /rbac/session` is the only request on which `RbacHeaderFilter` accepts a login scheme. `RbacLoginService` picks
the `RbacLogin` for the scheme, which obtains tokens through `RbacGateway`; the session is stored in
`RbacSessionRegistry` and its id returned by `RbacSessionEndpoint`. `Token` is not a login: it is accepted on every
request, validated once per token by `TokenValidationCache` (kept until the token expires), and never registered. See
[security.md](security.md), [ADR 1](adr/0001-rbac-header-and-cached-sessions.md) and
[ADR 7](adr/0007-stateless-rbac-tokens.md).

## Two filter chains

| Chain | Paths | Authentication | State |
|---|---|---|---|
| admin | `/admin/**`, `/oauth2/**`, `/login/**`, `/logout` | CERN SSO browser session, or `Rbac: Cached`; admin role | HTTP session, CSRF token |
| api | everything else | `Rbac` header; user role (`/api`, `/ws`), user or admin role (`/rbac`) | stateless |

Paths not listed as public or matched above are denied.

## External dependencies

| Dependency | Used for | Notes |
|---|---|---|
| JAPC, CMW RDA3 (`cern.japc`, `cern.cmw`) | Device access | Versions pinned in `build.gradle.kts`. |
| RBAC client (`cern.accsoft.security`) | Login, token validation and renewal | Only through `RbacGateway`. |
| rade-codec, rade common (`cern.rade.services`) | JSON and LabVIEW encoding | Sibling projects, released separately. |
| Spring Boot (web MVC, WebSocket, security, OAuth2 client, actuator, OpenTelemetry) | Framework | |
| bucket4j | Rate limiting | |
| springdoc | Serves Swagger UI for `docs/openapi.yml` | The specification is hand-written, not generated. |

## State

Everything is in memory: RBAC sessions, rate-limit buckets, open subscriptions, counters. One replica.
See [ADR 5](adr/0005-in-memory-state-single-replica.md).
