#!/bin/sh
#
# This script extracts the board name and hardware version from U-Boot and
# saves those values in order to allow software to determine the hardware it is
# running on.

if ! board_name="$(fw_printenv -n board_name 2>/dev/null)"; then
    echo "Failed to extract the board name from the U-Boot environment!" >&2
    board_name=@DEFAULT_BOARD_NAME@
fi

if ! board_revision="$(fw_printenv -n gateway_hardware_revision 2>/dev/null)"; then
    echo "Failed to extract the board revision from the U-Boot environment!" >&2
    board_revision="unknown"
fi

echo "${board_name} ${board_revision}" > /tmp/hw-revision

# Quit if /etc/hw-revision is already up-to-date
if cmp /tmp/hw-revision /etc/hw-revision; then
  rm /tmp/hw-revision
  exit
fi

# Note: This code does not need to be power cuts save as it runs during on
# every startup anyway.
mv /tmp/hw-revision /etc/hw-revision
