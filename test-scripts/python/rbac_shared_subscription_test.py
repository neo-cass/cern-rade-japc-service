import argparse
import base64
import json
import ssl
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path

import websocket

DEFAULT_ENV = Path(__file__).resolve().parents[2] / "pma.env"


def load_rbac_header(env_path):
    values = {}
    for line in env_path.read_text(encoding="utf-8").splitlines():
        if "=" in line and not line.lstrip().startswith("#"):
            key, value = line.split("=", 1)
            values[key.strip()] = value.strip().strip('"').strip("'")
    credentials = f"{values['username']}:{values['password']}".encode("iso-8859-1")
    return "Explicit " + base64.urlsafe_b64encode(credentials).decode("ascii")


def rest(base, path, rbac, method="GET", body=None):
    data = json.dumps(body).encode() if body is not None else None
    request = urllib.request.Request(base + "/api/v1/" + path, data=data, method=method)
    if data is not None:
        request.add_header("Content-Type", "application/json")
    if rbac:
        request.add_header("Rbac", rbac)
    try:
        with urllib.request.urlopen(request, timeout=30, context=ssl.create_default_context()) as response:
            content = response.read()
            return response.status, json.loads(content) if content else {}
    except urllib.error.HTTPError as e:
        return e.code, json.loads(e.read() or b"{}")


def rest_get(base, path, rbac):
    return rest(base, path, rbac)


def set_property(base, parameter, query, rbac, value):
    return rest(base, f"{parameter}{query}", rbac, "PUT", value)


def field_value(frame, field):
    return frame.get("value", {}).get(field, {}).get("value")


def subscribe(ws_base, path, rbac):
    header = [f"Rbac: {rbac}"] if rbac else []
    try:
        ws = websocket.create_connection(ws_base + "/ws/v1/" + path, header=header, subprotocols=["json"], timeout=30)
    except websocket.WebSocketBadStatusException as e:
        return None, e.status_code, describe_rejection(e)
    return ws, 101, None


def describe_rejection(e):
    try:
        body = json.loads(e.resp_body)
        return f"code {body.get('code')}: {body.get('message')}"
    except (TypeError, ValueError):
        return str(e)


def receive_data(ws, count, timeout):
    deadline = time.monotonic() + timeout
    frames = []
    while len(frames) < count and time.monotonic() < deadline:
        ws.settimeout(max(deadline - time.monotonic(), 0.1))
        try:
            opcode, frame = ws.recv_data_frame(control_frame=True)
        except websocket.WebSocketTimeoutException:
            break
        if opcode != websocket.ABNF.OPCODE_TEXT:
            continue
        message = json.loads(frame.data)
        frames.append(message)
        if message.get("type") == "error":
            break
    return frames


def summary(frames):
    return ", ".join(
        f"{f['type']} acq={f.get('acqStamp')}" if f.get("type") == "data" else f"error {f.get('code')}: {f.get('message')}"
        for f in frames
    )


class Result:
    def __init__(self):
        self.checks = []

    def check(self, name, passed, detail):
        self.checks.append(passed)
        print(f"[{'PASS' if passed else 'FAIL'}] {name}: {detail}")


