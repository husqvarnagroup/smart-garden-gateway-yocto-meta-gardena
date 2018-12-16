#!/bin/sh

set -u -o pipefail

ETH_LED_LNK_GPIO=3
ETH_LED_LNK_GPIO_PATH=/sys/class/gpio/gpio${ETH_LED_LNK_GPIO}
STATE_PATH=/var/run/linkupdown/state

# Make sure the ETH_LED_LNK GPIO is exported and configured
if [ ! -d "${ETH_LED_LNK_GPIO_PATH}" ]; then
    echo ${ETH_LED_LNK_GPIO} > /sys/class/gpio/export;
    echo out > "${ETH_LED_LNK_GPIO_PATH}/direction"
fi

while true; do
  swconfig dev switch0 port 0 get link 2>&1 | grep -q link:up
  current_state=$?
  last_state=$(cat "${STATE_PATH}" 2>/dev/null)

  if [ "${current_state}" = "${last_state}" ]; then
    sleep 3
    continue
  fi

  echo "${current_state}" > "${STATE_PATH}"

  if [ "${current_state}" = "0" ]; then
    echo "Link up: eth0"
    dhcpcd -q -n eth0
    echo 0 > ${ETH_LED_LNK_GPIO_PATH}/value
  else
    echo "Link down: eth0"
    dhcpcd -q -x eth0
    echo 1 > ${ETH_LED_LNK_GPIO_PATH}/value
  fi
done
