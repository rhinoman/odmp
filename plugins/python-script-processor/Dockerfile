FROM centos:8

RUN dnf update -y \
    && dnf install -y gcc python38 \
    && dnf clean all \
    && rm -rf /var/cache/dnf

RUN alternatives --set python /usr/bin/python3.8

WORKDIR /usr/src/plugin

COPY requirements.txt ./
RUN pip3 install --no-cache-dir -r requirements.txt

COPY . .

CMD ["gunicorn", "--bind", "0.0.0.0:8015", "wsgi"]