def main():
    parser = argparse.ArgumentParser(description="Shows whether a client without RBAC can join an RBAC-protected subscription opened by another client.")
    parser.add_argument("--base", default="http://localhost:8443", help="Service base URL")
    parser.add_argument("--parameter", default="rade-service-test-02/MyScalars", help="device/property")
    parser.add_argument("--selector", default=None)
    parser.add_argument("--env", type=Path, default=DEFAULT_ENV, help="File with username= and password=")
    parser.add_argument("--frames", type=int, default=1)
    parser.add_argument("--timeout", type=float, default=15)
    parser.add_argument("--set-field", default=None, help="Scalar field to change with RBAC while the client without RBAC is subscribed; restored afterwards")
    args = parser.parse_args()

    rbac = load_rbac_header(args.env)
    query = f"?selector={args.selector}" if args.selector else ""
    path = args.parameter + query
    ws_base = args.base.replace("https://", "wss://").replace("http://", "ws://")
    result = Result()

    status, body = rest_get(args.base, path, None)
    result.check("1. REST GET without RBAC is denied", status in (401, 403), f"HTTP {status}, code {body.get('code')}: {body.get('message')}")
    status, body = rest_get(args.base, path, rbac)
    result.check("2. REST GET with RBAC is allowed", status == 200, f"HTTP {status}")
    original = body if args.set_field and args.set_field in body else None

    ws, status, detail = subscribe(ws_base, path, None)
    if ws:
        frames = receive_data(ws, 1, args.timeout)
        ws.close()
        detail = summary(frames)
    result.check("3. Subscription without RBAC, alone, is denied", ws is None or any(f["type"] == "error" for f in frames), f"HTTP {status}, {detail}")
    time.sleep(2)

    owner, status, detail = subscribe(ws_base, path, rbac)
    if not owner:
        result.check("4. Subscription with RBAC is allowed", False, f"HTTP {status}, {detail}")
        return 1
    owner_frames = receive_data(owner, 1, args.timeout)
    result.check("4. Subscription with RBAC is allowed", owner_frames[0]["type"] == "data", summary(owner_frames))

    joiner, status, detail = subscribe(ws_base, path, None)
    if joiner:
        joiner_frames = receive_data(joiner, args.frames, args.timeout)
        leaked = bool(joiner_frames) and all(f["type"] == "data" for f in joiner_frames)
        detail = f"HTTP {status}, {summary(joiner_frames)}"
    else:
        leaked = False
        detail = f"HTTP {status}, {detail}"
    result.check("5. Subscription without RBAC while an RBAC subscription is open is denied", not leaked, detail)

    if joiner and leaked and original:
        old = original[args.set_field]
        new_value = (not old["value"]) if isinstance(old["value"], bool) else old["value"] + 1
        changed = dict(original, **{args.set_field: dict(old, value=new_value)})
        try:
            status, body = set_property(args.base, args.parameter, query, rbac, changed)
            if status != 200:
                result.check("5b. Client without RBAC does not receive the update after a SET with RBAC", False, f"SET failed: HTTP {status}, code {body.get('code')}: {body.get('message')}")
            else:
                received = []
                deadline = time.monotonic() + args.timeout
                while time.monotonic() < deadline and not any(field_value(f, args.set_field) == new_value for f in received):
                    received += receive_data(joiner, 1, deadline - time.monotonic())
                got = [field_value(f, args.set_field) for f in received]
                result.check("5b. Client without RBAC does not receive the update after a SET with RBAC",
                             new_value not in got,
                             f"SET {args.set_field}={new_value} with RBAC, client without RBAC received {args.set_field}={got}")
        finally:
            status, _ = set_property(args.base, args.parameter, query, rbac, original)
            print(f"       restored {args.set_field}={old['value']}: HTTP {status}")
    if joiner:
        joiner.close()

    owner.close()
    time.sleep(2)

    ws, status, detail = subscribe(ws_base, path, None)
    if ws:
        frames = receive_data(ws, 1, args.timeout)
        ws.close()
        detail = summary(frames)
    result.check("6. Subscription without RBAC after the RBAC subscription closed is denied", ws is None or any(f["type"] == "error" for f in frames), f"HTTP {status}, {detail}")

    print()
    if all(result.checks):
        print("RBAC is enforced per subscriber.")
    elif result.checks[:4] == [True] * 4 and not result.checks[4]:
        print("Confirmed: a client without RBAC received data from an RBAC-protected parameter by joining an existing subscription.")
    else:
        print("Inconclusive: preconditions 1-4 did not hold, see above.")
    return 0 if all(result.checks) else 1


if __name__ == "__main__":
    sys.exit(main())
