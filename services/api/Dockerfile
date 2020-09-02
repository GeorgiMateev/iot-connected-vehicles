FROM python:3.8-slim-buster
RUN apt-get clean \
    && apt-get -y update

RUN apt-get -y install \
    nginx \
    python3-dev \
    build-essential \
    supervisor

COPY nginx.conf /etc/nginx/nginx.conf

WORKDIR /app

COPY requirements.txt /app/requirements.txt
RUN pip install -r requirements.txt --src /usr/local/src

COPY app.py /app/app.py
COPY vehicles_data_provider.py /app/vehicles_data_provider.py

COPY uwsgi.ini /app/uwsgi.ini
COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf
COPY startup.sh /app/startup.sh

RUN chmod +x ./startup.sh

EXPOSE 8080
CMD [ "./startup.sh" ]