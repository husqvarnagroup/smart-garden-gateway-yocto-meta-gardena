#!/usr/bin/python3
# coding: utf-8
#
# Author: Herbert Mertig <herbert.c.mertig@consultant.husqvarnagroup.com>
# Author: Andreas Müller <andreas.mueller@husqvarnagroup.com>
#

"""
Client for CPMS site server.
"""

import json
from base64 import b64encode
import http.client
from http import HTTPStatus
from urllib.parse import urlencode

# pylint: disable=missing-docstring

class CPMSClient: # pylint: disable=too-many-public-methods

    def __init__(self, host, user, pwd):
        self.host = host
        self.url = "http://localhost:8000/api/iprconfig"
        self.basepath = '/api/iprconfig'
        self.user = user
        self.pwd = pwd
        userpass_b64 = b64encode((user+":"+pwd).encode('ascii')).decode('ascii')
        self.auth_headers = {'Authorization' : "Basic %s" % userpass_b64}

    def perform_request(self, method, path, headers=None, params=None, body=None, timeout=30, raw=False): # pylint: disable=too-many-arguments
        if headers is None:
            headers = {}
        connection = http.client.HTTPConnection(self.host, timeout=timeout)
        connection.request(method,
                           self.basepath + path + ("?" + urlencode(params) if params else ""),
                           body=body,
                           headers={**self.auth_headers, **headers})
        response = connection.getresponse()

        if not response.status in (HTTPStatus.OK, HTTPStatus.CREATED, HTTPStatus.NO_CONTENT):
            raise http.client.HTTPException(response.status, response.reason, response.read())

        body = response.read()
        connection.close()
        return body if raw else body.decode('ascii')

    def get_version(self):
        return self.perform_request("GET", '/version')

    def get_environment(self):
        return self.perform_request("GET", '/environment')

    def get_batch_info(self):
        result = self.perform_request("GET", '/batch/info',
                                      headers={'Accept': 'application/json'})
        return json.loads(result)

    def create_batch(self, template_id, quantity, template_params, timeout=120.0):
        params = {
            'template_id': template_id,
            'quantity': quantity,
            'parameters': template_params
        }
        result = self.perform_request("POST", '/batch',
                                      headers={'Accept': 'application/json',
                                               'Content-Type': 'application/json'},
                                      body=json.dumps(params),
                                      timeout=timeout)
        return json.loads(result)['batch-id']

    def deploy_batch(self, batch_id, timeout=30.0):
        self.perform_request("POST", '/batch/' + batch_id, timeout=timeout)

    def deploy_item_batch(self, item_id, timeout=300.0):
        self.perform_request("POST", '/' + item_id, timeout=timeout)

    def finalize_batch(self, batch_id):
        self.perform_request("POST", '/batch/' + batch_id + '/finalize')

    def delete_batch(self, batch_id, async_):
        self.perform_request("DELETE", '/batch/' + batch_id,
                             params={'async': 'true' if async_ else 'false'})

    def get_next_free_item(self, batch_id):
        result = self.perform_request("POST", '/next/' + batch_id,
                                      headers={'Accept': 'application/json'})
        return json.loads(result)

    def get_item(self, item_id):
        result = self.perform_request("GET", '/' + item_id,
                                      headers={'Accept': 'application/json'})
        return json.loads(result)

    def find_item(self, identifier):
        result = self.perform_request("GET", '/find/' + identifier,
                                      headers={'Accept': 'application/json'})
        return json.loads(result)

    def take_item(self, item_id):
        result = self.perform_request("POST", '/' + item_id + '/take',
                                      headers={'Accept': 'application/json'})
        return json.loads(result)

    def update_item(self, item, final=False):
        self.perform_request("PUT", '/' + item['ipr_id'],
                             headers={'Content-Type': 'application/json'},
                             body=json.dumps(item),
                             params={'final': final})

    def get_sync_info(self):
        return self.perform_request("GET", '/sync')

    def get_batch_sync_info(self, batch_id):
        return self.perform_request("GET", '/sync/' + batch_id)

    def finalize_item(self, item_id):
        self.perform_request("POST", '/' + item_id + '/finalize')

    def assemble_items(self, parent_id, child_name, child_id, final=False):
        self.perform_request("POST", '/' + parent_id + '/assembly/' + child_name,
                             headers={'Content-Type': 'application/json'},
                             body=json.dumps(child_id),
                             params={'final': final})

    def disassemble_items(self, parent_id, child_name):
        self.perform_request("DELETE", '/' + parent_id + '/assembly/' + child_name)

    def disassemble_item_if_assembled(self, child_id):
        """Check if the given IPR item is already assembled (i.e. has
        a parent) and disassemble it if necessary."""
        # Note: though multiple assemble calls are possible, parent just
        # contains the latest parent. There is no way to find older parents.
        child = self.get_item(child_id)
        # check if item has a parent
        if 'parent' in child.keys():
            parent = self.get_item(child['parent'])
            paths = [item[0] for item in parent['components'].items() if item[1]['ipr_id'] == child_id]
            for path in paths:
                self.disassemble_items(parent['ipr_id'], path)

    def get_keys(self, item_id):
        result = self.perform_request("GET", '/' + item_id + '/keys',
                                      headers={'Accept': 'application/json'})
        return json.loads(result)

    def get_software_info(self, item_id):
        result = self.perform_request("GET", '/' + item_id + '/software',
                                      headers={'Accept': 'application/json'})
        return json.loads(result)

    def get_file(self, file_id):
        return self.perform_request("GET", '/file/' + file_id,
                                    headers={'Accept': 'application/octet-stream'},
                                    raw=True)

    def get_file_by_name_and_version(self, item_id, software_name, version, file_name):
        return self.perform_request(
            "GET", '/' + item_id + '/software/' + software_name + '/' + version + '/' + file_name,
            headers={'Accept': 'application/octet-stream'},
            raw=True)

    def get_certificates(self, item_id):
        result = self.perform_request("GET", '/%s/certificates' % item_id,
                                      headers={'Accept': 'application/json'})
        return json.loads(result)

    def set_tag(self, item_id, tag_name, tag_value):
        self.perform_request("PUT", '/' + item_id + '/tag/' + tag_name,
                             headers={'Content-Type': 'application/json'},
                             body=json.dumps(tag_value))

    def get_value(self, item_id, path):
        result = self.perform_request("GET", '/' + item_id + '/value',
                                      headers={'Accept': 'application/json'},
                                      params={'path': path})
        return json.loads(result)

    def set_value(self, item_id, path, value, final=False):
        self.perform_request("PUT", '/' + item_id + '/value',
                             headers={'Content-Type': 'application/json'},
                             body=json.dumps(value),
                             params={'path': path, 'final': final})

    def add_event(self, item_id, event):
        self.perform_request("POST", '/' + item_id + '/event',
                             headers={'Content-Type': 'application/json'},
                             body=json.dumps(event))

    def add_simple_event(self, item_id, event_title, event_body):
        self.perform_request("POST", '/' + item_id + '/event/' + event_title,
                             headers={'Content-Type': 'application/json'},
                             body=json.dumps(event_body))

    def get_templates(self):
        result = self.perform_request("GET", '/templates', headers={'Accept': 'application/json'})
        return json.loads(result)


