# Note: this package was previously named `nngforward`
DESCRIPTION = "IPC Domain Socket Forwarder"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = " \
"

SRC_URI += " \
    file://ipcforward-lwm2mserver.service \
    file://ipcforward-lemonbeatd.service \
    file://ipcforward.sh \
"

SRCREV = "${AUTOREV}"
PR = "r0"

do_install:append() {
    # copy systemd files
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/ipcforward-lwm2mserver.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/ipcforward-lemonbeatd.service ${D}${systemd_unitdir}/system

    # copy forwarder script
    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/ipcforward.sh ${D}${bindir}/ipcforward
}

RDEPENDS:${PN} += " \
    socat \
"
PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = " \
    ipcforward-lwm2mserver.service \
    ipcforward-lemonbeatd.service \
"
