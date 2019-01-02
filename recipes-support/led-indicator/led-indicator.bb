DESCRIPTION = "Small led helper"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://LICENSE;md5=83621dfdfe7ffabe338547ea7957e56f"
MAINTAINER = "Seluxit ApS <info@seluxit.com>"
HOMEPAGE = "http://www.seluxit.com"

PV = "1.3"

PR = "r0"

SRC_URI = "\
    file://LICENSE \
    file://led-control.c \
    file://internet-led \
    file://internetLED.service \
    file://stopGreenLed.service \
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
    install -m 0644 ${WORKDIR}/stopGreenLed.service ${D}${systemd_unitdir}/system/
    install -m 0644 ${WORKDIR}/internetLED.service ${D}${systemd_unitdir}/system/
}

INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
INHIBIT_PACKAGE_STRIP = "1"
PACKAGES = "${PN}"

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE_${PN} = "stopGreenLed.service internetLED.service"
SYSTEMD_AUTO_ENABLE = "enable"