class CPMSClientBootstrapped(CPMSClient):
    """CPMS Client with configuration from bootstrapper."""
    def __init__(self):
        from cpms_config import CPMSConfiguration
        cpms_config = CPMSConfiguration()
        super(CPMSClientBootstrapped, self).__init__(
            "%s:%s" % (cpms_config.hostname, cpms_config.port),  # pylint: disable=no-member
            cpms_config.username,  # pylint: disable=no-member
            cpms_config.password)  # pylint: disable=no-member


def main():
    import time
    cpms = CPMSClient('localhost:8000', 'cpms-admin', 'SmartGarden')
    print("Environment: {}".format(cpms.get_environment()))
    print("Version: {}".format(cpms.get_version()))
    batch_id = cpms.create_batch('e747ff68-23f2-4d57-8558-d5974d47dddc',
                                 10,
                                 [
                                     {'name': 'productionYear', 'value': '2018'},
                                     {'name': 'productionWeek', 'value': '52'},
                                     {'name': 'productionDay', 'value': '3'}
                                 ])
    print("Created batch: {}".format(batch_id))
    time.sleep(30) # needed for backend to finish indexing - will be fixed soon
    cpms.deploy_batch(batch_id)
    print("Info: {}".format(cpms.get_batch_info()))
    item = cpms.get_next_free_item(batch_id)
    print("Next free item: {}".format(item))
    print("PNC: {}".format(cpms.get_value(item['ipr_id'], '/article_number')))
    print("Serial from configuration: {}".format(cpms.get_value(item['ipr_id'], '/configuration/serial')))
    print("Keys info: {}".format(cpms.get_keys(item['ipr_id'])))
    print("Software info: {}".format(cpms.get_software_info(item['ipr_id'])))
    with open('firmware.zip', 'wb') as fw_file:
        fw_file.write(cpms.get_file_by_name_and_version(item['ipr_id'], 'ble_spi-softdevice', 'configured', 'hex'))
    cpms.delete_batch(batch_id, True)

if __name__ == "__main__":
    main()
