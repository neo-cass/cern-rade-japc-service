# 4. One error model for REST and WebSocket

Status: accepted

## Context

LabVIEW clients feed errors into an error cluster (status, code, source). They need a stable numeric code and a useful
text; operators need the HTTP status to mean the same thing on every route.

## Decision

- Six service codes, `ErrorCode` 5000 to 5005 (JAPC, codec, RBAC, bad request, internal, rate limited).
- `ErrorCode.of(exception)` and `ErrorCode.httpStatus(exception)` are the only mapping from exceptions to codes and
  statuses. Controllers and services throw; nothing catches to build a response by hand.
- The body is the same structure in every format: status, code, source (`rade-japc <METHOD> <path>`), message.
- On a WebSocket, device errors are error frames and the subscription continues; fatal errors close the connection
  with close code `4000 + (code - 5000)`.
- The message of a device, codec, RBAC or request error is passed on: it is what the user needs to fix the request.
  The message of an internal error (5004) is replaced by a fixed text; the cause is only in the log.
- Every error is counted (`japc.errors`) and logged: a stack trace for 500, one line otherwise.

## Consequences

- A new failure needs at most one line in `ErrorCode`, and one row in the `Error` table of `docs/openapi.yml`.
- Messages from JAPC and RBAC reach clients verbatim. They come from CERN middleware, not from this service's
  internals, and are needed for diagnosis.
