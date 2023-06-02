import requests
from websocket import create_connection
import argparse

argParser = argparse.ArgumentParser()
argParser.add_argument("-d", "--device", help="device name")
argParser.add_argument("-t", "--tls", help="secure connection or not", default=False, action='store_true')
args = argParser.parse_args()

metadata = {"username": "ncass", "req_action": "SUBSCRIBE",
            "req_lang": "PYTHON", "selector": "", "device": args.device,
            "property": "doubles", "auth_type": "NONE", "rbac_id": 0}

#-d rade-japc-test01
headers = {"MetaData": str(metadata)}
post_protocol = ''
ws_protocol = ''
if args.tls:
    post_protocol = 'https://'
    ws_protocol = 'wss://'
else:
    post_protocol = 'http://'
    ws_protocol = 'ws://'

response = requests.post(post_protocol + "localhost:8080" + '/request', headers=headers)
sessionID = response.text
print(sessionID)

url = ws_protocol + "localhost:8080" + '/subs/' + sessionID
ws = create_connection(url=url)
try:
    while True:
        print(ws.recv())
except Exception:
    ws.close()
    print("FAILED")
