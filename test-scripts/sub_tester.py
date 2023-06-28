import requests
from websocket import create_connection

secure = False
server = "localhost:8080"
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

sessionid = response.text
print(sessionid)
url = ws_protocol + server + '/subs/' + sessionid
ws = create_connection(url=url)

try:
    while True:
        print(ws.recv())
        #ws.pong()


except Exception:
    ws.close()
    print("lol")
