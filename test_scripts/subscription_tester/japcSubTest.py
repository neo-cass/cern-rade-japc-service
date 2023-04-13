import requests
from websocket import create_connection
import argparse

argParser = argparse.ArgumentParser()
argParser.add_argument("-d", "--device", help="device name")
argParser.add_argument("-p", "--property", help="property name")
argParser.add_argument("-u", "--username", help="your username")
argParser.add_argument("-a", "--password", help="your password")
argParser.add_argument("-s", "--selector", help="selector name")
argParser.add_argument("-r", "--server", help="request server name")
argParser.add_argument("-t", "--secure", help="secure connection or not", default=False, action='store_true')
args = argParser.parse_args()

metadata = {"username": args.username, "password": args.password, "request_type": "PY_SUBSCRIBE", "selector": args.selector,
            "device": args.device, "property": args.property}

# data = hex(0x002240500002000A400A0464626C3000000A400A0464626C310007646F75626C65730000000000000000BFF0000000000000)
headers = {"MetaData": str(metadata)}
post_protocol = ''
ws_protocol = ''
if args.secure==True:
    post_protocol = 'https://'
    ws_protocol = 'wss://'
else:
    post_protocol = 'http://'
    ws_protocol = 'ws://'
response = requests.post(post_protocol + args.server + '/request', headers=headers)
sessionID = response.text
print(sessionID)
url = ws_protocol + args.server + '/subs/' + sessionID
ws = create_connection(url=url)
try:
    while True:
        print(ws.recv())
except KeyboardInterrupt:
    ws.close()