import websocket as websocket
import base64

url = "wss://ncass-masters-v3-rade-services.app.cern.ch/subscription/labview"
sub_url = "NONE/rade-japc-test01"

# if you need to test with RBAC this is how you would do it
# username = ""
# password =""
# userpass = username + ":" + password
# token = base64.urlsafe_b64encode(userpass.encode("iso-8859-1")).decode("iso-8859-1")
# rbac_header = "Rbac:Explicit " + token

# This is a URL safe Base64 encoded type descriptor
# I'll send you the labview code I used to do this for set and get.
type_descriptor = "AGZAUAAEAA5ACglkYmxfZmllbGQAFkAw_____wxzdHJpbmdfZmllbGQAABBABApsb25nX2ZpZWxkAAAeQEAAAf____8ABAADD2ludF9hcnJheV9maWVsZA10ZXN0X3Byb3BlcnR5"
headers = ["Subscription-Url:" + sub_url, "Type-Descriptor:" + type_descriptor]

websocket.enableTrace(True)
ws = websocket.WebSocketApp(url, header=headers)
ws.run_forever()
