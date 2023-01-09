DESCRIPTION = "Gateway Snapshot Script"
MAINTAINER = "Gardena GmbH"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

RDEPENDS_${PN} += "\
    busybox \
    healthcheck \
"

PV = "2.0.0"
PR = "r0"

SRC_URI = "\
    file://snapshot.sh \
"

S = "${WORKDIR}/"

do_install() {
    install -d ${D}${sbindir}
    install -m 755 ${S}snapshot.sh ${D}${sbindir}/snapshot
}
