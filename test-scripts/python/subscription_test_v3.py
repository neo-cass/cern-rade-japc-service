import time

import websocket as websocket
import threading
import base64


def on_message(ws, message):
    print(message)


def on_open(ws):
    def run():
        time.sleep(10)
        ws.close()
        print("WebSocket closed")

    threading.Thread(target=run).start()


websocket.enableTrace(True)

url = "wss://ncass-masters-v3-rade-services.app.cern.ch/subscription/labview"
sub_url_device_property = "NONE/rade-japc-test01/test_property"
sub_url_device = "NONE/rade-japc-test01"
sub_url_rbac = "NONE/rade-service-test-01"
sub_url_device_property_field = "NONE/rade-japc-test01/test_property/int_array_field"

# if you need to test with RBAC this is how you would do it
username = ""
password = ""
userpass = username + ":" + password
token = base64.urlsafe_b64encode(userpass.encode("iso-8859-1")).decode("iso-8859-1")
rbac_header = "Rbac:Explicit " + token

# Base64 encoded type descriptor from LabVIEW for test_property without int_array_field
type_descriptor_test_property_filter = "AEhAUAADAA5ACglkYmxfZmllbGQAFkAw_____wxzdHJpbmdfZmllbGQAABBABApsb25nX2ZpZWxkAA10ZXN0X3Byb3BlcnR5"
# Base64 encoded type descriptor from LabVIEW for MyScalars
type_descriptor_MyScalars = "ACpAUAACAAxAAwdteUludDMyAA5AIQlteUJvb2xlYW4JTXlTY2FsYXJz"

headers = ["Subscription-Url:" + sub_url_rbac, "Type-Descriptor:" + type_descriptor_MyScalars, rbac_header]
ws1 = websocket.WebSocketApp(url, header=headers, on_message=on_message, on_open=on_open)
ws1.run_forever()

headers = ["Subscription-Url:" + sub_url_device_property]
ws2 = websocket.WebSocketApp(url, header=headers, on_message=on_message, on_open=on_open)
ws2.run_forever()

headers = ["Subscription-Url:" + sub_url_device_property_field]
ws3 = websocket.WebSocketApp(url, header=headers, on_message=on_message, on_open=on_open)
ws3.run_forever()

headers = ["Subscription-Url:" + sub_url_device, "Type-Descriptor:" + type_descriptor_test_property_filter]
ws4 = websocket.WebSocketApp(url, header=headers, on_message=on_message, on_open=on_open)
ws4.run_forever()
