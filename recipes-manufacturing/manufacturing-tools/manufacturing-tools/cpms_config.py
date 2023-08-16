#!/usr/bin/python3
# coding: utf-8
#
# Author: Andreas Müller <andreas.mueller@husqvarnagroup.com>
# Date: 2018-09-11
# Version: 0.1
#
# Copyright (c) 2018 Gardena GmbH

"""
@package cpms_config
CPMS configuration package.
"""

import json

from bootstrap import bootstrap_get_cpms_config

class CPMSConfiguration: # pylint: disable=too-few-public-methods
    """CPMS configuration class - fetches configuration from bootstrap server."""
    # bootstrapping server API config

    def __init__(self):
        """Initialize class and fetch configuration."""
        config_body = bootstrap_get_cpms_config()

        config = json.loads(config_body.decode("ascii"))
        for key in config:
            setattr(self, key, config[key])
