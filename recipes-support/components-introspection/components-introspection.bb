DESCRIPTION = "Helper scripts to determine the used hardware"
MAINTAINER = "Gardena GmbH"
HOMEPAGE = "https://www.gardena.com/"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28"

inherit systemd

PV = "0.1.1"
PR = "r0"

SRC_URI = " \
    file://update-hw-revision.sh \
    file://update-hw-revision.service \
    file://update-sw-versions.sh \
    file://update-sw-versions.service \
"

DEFAULT_BOARD_NAME = "unknown"
DEFAULT_BOARD_NAME_mt7688 = "smart-gateway-mt7688"
DEFAULT_BOARD_NAME_at91sam9x5 = "smart-gateway-at91sam"

do_install_append () {
    install -d ${D}${bindir}
    install -d ${D}${systemd_unitdir}/system

    install -m 644 ${WORKDIR}/update-hw-revision.service ${D}${systemd_unitdir}/system
    install -m 755 ${WORKDIR}/update-hw-revision.sh ${D}${bindir}/update-hw-revision
    sed -i -e 's,@DEFAULT_BOARD_NAME@,${DEFAULT_BOARD_NAME},g' \
               ${D}${bindir}/update-hw-revision

    install -m 644 ${WORKDIR}/update-sw-versions.service ${D}${systemd_unitdir}/system
    install -m 755 ${WORKDIR}/update-sw-versions.sh ${D}${bindir}/update-sw-versions
}

SYSTEMD_SERVICE_${PN} = "update-hw-revision.service update-sw-versions.service"
