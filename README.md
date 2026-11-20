# RADE JAPC service

HTTP and WebSocket access to CERN accelerator device parameters through JAPC, for LabVIEW (RADE) and any other client.

- `GET` / `PUT` `/api/v1/{device}/{property}[/{field}]` reads and sets a parameter.
- `wss://…/ws/v1/{device}/{property}[/{field}]` subscribes to it.
- Bodies are JSON or LabVIEW flattened data, chosen by `Accept`, `Content-Type` or `Sec-WebSocket-Protocol`.
- Every call runs with the caller's RBAC token. Log in once at `PUT /rbac/session`, then send `Rbac: Cached <id>`;
  or send an RBAC token you obtained yourself as `Rbac: Token <base64url(token)>` on every call, with no login.

The API contract is `docs/openapi.yml` (REST) and `docs/asyncapi.yml` (WebSocket). The running service serves both,
rendered, under `/docs`.

## Quick start

Needs Java 21 and the CERN network (CBNG plugins, JAPC, RBAC and the CMW directory are CERN-internal).

```bash
./gradlew test                       # unit and in-process API tests, no device needed
./gradlew bootWar                    # build/libs/japc-1.0.0.war
java -jar build/libs/japc-1.0.0.war --spring.profiles.active=debug
```

The service listens on `http://localhost:8443` (plain HTTP; TLS is terminated by the OpenShift router) and exposes
health on `http://localhost:8081/actuator/health`.

```bash
curl -X PUT -D - -H "Rbac: Explicit $(printf '%s' 'user:password' | base64 | tr '+/' '-_')" http://localhost:8443/rbac/session
curl -H 'Rbac: Cached <id from the Rbac response header>' http://localhost:8443/api/v1/RADE.TEST/Get
curl -H 'Rbac: Token <base64url(serialized RBAC token)>' http://localhost:8443/api/v1/RADE.TEST/Get   # no login needed
```

`/docs` is only populated when the API documentation was built first (CI job `build_docs`, see `.gitlab-ci.yml`).

## Where things are

| Path | Content |
|---|---|
| `src/main/java/cern/rade/services/japc` | The service, one package per feature. Start with `docs/architecture.md`. |
| `src/main/resources` | `application*.yml` (configuration per profile), log4j2 configuration, static pages (`web/`). |
| `src/test` | Tests. `ApiContractTest` starts the whole service against a stub device. `EndpointTest` runs against a deployed instance. |
| `docs/openapi.yml`, `docs/asyncapi.yml` | The API contract. Change them in the same commit as the code. |
| `deploy/` | OpenShift manifests, Grafana dashboard, MONIT setup. |
| `.gitlab-ci.yml` | Build, test, review apps, staging, production. |

## Documentation

| Document | Read it when |
|---|---|
| [Architecture](docs/architecture.md) | You are new to the code. |
| [How to add an endpoint](docs/adding-an-endpoint.md) | You need to add or change a route. |
| [Security](docs/security.md) | You touch authentication, input handling or anything client-facing. |
| [Configuration](docs/configuration.md) | You deploy or tune the service. |
| [Operations](docs/operations.md) | The service misbehaves, or you release it. |
| [Decisions](docs/adr) | You wonder why it is built this way. |
| [MONIT setup](deploy/monit/README.md) | You set up monitoring and alerts. |

## Rules for changes

1. The LabVIEW client cannot change easily. Paths, headers, payloads, status codes and the login flow in
   `docs/openapi.yml` and `docs/asyncapi.yml` are a contract: do not change them without the client's owner.
2. Security first, then simplicity. No new dependency, layer or abstraction without a present need.
3. Every change to behaviour comes with a test and, if client-visible, with the specification updated.
4. `./gradlew test` must pass before a merge; CI also lints both specifications.

## Support

LabVIEW Support on the CERN Service Portal (functional element "LabVIEW Support"). Contacts are on the service's home
page (`/`).
