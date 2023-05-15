import requests
from websocket import create_connection
import argparse

argParser = argparse.ArgumentParser()
argParser.add_argument("-d", "--device", help="device name")
argParser.add_argument("-p", "--property", help="property name")
argParser.add_argument("-u", "--username", help="your username")
argParser.add_argument("-a", "--authorisation", help="your password or token")
argParser.add_argument("-s", "--selector", help="selector name")
argParser.add_argument("-l", "--location", help="host name")
argParser.add_argument("-r", "--action", help="request type")
argParser.add_argument("-e", "--language", help="language to be encoded into on response")
argParser.add_argument("-t", "--tls", help="secure connection or not", default=False, action='store_true')
args = argParser.parse_args()

metadata = {"username": args.username, "password": args.authorisation, "request_action": args.action,
            "request_language": args.language, "selector": args.selector, "device": args.device,
            "property": args.property}

headers = {"MetaData": str(metadata)}
post_protocol = ''
ws_protocol = ''
if args.tls:
    post_protocol = 'https://'
    ws_protocol = 'wss://'
else:
    post_protocol = 'http://'
    ws_protocol = 'ws://'

response = requests.post(post_protocol + args.location + '/request', headers=headers)
sessionID = response.text
print(sessionID)

#if args.request == 'PY_SUBSCRIBE':
 #   url = ws_protocol + args.location + '/subs/' + sessionID
  #  ws = create_connection(url=url)
   # try:
    #    while True:
     #       print(ws.recv())
    #except KeyboardInterrupt:
     #   ws.close()
