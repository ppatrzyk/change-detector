from datetime import datetime, timezone
import httpx
import json
import pika
import os
import sys
import time
import uuid

RABBIT_CONN_STR = os.environ["RABBIT"]
RABBIT_EXCHANGE = os.environ["RABBIT_EXCHANGE"]
URL = os.environ["URL"]

if __name__ == '__main__':
    rabbit_conn = pika.connection.URLParameters(RABBIT_CONN_STR)
    with httpx.Client() as httpx_client, pika.BlockingConnection(rabbit_conn) as rabbit_client:
        channel = rabbit_client.channel()
        while True:
            try:
                response = httpx_client.get(URL)
                response.raise_for_status()
                ts = datetime.now(tz=timezone.utc).isoformat()
                message = {"key": URL, "ts": ts, "content": response.text}
                kwargs = {
                    "properties": pika.spec.BasicProperties(correlation_id=str(uuid.uuid4())),
                    "exchange": RABBIT_EXCHANGE,
                    "routing_key": RABBIT_EXCHANGE,
                    "body": json.dumps(message),
                }
                channel.basic_publish(**kwargs)
                sys.stdout.write(f"Message published: {ts}")
            except Exception as e:
                sys.stdout.write(f"Error: {str(e)}")
            time.sleep(1)
