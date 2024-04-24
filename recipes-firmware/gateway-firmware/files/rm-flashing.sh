#!/bin/sh
# shellcheck shell=dash
#
# Check if RM has latest firmware and flash if needed.

set -eu -o pipefail

rm_firmware_version_file=/etc/rm-firmware-version
rm_firmware_version_file_tmp=/etc/rm-firmware-version.tmp
latest_rm_firmware_version_file=/etc/rm-firmware-version.latest

if [ ! -e $rm_firmware_version_file ] || [ ! -e $latest_rm_firmware_version_file ] \
   || [ "$(cat $rm_firmware_version_file)" != "$(cat $latest_rm_firmware_version_file)" ]; then
    /usr/bin/rm-flashtool --flash-rm-firmware
    cp $latest_rm_firmware_version_file $rm_firmware_version_file_tmp
    sync
    mv $rm_firmware_version_file_tmp $rm_firmware_version_file
fi
