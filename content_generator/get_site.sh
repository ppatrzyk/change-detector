#!/usr/bin/env bash
set -e

# env vars to be set
# URL: url of content ot get
# RABBIT: rabbitms api url to post messages
# RABBIT_LOGIN
# RABBIT_PASS

while true
do
    content=$(curl -s $URL | sed 's/"/\\"/g' | sed -z 's/\n/\\\\n/g')
    ts=$(date +"%Y-%m-%dT%H:%M:%S%z")
    id=$(uuidgen)
    payload="{\\\"key\\\": \\\"$id\\\", \\\"ts\\\": \\\"$ts\\\", \\\"content\\\": \\\"$content\\\"}"
    post_data="{\"properties\":{\"correlation_id\": \"$id\"},\"routing_key\":\"\",\"payload\":\"$payload\",\"payload_encoding\":\"string\"}"
    curl -s -u $RABBIT_LOGIN:$RABBIT_PASS -H "Content-type: application/json" -X POST $RABBIT -d "$post_data"
    sleep 1
done
