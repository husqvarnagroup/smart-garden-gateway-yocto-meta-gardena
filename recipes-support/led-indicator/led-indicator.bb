DESCRIPTION = "LED indicator"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PV = "1.3"

PR = "r0"

SRC_URI = "\
    file://led-control.c \
    file://internet-led \
    file://internet-led.service \
    file://stop-power-led.service \
"

S = "${WORKDIR}/"

FILES_${PN} = " \
    ${bindir}/led-indicator \
    ${libdir}/seluxit/scripts/ \
    ${systemd_unitdir}/system/ \
"

do_compile() {
    ${CC} ${WORKDIR}/led-control.c -o led-indicator
}

do_install() {
    install -d ${D}${bindir}
    install -m 755 ${S}led-indicator ${D}${bindir}

    install -m 700 -d ${D}${libdir}/seluxit/scripts
    install -m 700 ${S}internet-led ${D}${libdir}/seluxit/scripts

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/stop-power-led.service ${D}${systemd_unitdir}/system/
    install -m 0644 ${WORKDIR}/internet-led.service ${D}${systemd_unitdir}/system/
}

INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
INHIBIT_PACKAGE_STRIP = "1"
PACKAGES = "${PN}"

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE_${PN} = "stop-power-led.service internet-led.service"
SYSTEMD_AUTO_ENABLE = "enable"
