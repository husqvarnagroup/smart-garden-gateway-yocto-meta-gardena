DESCRIPTION = "Collecting metrics to improve system"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PV = "0.2.0"
PR = "r0"

RDEPENDS:${PN} = "mdns systemd"

SRC_URI = "\
    file://metrics.service \
    file://metrics.sh \
    file://metrics.timer \
"

S = "${WORKDIR}"

do_install() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/metrics.service ${D}${systemd_unitdir}/system/
    install -m 0644 ${WORKDIR}/metrics.timer ${D}${systemd_unitdir}/system/

    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/metrics.sh ${D}${bindir}/metrics
}

FILES:${PN} += " \
    ${systemd_unitdir}/system \
"

inherit systemd allarch
SYSTEMD_SERVICE:${PN} = "${PN}.timer"
