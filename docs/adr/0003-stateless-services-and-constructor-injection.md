# 3. Stateless singleton services, constructor injection, typed configuration

Status: accepted

## Context

The first version created a `GetService` or `SetService` object per request, each with its own "executed" state, and
read configuration through a static `ContextProvider` that needed `@DependsOn` to work. The JAPC parameter was created
in three places. A new maintainer could not tell which pattern to copy.

## Decision

- One controller per resource (`ParameterEndpoint`, `RbacSessionEndpoint`, `AdminEndpoint`). Controllers translate HTTP
  to a call on a service and back; they hold no logic.
- Services are stateless Spring singletons (`ParameterService`, `AdminService`). Everything a call needs is a
  parameter: the `ParameterAddress` and the caller's `RbacSession`.
- Dependencies are constructor parameters. There is no static access to the application context.
- Configuration is an immutable record per area (`RbacProperties`, `RateLimitProperties`, `SubscriptionProperties`),
  with defaults and validation in the record, so a wrong value stops the service at startup.
- The only objects with per-client state are the ones that genuinely have it: `RbacSession` and, per WebSocket
  connection, `SubscriptionService` with its `SubscriptionWebSocketHandler`.
- All access to JAPC parameters goes through `ParameterProvider`, so tests replace devices with a stub.

## Consequences

- Adding an endpoint is: one method in a controller, one method in a service. See `docs/adding-an-endpoint.md`.
- Services can be tested without Spring, and the whole application can be started in a test without a device.
