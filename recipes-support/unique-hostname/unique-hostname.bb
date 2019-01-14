DESCRIPTION = "Set up unique hostname"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PV = "1.0"

PR = "r0"

SRC_URI = "\
    file://unique-hostname.sh \
    file://unique-hostname.service \
"

S = "${WORKDIR}/"

do_install() {
    install -d ${D}${bindir}
    install -m 755 ${S}unique-hostname.sh ${D}${bindir}/unique-hostname

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/unique-hostname.service ${D}${systemd_unitdir}/system
}

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE_${PN} = "unique-hostname.service"
