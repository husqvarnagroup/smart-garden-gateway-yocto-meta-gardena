#!/bin/sh
#List current devices on the gateway
#Use -i to list only included devices

SHADOWAY_DIR=/var/shadoway/work

print_info() {
    echo "[$NUMBER]"
    echo "Name: $NAME"
    echo "Address: $IPV6"
    echo "Wakeup channel: $WAKEUP_CHANNEL"
    echo "Included: $INCLUDED"
    echo "SGTIN: $SGTIN"
    echo "HW version: $HW_VER"
    echo "Bootloader version: $BOOT_VER"
    echo "Stack version: $STACK_VER"
    echo "Application version: $APP_VER"
    echo
    NUMBER=$((NUMBER+1))
}

NUMBER=1

for device in "$SHADOWAY_DIR"/Device_* ; do
    NAME=$(awk '/name/ {$1=$2=""; print $0}' "$device"/*.json | awk -F '"' '{print $2}')
    HW_VER=$(awk '/version_hw/ {print $3}' "$device"/*.json | cut -d '"' -f2)
    BOOT_VER=$(awk '/version_boot/ {print $3}' "$device"/*.json | cut -d '"' -f2)
    STACK_VER=$(awk '/version_stack/ {print $3}' "$device"/*.json | cut -d '"' -f2)
    APP_VER=$(awk '/version_app/ {print $3}' "$device"/*.json | cut -d '"' -f2)
    IPV6=$(awk '/address/ {print $3}' "$device"/*.json | cut -d '"' -f2)
    SGTIN=$(awk '/serialid/ {print $3}' "$device"/*.json | cut -d '"' -f2)
    INCLUDED=$(awk '/included/ {print $3}' "$device"/*.json | tr -d ,)
    WAKEUP_CHANNEL=$(awk '/wakeup_channel/ {print $3}' "$device"/*.json | tr -d ,)

    if [ "$1" = "-i" ]; then
        if [ "$INCLUDED" = "true" ]; then
            print_info
        fi
    else
        print_info
    fi

done
