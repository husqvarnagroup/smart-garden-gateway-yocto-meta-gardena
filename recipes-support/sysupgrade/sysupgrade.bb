SUMMARY = "Manage data migration on system upgrade"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

FILESEXTRAPATHS_append := "${THISDIR}/files:"

inherit systemd allarch

SRC_URI = " \
    file://sysupgrade.service \
    file://sysupgrade.sh \
"

PR = "r0"
PV = "0.1"

do_install () {
    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/sysupgrade.sh ${D}${bindir}/sysupgrade

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/sysupgrade.service ${D}${systemd_unitdir}/system/
}

SYSTEMD_SERVICE_${PN} += "sysupgrade.service"
