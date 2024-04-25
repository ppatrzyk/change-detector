# change-detector

Flink job for observing stream of text content and detecting changes.

![diagram](diagram.png)

## Details

Content needs to be published to rabbitmq queue `contentqueue` in the following JSON format:

```
{
    "key": <source_id>,
    "ts": <content_time>,
    "content": <actual_text>
}
```

This repo contains a simple python script that gets a website and publishes it to queue. Url can be configured with env variables in [docker compose file](docker-compose.yml).

## Running instructions

```
# 1 Build image for content_generator
# from /content_generator
docker build . -t content-generator:0.1

# 2. build flink job
mvn clean package

# 3. run
docker-compose up -d
```

Exposed via http:
- Rabbit dashboard: http://localhost:15672
- Flink dashboard: http://localhost:8081
