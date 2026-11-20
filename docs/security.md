# Security

The service can set values on accelerator devices. It must never run a device call for someone who is not who they
claim to be, or with more rights than RBAC gives them.

## Model

- **Who can reach it.** The OpenShift route is visible on the CERN intranet only and terminates TLS; plain HTTP is
  redirected. The service itself listens on plain HTTP inside the cluster.
- **Authentication.** RBAC. A client logs in at `PUT /rbac/session` with one of the `Rbac` login schemes and receives
  a cached id; see [ADR 1](adr/0001-rbac-header-and-cached-sessions.md). A client holding its own RBAC token sends it as
  `Rbac: Token` on every request instead, with no session; see [ADR 7](adr/0007-stateless-rbac-tokens.md). The admin panel logs in with CERN SSO and the
  SSO token is exchanged for an RBAC login; setup and the forms involved are in [sso_setup.md](sso_setup.md).
- **Authorisation, twice.** The service requires an RBAC role to use the API (`required-role`) and another for the
  admin API (`admin-role`). The device server then applies its own RBAC rules to the user's token, which the service
  forwards with every JAPC call. The service never uses a token of its own for device access.
- **Default deny.** A path that no matcher in `SecurityConfiguration` names is refused.

## What protects what

| Threat | Protection | Where |
|---|---|---|
| Call without login, or with a login scheme outside the login route | 401 before any controller runs | `RbacHeaderFilter` |
| Missing role | 403 | `SecurityConfiguration` |
| A route reaching a device without a session | Controllers call `RbacAuthentication.required()`, which refuses | `RbacAuthentication` |
| Stolen or leaked cached id | Random UUID, never logged, never returned except by the login response (`Cache-Control: no-store`); admins see a separate `handle`; revocable; dropped after 24 h idle | `RbacSession`, `RbacSessionRegistry` |
| Session pile-up | At most `sessions-per-user` sessions per user; sessions whose id was never handed out are dropped | `RbacSessionRegistry` |
| Password guessing through the service | Failed logins are limited per client address (`login-failures-per-minute`); beyond it every login from the address gets 429 | `RateLimiter`, `RbacHeaderFilter` |
| Forged or expired `Rbac: Token` | Signature checked against the RBAC environment's keys before the token is cached; cache entries end at the token's end time; failures count toward the same per-address limit as logins | `TokenValidationCache`, `RbacGateway`, `RbacHeaderFilter` |
| Flooding by one user | Token bucket per RBAC user, limit on open subscriptions per user, 429 | `RateLimiter` |
| Flooding by one address | Router limits per IP | `deploy/route.yaml` |
| Oversized request | Bodies above `max-request-body` (16 MiB by default) are refused with 413; headers above Tomcat's 8 KiB default never arrive; WebSocket input is limited to 8 KiB and ignored | `Format.readBody`, `SubscriptionWebSocketConfigurator` |
| Malformed LabVIEW data or descriptor | Lengths are checked against the available data before allocating; any decoding failure is a 400 | rade-codec |
| JSON deserialisation attacks | Values are decoded by type name (`@type`) to JAPC value types only; no class names from the client | rade-codec `JSONCodec` |
| Parameter name injection | Device, property and field may not contain `/`, `#` or control characters | `ParameterAddress` |
| Information leak in errors | Internal errors (5004) return a fixed message; the cause is only logged | `ErrorCode.message` |
| Log forging | Line breaks in messages are escaped by the log pattern | `log4j2-*.yml` |
| Cross-site request forgery | API: no cookies, credentials are a custom header a browser cannot send cross-site. Admin panel: CSRF token on state-changing requests | `SecurityConfiguration` |
| Cross-site WebSocket hijacking | Authentication by header, which browsers cannot set on a WebSocket; Spring rejects foreign `Origin` | `SubscriptionWebSocketConfigurator` |
| Script injection in the admin panel | DOM text nodes only, no `innerHTML`; Content-Security-Policy `default-src 'self'` | `admin.js`, `SecurityConfiguration` |
| Session cookie theft (admin panel) | `HttpOnly`, `Secure`, `SameSite=Lax` | `application-sso.yml` |

## Secrets

| Secret | Where it lives | Rotation |
|---|---|---|
| SSO client id and secret | OpenShift secret `rade-services-japc-admin-sso`, read as environment variables | CERN Application Portal, then update the secret and restart |
| MONIT tenant credentials | OpenShift secret `monit-tenant` | With MONIT support |
| User passwords, Kerberos tickets, OAuth tokens | In transit in the `Rbac` header of the login request only; never stored or logged | n/a |
| RBAC tokens sent with `Rbac: Token` | In transit on every request; in memory in `TokenValidationCache` (keyed by SHA-256) until they expire; never logged | Client obtains a new one |
| RBAC master and application tokens | In memory, inside `RbacSession` | Lost on restart |
| Cached session ids | In memory and at the client | Revoke in the admin panel |

Nothing secret is in the repository or the image. Keep it that way: configuration files reference environment
variables.

## Known limitations

Accepted for now; each is a client-visible change that needs agreement with client owners before it is closed.

1. **Open subscriptions outlive their session.** Credentials are checked at the handshake only. Revoking a session or
   losing a role does not close subscriptions already open. An admin can close them in the admin panel.
2. **Sessions are lost on restart.** Clients must log in again on 401.
3. **RBAC tokens presented with `Token` are not bound to the sender's address** by this service. A leaked `Token`
   works from anywhere until it expires, and the service cannot revoke it: it keeps no session for it.
4. **The login throttle is per address.** Clients behind one shared address share one allowance, and one of them
   failing repeatedly (a wrong password, an expired `Token`) blocks logins and `Token` requests (not calls with a
   cached id) for all of them for up to a minute.

## When you change code

- Read the checklist in [adding-an-endpoint.md](adding-an-endpoint.md).
- Never log the `Rbac` header, a session id, a password or a token. `RbacCredentials.toString` hides the value on
  purpose.
- Never widen `PUBLIC_PATHS` to a path that returns device data or session information.
- Keep the login route the only place that accepts login schemes (`Explicit`, `Kerberos`, `Oauth`). Elsewhere only
  `Cached` and `Token` are accepted, and `Token` must keep going through `TokenValidationCache`.
- A dependency upgrade that touches Spring Security, Tomcat, Jackson or the RBAC client needs the full test suite,
  including `EndpointTest` against a deployed review app.

## Reporting a problem

Contact the service's support line (README) and CERN Computer Security (`computer.security@cern.ch`). Revoke all
sessions in the admin panel if a cached id may have leaked.
