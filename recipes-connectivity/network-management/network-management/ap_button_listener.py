#!/usr/bin/env python3
"""Process starting AP on button press."""

from evdev import InputDevice, ecodes, events
import subprocess


def start_ap():
    command = "/usr/bin/network_management start_ap"
    print("Button pressed. Executing: {}".format(command))
    res = subprocess.run(command, shell=True)
    if res.returncode != 0:
        print("Command exited with return code {}.".format(res.returncode))


def flush(device):
    """Continuously read and discard events until None is read."""
    while device.read_one() is not None:
        pass


DEV = InputDevice('/dev/input/event0')

flush(DEV)
for event in DEV.read_loop():
    if event.type == ecodes.EV_KEY and \
            event.code == ecodes.KEY_PROG1 and \
            event.value == events.KeyEvent.key_up:
        start_ap()
        flush(DEV)
