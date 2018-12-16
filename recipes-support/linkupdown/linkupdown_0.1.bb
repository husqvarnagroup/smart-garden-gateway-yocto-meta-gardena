SUMMARY = "Very simple hack to work around our (currently) non-working link detection"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "\
    file://linkupdown.service \
    file://linkupdown.sh \
"

inherit systemd allarch

PR = "r2"

FILES_${PN} += " \
    ${systemd_unitdir}/system/linkupdown.service \
"

do_install () {
  install -d ${D}${bindir}
  install -m 0755 ${WORKDIR}/linkupdown.sh ${D}${bindir}/linkupdown

  install -d ${D}${systemd_unitdir}/system
  install -m 644 ${WORKDIR}/linkupdown.service ${D}${systemd_unitdir}/system
}

DEPENDS += "systemd swconfig"
SYSTEMD_SERVICE_${PN} += "linkupdown.service"
