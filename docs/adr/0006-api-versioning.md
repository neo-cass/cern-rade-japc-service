# 6. API versions in the path, unversioned prefix means latest

Status: accepted

## Context

LabVIEW clients are deployed on many machines and updated rarely. A breaking change to the API must not break them,
yet new clients should not have to hard-code a version to get the current API.

## Decision

- Every API version has its own path prefix: `/api/v1`, `/ws/v1`. A version is only added for a change that breaks
  existing clients; additions go into the current version.
- The unversioned prefixes `/api` and `/ws` always serve the latest version. Today that is v1.
- A path segment `v<number>` directly after the prefix is a version, never a device name. A version that does not
  exist is 404, on REST and on WebSocket.
- Old versions stay available as long as a client uses them.
- `/rbac/session` and `/admin/api` are not versioned: login is shared by all versions, and the admin API has one
  client, the admin panel shipped with the service.

## Consequences

- A client on an unversioned URL is moved to the next version the day it is deployed. Clients that cannot change
  quickly, LabVIEW in particular, must use `/api/v1` and `/ws/v1`.
- Adding v2 is mechanical: see "Adding an API version" in `docs/adding-an-endpoint.md`.
- No device can be named `v1`, `v2`, and so on.
