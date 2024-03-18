import websocket as websocket
import base64

url = "wss://ncass-masters-restify-rade-services.app.cern.ch/subscription/labview"
sub_url = "NONE/rade-service-test-01/MyScalars"
username = ""
password =""
userpass = username + ":" + password
token = base64.b64encode(userpass.encode("iso-8859-1")).decode("iso-8859-1")
rbac_creds = "Rbac:Explicit " + token
headers = ["Subscription-Url:" + sub_url, rbac_creds]

websocket.enableTrace(True)
ws = websocket.WebSocketApp(url, header=headers)
ws.run_forever()

