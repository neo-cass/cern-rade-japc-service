# 1. RBAC login through the `Rbac` header and cached sessions

Status: accepted

## Context

Every JAPC call must run with the RBAC token of the person or machine making it, so that device servers apply their
own access rules. Clients are LabVIEW applications, scripts and the browser-based admin panel. The LabVIEW HTTP client
cannot send POST, cannot run a Kerberos or OAuth flow against this service, and must not send a password on every
request.

## Decision

- Credentials travel in one request header, `Rbac: <scheme> <value>`. Each scheme is one class implementing
  `RbacLogin` (`Explicit`, `Kerberos`, `Oauth`, `Token`, `Cached`).
- Login schemes are accepted by `PUT /rbac/session` only. The service logs in to RBAC, keeps the tokens in an
  `RbacSession`, and answers with `Rbac: Cached <id>`. Every other call sends that header.
- The cached id is a random UUID and is a secret. Admins see sessions under a separate public `handle`.
- Sessions live in memory (`RbacSessionRegistry`). They are renewed from the master token for up to a year, dropped
  after 24 hours without use, and limited per user.
- The admin panel logs in with CERN SSO; the SSO token is exchanged for an RBAC login, so the panel and the API check
  the same RBAC roles.
- Authorisation is by RBAC role: one role for the API, one for the admin API (`service-properties.rbac`).

## Consequences

- A password crosses the network once per login instead of once per request.
- A restart, or a second replica, loses the sessions: clients must handle 401 by logging in again. See ADR 5.
- The cached id must be protected like a password (TLS, `Cache-Control: no-store`, never logged).
- Open WebSocket subscriptions are authenticated at the handshake only. Revoking a session does not close them.
