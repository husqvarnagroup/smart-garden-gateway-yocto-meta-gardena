DESCRIPTION = "Set up unique hostname"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PV = "1.1.1"

PR = "r0"

SRC_URI = "\
    file://unique-hostname.sh \
    file://unique-hostname.service \
    file://keep.d/unique-hostname \
"

S = "${WORKDIR}"

FILES:${PN} += " \
    ${base_libdir}/upgrade/keep.d \
"

do_install() {
    install -d ${D}${bindir}
    install -m 755 ${S}/unique-hostname.sh ${D}${bindir}/unique-hostname

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/unique-hostname.service ${D}${systemd_unitdir}/system

    install -d ${D}${base_libdir}/upgrade/keep.d
    install -m 0644 ${WORKDIR}/keep.d/unique-hostname ${D}${base_libdir}/upgrade/keep.d
}

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = "unique-hostname.service"
