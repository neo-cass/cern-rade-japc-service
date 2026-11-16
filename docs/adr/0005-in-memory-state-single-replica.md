# 5. State in memory, one replica

Status: accepted

## Context

The service holds RBAC sessions (with master tokens), rate-limit buckets, open subscriptions and request counters.
Sharing them between replicas would need an external store holding credentials, and sticky routing for WebSockets.

## Decision

- All state is in the memory of one instance. `deploy/app.yaml` runs one replica.
- A restart loses sessions, counters and subscriptions. Clients recover by logging in again on 401 and reopening
  their subscriptions.
- Limits bound the state: sessions per user, idle timeout, subscriptions per user, queue size per subscription,
  request body size.

## Consequences

- No database, no cache server, no secret store for tokens: nothing else to operate or to secure.
- Deployments interrupt clients for a few seconds. Deploy outside operation when that matters.
- Scaling out is not possible without revisiting this decision. Scale up (CPU, memory) first.
