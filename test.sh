#!/bin/bash

HOST=localhost:8080

if [ "$1" == "gae" ]; then
  HOST=skinny-1366.appspot.com
fi

case "$2" in
  export)
    curl -v http://$HOST/ecwid/export/
    ;;
  file)
    curl -v http://$HOST/f/ecwid.xml
    ;;
  logs)
    curl -v http://$HOST/logs/
    ;;
  post) 
    curl -v -H "Content-Type: application/json" -X PUT -d '{"fulfillmentStatus":"SHIPPED"}' https://app.ecwid.com/api/v3/9161129/orders/1?token=TZmFgJfvWPhhNJmJQzcfrRmYnSQGdhVQ
    ;;
  *)
    echo $"Usage: $0 {export|file}"
esac
