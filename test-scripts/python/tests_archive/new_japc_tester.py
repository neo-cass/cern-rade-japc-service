import websocket as websocket

import argparse

metadata = "MetaData: {\"req_action\": \"SUBSCRIBE\", \"req_lang\": \"LABVIEW\", \"selector\": \"\", \"req_url\": \"rade-japc-test01/doubles\"}"
# metadata = "MetaData: {\"username\": \"ncass\", \"password\": \"\", \"req_action\": \"SUBSCRIBE\", \"req_lang\": \"LABVIEW\", \"selector\": \"\", \"req_url\": \"rade-japc-test01/doubles\", \"auth_type\": \"RBAC_EXPLICIT\"}"

# wscat --header "MetaData: {\"username\": \"ncass\", \"password\": \"\", \"req_action\": \"SUBSCRIBE\", \"req_lang\": \"LABVIEW\", \"selector\": \"\", \"device\": \"TCDIV.88121\", \"property\": \"ExpertStatus\", \"auth_type\": \"RBAC_EXPLICIT\"}" --connect ws://localhost:8443/subscription
url = "ws://localhost:8443/test/subscription"
headers = "Subscription-Url:""/rade-japc-test01/doubles"

websocket.enableTrace(True)
ws = websocket.WebSocketApp(url, header=[headers])
ws.run_forever()
