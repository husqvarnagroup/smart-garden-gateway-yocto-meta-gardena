DESCRIPTION = "LED indicator"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PV = "2.0.1"

PR = "r0"

SRC_URI = "\
    file://internet-ledd.sh \
    file://led-indicator.c \
    file://led-indicatorc.sh \
    file://internet-led.service \
    file://power-ledd.sh \
    file://power-led.service \
    file://rf-led.service \
    file://rf-led-setup.sh \
"
SRC_URI:append:mt7688 = " \
\
    file://ethernet-leds.service \
    file://ethernet-led-setup.sh \
"

S = "${WORKDIR}"

do_compile() {
    ${CC} ${CFLAGS} ${LDFLAGS} ${WORKDIR}/led-indicator.c -o led-indicator -Wall -Wextra -Wpedantic -Werror
}

do_install() {
    install -d ${D}${bindir}
    install -m 755 ${S}/led-indicatorc.sh ${D}${bindir}/led-indicatorc
    install -m 755 ${S}/internet-ledd.sh ${D}${bindir}/internet-ledd
    install -m 755 ${S}/power-ledd.sh ${D}${bindir}/power-ledd
    install -m 755 ${S}/rf-led-setup.sh ${D}${bindir}/rf-led-setup
    install -m 755 ${WORKDIR}/led-indicator ${D}${bindir}/

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/power-led.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/internet-led.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/rf-led.service ${D}${systemd_unitdir}/system
}

do_install:append:mt7688() {
    install -m 755 ${S}/ethernet-led-setup.sh ${D}${bindir}/ethernet-led-setup

    install -m 0644 ${WORKDIR}/ethernet-leds.service ${D}${systemd_unitdir}/system
}

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = "power-led.service internet-led.service rf-led.service"
SYSTEMD_SERVICE:${PN}:append:mt7688 = " ethernet-leds.service"
