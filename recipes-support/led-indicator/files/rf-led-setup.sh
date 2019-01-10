#!/bin/sh

LED=/sys/class/leds/smartgw:radio:

# set up LED:
# - green on RX
# - yellow on TX

for color in blue green red; do
  echo netdev > ${LED}${color}/trigger
  echo ppp0 > ${LED}${color}/device_name
done
echo 1 > ${LED}green/rx
echo 1 > ${LED}green/tx
echo 1 > ${LED}red/tx
