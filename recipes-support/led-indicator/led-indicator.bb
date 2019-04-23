DESCRIPTION = "LED indicator"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PV = "1.4"

PR = "r0"

DEPENDS = "shadoway"

SRC_URI = "\
    file://internet-ledd.sh \
    file://led-indicator.c \
    file://led-indicatorc.sh \
    file://rf-led-setup.sh \
    file://internet-led.service \
    file://rf-led.service \
    file://power-ledd.sh \
    file://power-led.service \
    file://ethernet-leds.service \
    file://ethernet-led-setup.sh \
"

S = "${WORKDIR}/"

do_compile() {
    ${CC} ${WORKDIR}/led-indicator.c -o led-indicator -Wall -Wextra -Wpedantic -Werror
}

do_install() {
    install -d ${D}${bindir}
    install -m 755 ${S}led-indicatorc.sh ${D}${bindir}/led-indicatorc
    install -m 755 ${S}internet-ledd.sh ${D}${bindir}/internet-ledd
    install -m 755 ${S}power-ledd.sh ${D}${bindir}/power-ledd
    install -m 755 ${WORKDIR}/led-indicator ${D}${bindir}/
    install -m 755 ${S}rf-led-setup.sh ${D}${bindir}/rf-led-setup
    install -m 755 ${S}ethernet-led-setup.sh ${D}${bindir}/ethernet-led-setup

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/power-led.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/internet-led.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/rf-led.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/ethernet-leds.service ${D}${systemd_unitdir}/system
}

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE_${PN} = "power-led.service internet-led.service rf-led.service ethernet-leds.service"
