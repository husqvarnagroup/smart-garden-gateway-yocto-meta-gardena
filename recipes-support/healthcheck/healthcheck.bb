DESCRIPTION = "Checking for known and potential problems"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PV = "0.27.0"
PR = "r0"

RDEPENDS:${PN} = "curl openssl systemd"

SRC_URI = "\
    file://healthcheck.service \
    file://healthcheck.sh \
    file://healthcheck.timer \
"

S = "${WORKDIR}"

UPDATE_URL_PROTOCOLLESS = "${@DISTRO_UPDATE_URL_BASE.split('://', 1)[1]}/gardena-update-image-bnw-${MACHINE}.swu"

do_install() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/healthcheck.service ${D}${systemd_unitdir}/system/
    install -m 0644 ${WORKDIR}/healthcheck.timer ${D}${systemd_unitdir}/system/

    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/healthcheck.sh ${D}${bindir}/healthcheck
    sed -i 's#@UPDATE_URL_PROTOCOLLESS@#${UPDATE_URL_PROTOCOLLESS}#' ${D}${bindir}/healthcheck
}

FILES:${PN} += " \
    ${systemd_unitdir}/system \
"

inherit systemd allarch
SYSTEMD_SERVICE:${PN} = "${PN}.timer"
