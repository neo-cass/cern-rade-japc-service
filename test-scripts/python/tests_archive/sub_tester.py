import asyncio

import requests
from websocket import create_connection

secure = False
server = "localhost:8443"
metadata = {"username": "ncass", "password": "", "req_action": "SUBSCRIBE",
            "req_lang": "PYTHON", "selector": "", "device": "rade-service-test-01",
            "property": "MyScalars", "auth_type": "RBAC_EXPLICIT", "rbac_id": 0}
headers = {"MetaData": str(metadata)}
if secure:
    post_protocol = 'https://'
    ws_protocol = 'wss://'
else:
    post_protocol = 'http://'
    ws_protocol = 'ws://'

response = requests.post(post_protocol + server + '/request', headers=headers)

metadata = dict(eval(response.headers.get("MetaData")))
print("rbac_id: " + str(metadata["rbac_id"]))
sessionid = metadata["sub_id"]
url = ws_protocol + server + '/subs/' + sessionid
ws = create_connection(url=url)
cont = True
while (cont):
    ws.recv()
    choice = input("press any key for value, type ! to stop")
    if choice == '!':
        cont=False
ws.close()
