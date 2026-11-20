# 7. `Rbac: Token` is stateless

Status: accepted. Amends ADR 1.

## Context

ADR 1 had every scheme, `Token` included, log in at `PUT /rbac/session` and use a cached id afterwards. A `Token` is an
RBAC token the client obtained on its own machine, and RBAC only renews a token from the address it was issued to. So
the service could never renew it, and the cached session ended when the token did. The cached id added nothing for
these clients, just a second secret, an extra request and in-memory state, plus a session that a restart loses.

## Decision

- `Rbac: Token <base64url(token)>` is sent on every request and on the WebSocket handshake. It is not a login: no
  `RbacSession` is registered and no cached id is handed out. `PUT` and `DELETE /rbac/session` with a `Token` answer
  400.
- Cached sessions remain only for logins whose master token the service holds and renews (`Explicit`, `Kerberos`,
  `Oauth`). Rule: the service hands out a cached id only for a credential it renews itself; a credential the client
  owns is sent by the client every time.
- `TokenValidationCache` remembers tokens whose signature was checked, keyed by the SHA-256 of their bytes, until the
  token's own end time. Invalid tokens are never cached. `token-cache-max-entries` (20000) bounds it; beyond it tokens
  are validated on every request.
- Failed `Token` requests count toward the per-address login failure limit, like failed logins.
- Subscriptions are checked at the handshake only, as for cached sessions.

## Consequences

- `Token` clients survive a restart: they lose nothing but cache entries, which only cost one more validation.
- `Token` users do not appear in the admin panel's session list, are not counted against `sessions-per-user`, and
  cannot be revoked by the service. They stop working when the token expires.
- A client retrying with an expired `Token` uses up the failure allowance of its address, which then refuses every
  `Token` and login from that address for up to a minute.
