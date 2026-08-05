#!/usr/bin/env sh
#
# Copyright (c) 2021 GARDENA GmbH
#

OPTIND=1

show_help() {
    echo "Usage: $0 [-hq] service_name base_port [eventbus_location]"
}

QUIET=false

while getopts "h?q" opt; do
    case "$opt" in
        h|\?)
            show_help
            exit 0
            ;;
        q)
            QUIET=true
            ;;
    esac
done

shift $((OPTIND-1))

[ "${1:-}" = "--" ] && shift

if [ -z "$1" ] ; then
    echo 'Must provide service name'
    exit 1
fi

if [ -z "$2" ] ; then
    echo 'Must provide base port'
    exit 1
fi

SERVICE_NAME="$1"
EVENT_TCP_PORT=$2
COMMAND_TCP_PORT=$((EVENT_TCP_PORT+1))

if [ -z "$3" ]; then
  EVENTBUS_LOCATION="/tmp"
else
  EVENTBUS_LOCATION="$3"
fi

echo "Starting socat commands"

# open firewall ports
FIREWALL_COMMENT="ipcforward-${SERVICE_NAME}"
if nft list chain inet filter input >/dev/null 2>&1; then
  nft insert rule inet filter input tcp dport "{ $EVENT_TCP_PORT, $COMMAND_TCP_PORT }" \
    accept comment "\"$FIREWALL_COMMENT\""
else
  echo "No firewall installed, not opening any ports."
fi

# run socat in the background
socat "TCP-LISTEN:$EVENT_TCP_PORT,reuseaddr,fork" "UNIX-CONNECT:${EVENTBUS_LOCATION}/${SERVICE_NAME}-event.ipc" & EVENT_PID=$!
socat "TCP-LISTEN:$COMMAND_TCP_PORT,reuseaddr,fork" "UNIX-CONNECT:${EVENTBUS_LOCATION}/${SERVICE_NAME}-command.ipc" & COMMAND_PID=$!

# install signal trap
trap 'kill $EVENT_PID; kill $COMMAND_PID' INT TERM

[ "$QUIET" = false ] && echo "Socat commands started. Use the following commands on your client:"
[ "$QUIET" = false ] && echo "==="
IP=$(ip -o route get to 1.0.0.0 | sed -n 's/.*src \([0-9.]\+\).*/\1/p')
[ "$QUIET" = false ] && echo "socat UNIX-LISTEN:${EVENTBUS_LOCATION}/${SERVICE_NAME}-event.ipc,fork,reuseaddr,unlink-early TCP:$IP:$EVENT_TCP_PORT"
[ "$QUIET" = false ] && echo "socat UNIX-LISTEN:${EVENTBUS_LOCATION}/${SERVICE_NAME}-command.ipc,fork,reuseaddr,unlink-early TCP:$IP:$COMMAND_TCP_PORT"
[ "$QUIET" = false ] && echo "==="


# wait for socat commands in the background
wait $EVENT_PID
wait $COMMAND_PID

# close firewall ports, the rule can only be deleted by its handle
HANDLE=$(nft --handle list chain inet filter input 2>/dev/null \
  | sed -n "s/.*comment \"$FIREWALL_COMMENT\" # handle \([0-9]\+\)/\1/p")
if [ -n "$HANDLE" ]; then
  nft delete rule inet filter input handle "$HANDLE"
fi

echo "Socat commands stopped."
