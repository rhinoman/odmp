#  Copyright (c) 2020. James Adam and the Open Data Management Platform contributors.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

from flask import Flask, request
from plugin_configuration import config
from consul import Consul, Check
import base64
import json
import logging
from os import getenv
import uuid
import socket
import atexit

plugin = Flask(__name__)
port = int(getenv("PLUGIN_PORT", 8015))
logger = logging.getLogger()


@plugin.route("/config", methods=['GET'])
def getConfig():
    plugin.logger.info('Config Requested')
    return config


@plugin.route("/process", methods=['GET', 'POST'])
def proc():
    props = decode_headers(request)
    code = props['code']
    data = request.get_data(cache=False, as_text=False, parse_form_data=False)
    result = execute(code, data)
    return result


def execute(code, data):
    exec(code)
    return locals()['process'](data)


def decode_headers(http_req):
    enc_props = http_req.args.get('properties')
    byte_props = base64.urlsafe_b64decode(enc_props).decode('UTF-8')
    props = json.loads(byte_props)
    return props


# Register this plugin with Consul
def register_plugin():
    logger.info("Registering plugin with Consul")
    c = Consul()

    hname = socket.gethostname()
    ipaddr = socket.gethostbyname(hname)

    health_check = Check.http(url=f'http://{ipaddr}:{port}/config', interval="20s")
    service_name = config['serviceName']
    service_id = f'{service_name}-{str(uuid.uuid4())}'

    c.agent.service.register(name=service_name,
                             service_id=service_id,
                             address=ipaddr,
                             port=port,
                             tags=['secure=false'],
                             check=health_check)
    atexit.register(deregister, c, service_id)


def deregister(c, service_id):
    """
    Deregister the service on exit
    | c - Consul connection
    | service_id - The service id of this instance registered with Consul
    """
    logging.info("De-registering plugin")
    c.agent.service.deregister(service_id)


if __name__ == "__main__":
    register_plugin()
    plugin.run(host='0.0.0.0', port=port)
