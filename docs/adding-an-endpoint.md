# How to add an endpoint

Copy an existing one. `ParameterEndpoint.get` with `ParameterService.get` is the reference for device access;
`AdminEndpoint` with `AdminService` for everything else.

## Before you start

- A new route for LabVIEW clients is a contract for many years. Agree it with the client's owner first.
- A change to an existing route's path, headers, payload or status codes breaks deployed LabVIEW clients. Add, do not
  change.

## Steps

### 1. Route

Add the path to `ApiPaths` if it is new. Decide which filter chain serves it (`SecurityConfiguration`):

- under `/api/**`: authenticated, user role, rate-limited and counted automatically;
- under `/admin/api/**`: authenticated, admin role;
- anything else is **denied** until you add a matcher in `apiFilterChain`. Never use `permitAll` for a route that
  reaches a device.

### 2. Service method

Add a method to the service of the feature (`ParameterService`, `AdminService`), or a new `@Service` in a new feature
package. Keep it stateless: take everything as parameters, inject collaborators through the constructor.

```java
public AcquiredParameterValue get(ParameterAddress address, RbacSession session) throws ParameterException {
    Parameter parameter = parameters.newParameter(address.parameterName());
    Selector selector = SelectorFactory.newSelector(address.selector());
    return RbacSession.callAs(session, () -> parameter.getValue(selector));
}
```

- Device names from the request go through `ParameterAddress`; do not build JAPC names by hand.
- Every JAPC call runs inside `RbacSession.callAs` / `runAs`. A call outside it runs without the user's token.
- Throw; do not catch to build a response. `ParameterException`, `CodecException`, `AuthenticationException` and
  Spring's `ResponseStatusException` are already mapped by `ErrorCode`.

### 3. Controller method

```java
@GetMapping({ApiPaths.DEVICE_PROPERTY, ApiPaths.DEVICE_PROPERTY_FIELD})
public ResponseEntity<NamedValue> get(@PathVariable("device") String device, ...) throws ParameterException {
    AcquiredParameterValue acquired = parameters.get(new ParameterAddress(device, property, field, selector),
            RbacAuthentication.required());
    return ResponseEntity.ok().headers(stamps(acquired)).body(new NamedValue(...));
}
```

- Get the caller with `RbacAuthentication.required()`.
- Return a value object, not bytes. For a device value return `NamedValue` (encoded by whichever format the client
  asked for). For JSON-only data, as in the admin API, return a record.
- Take a device value in the body as `ParameterValue` (JSON) or `LabVIEWBody` (LabVIEW).

### 4. A new error

If the failure is a new exception type, add it to `ErrorCode.of` and `ErrorCode.httpStatus`. Reuse the six codes.

### 5. Tests

- Service logic: a plain unit test with a stub `ParameterProvider` (`ParameterServiceTest`).
- The route: add cases to `ApiContractTest`, which starts the real application with a stub device. Cover at least
  success, no login (401), wrong role (403) and malformed input (400).

### 6. Specification and documentation

Add the route to `docs/openapi.yml` (or `docs/asyncapi.yml`) in the same commit, with its error cases. CI lints the
files; the running service serves them under `/docs`.

## Adding an API version

See [ADR 6](adr/0006-api-versioning.md). Only for a change that breaks existing clients; everything else is added to
the current version.

1. Add `API_V2` and `WS_V2` to `ApiPaths`, and the new prefix to `stripWebSocketBase`.
2. Put the new controllers in their own package and map them to `{ApiPaths.API, ApiPaths.API_V2}`. The unversioned
   prefix moves with them: remove `ApiPaths.API` from the v1 controllers, which keep `ApiPaths.API_V1`.
3. Keep v1 working and tested for as long as a client uses it. The request log (user and target path of every request) shows who still does.
4. Give the new version its own specification file; do not edit the v1 files except to mark them superseded.

## Checklist

- [ ] Route is covered by a matcher that requires a role.
- [ ] No JAPC call outside `RbacSession.callAs`.
- [ ] Input from the client is validated (`ParameterAddress`, typed parameters) before use.
- [ ] No exception text from internals in a response; no secret in a log line.
- [ ] Tests for success, 401, 403 and bad input.
- [ ] `docs/openapi.yml` / `docs/asyncapi.yml` updated.
