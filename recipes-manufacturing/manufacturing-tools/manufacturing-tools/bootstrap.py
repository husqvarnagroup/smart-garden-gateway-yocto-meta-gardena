#!/usr/bin/python3
# coding: utf-8
#
# Author: Andreas Müller <andreas.mueller@husqvarnagroup.com>
# Date: 2018-11-14
# Version: 0.1
#
# Copyright (c) 2018 Gardena GmbH

"""
@package bootstrap
Bootstrap server interaction.
"""

import http.client
import urllib
from http import HTTPStatus

BOOTSTRAP_SERVER_HOST = "10.42.0.1"
BOOTSTRAP_SERVER_PORT = 80
BOOTSTRAP_SERVER_CPMS_CONFIG_PATH = "/configuration/cpms.json"
BOOTSTRAP_SERVER_BATCH_CONFIG_PATH = "/configuration/batch.json"
BOOTSTRAP_SERVER_HOMEKIT_TOKEN_GENERATOR_PATH = "/tokens/ahk.sh"


def bootstrap_server_get(path):
    """HTTP GET request to bootstrap server."""
    connection = http.client.HTTPConnection(BOOTSTRAP_SERVER_HOST, BOOTSTRAP_SERVER_PORT, timeout=15)
    connection.request("GET", path)
    response = connection.getresponse()

    if response.status != HTTPStatus.OK:
        raise http.client.HTTPException(response.status, response.reason, response.read())

    body = response.read()
    connection.close()

    return body


def bootstrap_server_put(path, data):
    """HTTP PUT request to bootstrap server."""
    connection = http.client.HTTPConnection(BOOTSTRAP_SERVER_HOST, BOOTSTRAP_SERVER_PORT, timeout=30)
    connection.request("PUT", path, urllib.parse.urlencode(data), {'Content-Type': 'application/x-www-form-urlencoded'})
    response = connection.getresponse()

    if response.status != HTTPStatus.OK and response.status != HTTPStatus.CREATED:
        raise http.client.HTTPException(response.status, response.reason, response.read())

    body = response.read()
    connection.close()

    return body


def bootstrap_get_cpms_config():
    """Get CPMS configuration."""
    return bootstrap_server_get(BOOTSTRAP_SERVER_CPMS_CONFIG_PATH)


def bootstrap_get_batch():
    """Get batch ID."""
    return bootstrap_server_get(BOOTSTRAP_SERVER_BATCH_CONFIG_PATH)

def bootstrap_get_homekit_tokens():
    """Get HomeKit tokens from bootstrap server."""
    return bootstrap_server_get(BOOTSTRAP_SERVER_HOMEKIT_TOKEN_GENERATOR_PATH)
