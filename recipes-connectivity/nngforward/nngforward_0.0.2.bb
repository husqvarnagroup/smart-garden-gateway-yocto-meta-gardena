DESCRIPTION = "NNG Socket Forwarder"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = " \
"

SRC_URI += " \
    file://nngforward-lwm2mserver.service \
    file://nngforward-lemonbeatd.service \
    file://nngforward.sh \
"

SRCREV = "${AUTOREV}"
PR = "r0"

do_install_append() {
    # copy systemd files
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/nngforward-lwm2mserver.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/nngforward-lemonbeatd.service ${D}${systemd_unitdir}/system

    # copy forwarder script
    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/nngforward.sh ${D}${bindir}/nngforward
}

RDEPENDS_${PN} += " \
    socat \
"
PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE_${PN} = " \
    nngforward-lwm2mserver.service \
    nngforward-lemonbeatd.service \
"
