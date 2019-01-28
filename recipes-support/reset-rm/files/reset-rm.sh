#!/bin/sh

set -eu -o pipefail

GPIO_DIR="/sys/class/gpio"
RM_SWD_RESET_PIN="37"

echo $RM_SWD_RESET_PIN > "${GPIO_DIR}/export"
echo "out" > "${GPIO_DIR}/gpio${RM_SWD_RESET_PIN}/direction"
echo "0" > "${GPIO_DIR}/gpio${RM_SWD_RESET_PIN}/value"
sleep 0.1
echo "1" > "${GPIO_DIR}/gpio${RM_SWD_RESET_PIN}/value"
echo $RM_SWD_RESET_PIN > "${GPIO_DIR}/unexport"
