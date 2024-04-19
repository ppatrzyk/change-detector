FROM debian:12.5

RUN apt-get update \
    && apt-get install -y \
    ca-certificates \
    curl \
    --no-install-recommends \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

COPY ./get_site.sh /usr/sbin/get_site.sh

CMD ["bash", "/usr/sbin/get_site.sh"]
