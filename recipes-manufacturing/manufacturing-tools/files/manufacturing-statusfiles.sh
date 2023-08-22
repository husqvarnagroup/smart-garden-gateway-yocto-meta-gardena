#!/bin/sh
#
# Create status files relevant for manufacturing based on U-Boot
# environment variables.
#
# This is started before other manufacturing services, so that the
# files can be recreated in time after a factory reset.

set -eu

for var in fct_finalized; do
    statusfile="/etc/$var"
    status="$(fw_printenv -n $var 2>/dev/null || echo 0)"
    if [ "${status}" = "1" ] || [ "$(uname -m)" = "armv5tejl" ]; then
        [ -f $statusfile ] || touch $statusfile
    fi
    # We never unset status variables, so trying to delete a status
    # file in case the U-Boot variable is missing is not necessary.
    # The only way to redo a step during manufacturing is to
    # completely start over ('run do_start_over' in U-Boot).
done

sync
