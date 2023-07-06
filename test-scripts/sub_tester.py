import requests
from websocket import create_connection

secure = False
# server = "springboot-test-rade-services.app.cern.ch"
server="localhost:8080"
metadata = {"username": "ncass", "password": "", "req_action": "SUBSCRIBE",
            "req_lang": "PYTHON", "selector": "", "device": "rade-japc-test01",
            "property": "doubles", "auth_type": "NONE", "rbac_id": 0}
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
    choice = None
    val = None
    while choice != 'stop':
        choice = input()
        val = ws.recv()
        print(val)
        print(choice)
    ws.send("CLOSE")
    if ws.recv() == "DONE":
        ws.close()

except Exception:
    ws.close()
    print("lol")
