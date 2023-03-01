DESCRIPTION = "Checking for known and potential problems"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PV = "0.19.0"
PR = "r0"

RDEPENDS_${PN} = "curl openssl systemd"

SRC_URI = "\
    file://healthcheck.service \
    file://healthcheck.sh \
    file://healthcheck.timer \
"

S = "${WORKDIR}/"

do_install() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/healthcheck.service ${D}${systemd_unitdir}/system/
    install -m 0644 ${WORKDIR}/healthcheck.timer ${D}${systemd_unitdir}/system/

    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/healthcheck.sh ${D}${bindir}/healthcheck
}

FILES_${PN} += " \
    ${systemd_unitdir}/system \
"

inherit systemd allarch
SYSTEMD_SERVICE_${PN} = "${PN}.timer"
