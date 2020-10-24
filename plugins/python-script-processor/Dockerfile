FROM python:3.8-buster

WORKDIR /usr/src/plugin

COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

CMD ["gunicorn", "./plugin.py"]
