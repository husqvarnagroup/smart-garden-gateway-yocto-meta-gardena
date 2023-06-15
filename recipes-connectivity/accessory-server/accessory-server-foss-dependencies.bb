SUMMARY = "Homekit Accessory Server FOSS dependencies"

inherit packagegroup

PR = "r0"

RDEPENDS:${PN} += " \
    dnsmasq \
    environment \
    hostapd \
    led-indicator \
    u-boot-fw-utils \
    wpa-supplicant \
    wpa-supplicant-cli \
"
