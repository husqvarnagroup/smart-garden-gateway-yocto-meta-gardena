#!/bin/sh
#
# Turn the LED indicator on or off.
#
# USAGE:
#
#   led-indicator <name> <on|off|flash>
#

LED_FNAME=/sys/class/leds/$1

led_on () {
	[ -z "$1" ] || LED_FNAME=/sys/class/leds/$1

	echo "oneshot" > "${LED_FNAME}/trigger"
	echo 1 > "${LED_FNAME}/brightness"
}

led_off () {
	[ -z "$1" ] || LED_FNAME=/sys/class/leds/$1

	echo "oneshot" > "${LED_FNAME}/trigger"
	echo 0 > "${LED_FNAME}/brightness"
}

led_flash () {
	[ -z "$1" ] || LED_FNAME=/sys/class/leds/$1

	echo "timer" > "${LED_FNAME}/trigger"
	#echo "300" > ${LED_FNAME}/delay_on
	#echo "500" > ${LED_FNAME}/delay_off
}

flash() {
    echo "$2" > "/sys/class/leds/$1/delay_off"
    echo "$2" > "/sys/class/leds/$1/delay_on"
}

identify () {
    time0=250
    time1=200
    time2=141
    time3=100
    time4=71
    time5=50

    led_flash smartgw:internet:green &
    led_flash smartgw:internet:red

    flash smartgw:internet:green $time1 &
    flash smartgw:internet:red $time1
    /bin/usleep $((time0 + 4 * time1 * 1000))

    flash smartgw:internet:green $time2 &
    flash smartgw:internet:red $time2
    /bin/usleep $((time0 + 4 * time1 * 1000))

    flash smartgw:internet:green $time3 &
    flash smartgw:internet:red $time3
    /bin/usleep $((time0 + 4 * (time1 + time2) * 1000))

    flash smartgw:internet:green $time4 &
    flash smartgw:internet:red $time4
    /bin/usleep $((time0 + 4 * (time1 + time2 + time3) * 1000))

    flash smartgw:internet:green $time5 &
    flash smartgw:internet:red $time5
    /bin/usleep $((time0 + 4 * (time1 + time2 + time3 + time4) * 1000))

    led_off smartgw:internet:red &
    led_on smartgw:internet:green
}

INDICATOR_STATUS="$2"
if [ "z${INDICATOR_STATUS}" = "zon" ]; then
	led_on
	exit 0
elif [ "z${INDICATOR_STATUS}" = "zoff" ]; then
	led_off
	exit 0
elif [ "z${INDICATOR_STATUS}" = "zflash" ]; then
	led_flash
	exit 0
elif [ "z${INDICATOR_STATUS}" = "zidentify" ]; then
	identify &
	exit 0
else
	echo "USAGE: $(basename "$0") <LED name> <on|off|flash>"
	exit 1
fi

