FROM python:3.12.3-slim

COPY ./ ./content_generator/
WORKDIR /content_generator
RUN pip3 install -r requirements.txt

CMD [ "python3", "get_site.py" ]