#!/bin/sh

# (Try to) ensure we have connectivity on our WLAN interface
/lib/systemd/systemd-networkd-wait-online -i wlan0 --timeout=300

wlan_down=0

if [ "$(cat /sys/class/net/wlan0/carrier)" -ne 1 ]; then
  echo wlan missing carrier
  wlan_down=1
fi

if [ "$(iw dev wlan0 scan | wc -l)" -le 50 ] && ! ping -c1 heise.de >/dev/null; then
  echo Scanning and pinging failed
  wlan_down=1
fi

# Reboot if WLAN is working
if [ "${wlan_down}" -eq 0 ]; then
  date >> /home/root/wlan0-date-good
  dmesg > "/home/root/wlan0-dmesg-good.$(date +%s)"
  echo WLAN STILL ALIVE!
  sync
  reboot
else
  date >> /home/root/wlan0-date-bad
  dmesg > /home/root/wlan0-dmesg-bad
  echo "${wlan_down}" >> /home/root/wlan0-reason-bad
  echo WLAN IS DEAD!
  echo 1 > "/sys/class/leds/smartgw:internet:blue/brightness"
  echo 0 > "/sys/class/leds/smartgw:internet:green/brightness"
  echo 0 > "/sys/class/leds/smartgw:internet:red/brightness"
fi
