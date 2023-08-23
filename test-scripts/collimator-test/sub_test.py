import requests
from websocket import create_connection
from collections import deque
from multiprocessing import Pool
import argparse
import os


def subscribe(device):
    secure = True
    server = "japc-dev-rade-services.app.cern.ch"
    metadata = {"username": "ncass", "password": "", "req_action": "SUBSCRIBE",
                "req_lang": "PYTHON", "selector": "", "device": device,
                "property": "CollimatorStatus", "auth_type": "RBAC_EXPLICIT", "rbac_id": 0}
    headers = {"MetaData": str(metadata)}
    if secure:
        post_protocol = 'https://'
        ws_protocol = 'wss://'
    else:
        post_protocol = 'http://'
        ws_protocol = 'ws://'

    response = requests.post(post_protocol + server + '/request', headers=headers)

    sessionid = response.text

    url = ws_protocol + server + '/subs/' + sessionid
    ws = create_connection(url=url)
    vals = deque(maxlen=3)
    try:
        while True:
            vals.append(ws.recv())

    except Exception as e:
        print(e)
        f = open("./logs", "a")
        f.write("An exception occurred in sub test for device: " + device + "\n Exception details: {}".format(e))
        f.close()
        print("FAILED TEST FOR: " + device)
        print("An exception occurred in sub test for device: " + device + "\n Exception details: {}".format(e))
        ws.close()
        return vals

def printDeque(de):
    vals = ""
    for i in range(0, len(de)):
        if i!= (len(de) - 1):
            vals+=de[i]+'\n'
        else:
            vals+=de[i]
    return vals

collimators = []
with open("collimators.csv") as f:
    for row in f:
        collimators.append(row.split(',')[0])
tests = []
collimators.pop(0)
print(len(collimators))
with Pool(len(collimators)) as p:
    vals = p.map(subscribe, collimators)
for i in range(0, len(vals)):
    print(printDeque(vals[i]))

# argParser = argparse.ArgumentParser()
# argParser.add_argument("-d", "--device", help="device name")
# argParser.add_argument("-t", "--tls", help="secure connection or not", default=False, action='store_true')
# args = argParser.parse_args()

# server = "collimator-test-rade-services.app.cern.ch"
# server="localhost:8080"
# server = "cwe-513-vpl581.cern.ch:8080"

# print("RBAC_ID: " + str(dict(eval(response.headers.get("MetaData")))["rbac_id"]))
# print("SUB_ID: " + sessionID)
