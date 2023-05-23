SUMMARY = "Do the migration"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit systemd allarch

SRC_URI += " \
    file://eoltest-check.service \
    file://manufacturing-statusfiles.service \
    file://manufacturing-statusfiles.sh \
    file://keep.d/eoltest \
    file://keep.d/manufacturing-statusfiles \
    "

PV = "2019-10-22"
PR = "r0"

FILES:${PN} += " \
    ${base_libdir}/upgrade/keep.d \
"

RDEPENDS:${PN} += " \
    migration \
"

do_install () {
    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/manufacturing-statusfiles.sh ${D}${bindir}/manufacturing-statusfiles

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/eoltest-check.service ${D}${systemd_unitdir}/system/
    install -m 0644 ${WORKDIR}/manufacturing-statusfiles.service ${D}${systemd_unitdir}/system/

    install -d ${D}${base_libdir}/upgrade/keep.d
    install -m 0644 ${WORKDIR}/keep.d/eoltest ${D}${base_libdir}/upgrade/keep.d
    install -m 0644 ${WORKDIR}/keep.d/manufacturing-statusfiles ${D}${base_libdir}/upgrade/keep.d
}

SYSTEMD_SERVICE:${PN} += "manufacturing-statusfiles.service"
SYSTEMD_SERVICE:${PN} += "eoltest-check.service"
