# 2. JSON and LabVIEW as interchangeable formats

Status: accepted

## Context

The same GET, SET and subscription must be available to LabVIEW (flattened binary data plus a type descriptor) and to
everything else (JSON). Errors must reach the client in the format it understands, including errors raised before a
controller runs (authentication, routing).

## Decision

- A `Format` describes one wire format: its media type, WebSocket subprotocol, HTTP message converter, error encoding
  and subscription frames. `Formats.ALL` lists them; JSON is first and therefore the default.
- REST uses standard content negotiation: `Accept` selects the response format, `Content-Type` the request format.
  WebSocket uses `Sec-WebSocket-Protocol`.
- Controllers return format-neutral values (`NamedValue`, `LabVIEWValue`) and receive decoded or decodable bodies
  (`ParameterValue`, `LabVIEWBody`). Encoding and decoding live in rade-codec.
- Errors are written by `Format.errorResponse` wherever they arise: `ErrorHandler` for controllers,
  `SecurityErrorWriter` for the security filters, the handshake interceptor for subscriptions.

## Consequences

- A new format is one `Format` subclass, one converter and one entry in `Formats.ALL`.
- Controllers contain no format-specific code, except the routes that exist only for LabVIEW (property named by the
  type descriptor).
- A LabVIEW SET body cannot be decoded by the converter alone: enums need the JAPC descriptor of the parameter. The
  converter therefore produces a `LabVIEWBody` and the service decodes it once the parameter is known (`ValueSource`).
