DESCRIPTION = "Gateway Snapshot Script"
MAINTAINER = "Gardena GmbH"
HOMEPAGE = "https://www.gardena.com/"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28"

PV = "1.3"
PR = "r0"

SRC_URI = "\
    file://snapshot.sh \
"

S = "${WORKDIR}/"

do_install() {
    install -d ${D}${sbindir}
    install -m 755 ${S}snapshot.sh ${D}${sbindir}/snapshot
}
