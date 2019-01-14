#!/bin/sh

# Activity LED
cd /sys/class/leds/smartgw:eth:act/ || exit
echo netdev > trigger
echo eth0 > device_name
echo 1 > rx
echo 1 > tx

# Link LED
cd /sys/class/leds/smartgw:eth:link/ || exit
echo netdev > trigger
echo eth0 > device_name
echo 1 > "link"
