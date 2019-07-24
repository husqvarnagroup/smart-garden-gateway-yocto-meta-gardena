#!/usr/bin/env python3

import gpiod
import sys
import time
import configparser

config = configparser.ConfigParser()
config.read('/etc/reset-rm.cfg')
pin = config.get('config', 'pin')

line = gpiod.find_line(pin)
if not line:
    raise Exception('line %s not found' % (pin))
line.request(consumer=sys.argv[0], type=gpiod.LINE_REQ_DIR_OUT)

line.set_value(0)
time.sleep(0.1)
line.set_value(1)

line.release()
