DESCRIPTION = "Reset Radio Module service"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PV = "1.0"

PR = "r0"

SRC_URI = "\
    file://reset-rm.py \
    file://reset-rm.service \
    file://reset-rm.cfg \
"

S = "${WORKDIR}/"

RDEPENDS_${PN} += " \
    python3 \
    libgpiod-python \
"

do_install() {
    install -d ${D}${bindir}
    install -m 755 ${S}reset-rm.py ${D}${bindir}/reset-rm

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/reset-rm.service ${D}${systemd_unitdir}/system

    install -d ${D}${sysconfdir}
    install -m 0644 ${WORKDIR}/reset-rm.cfg ${D}${sysconfdir}/reset-rm.cfg
}

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE_${PN} = "reset-rm.service"
