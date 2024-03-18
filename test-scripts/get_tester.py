import requests as rq
import base64
username = ""
password =""
userpass = username + ":" + password
token = base64.b64encode(userpass.encode("iso-8859-1")).decode("iso-8859-1")
headers = {"Rbac": "Explicit " + token}
response = rq.get("https://ncass-masters-restify-rade-services.app.cern.ch/get/NONE/rade-service-test-01/MyScalars/myBoolean", headers=headers)
print(response.content)
print(response.headers)