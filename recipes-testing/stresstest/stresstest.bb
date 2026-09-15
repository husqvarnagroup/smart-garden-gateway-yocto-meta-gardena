DESCRIPTION = "Generate traffic on ifup using Tele2 servers (http://speedtest.tele2.net/)"
LICENSE = "GPL-2.0-or-later"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0-or-later;md5=fed54355545ffd980b814dab4a3b312c"

PV = "0.1"

PR = "r2"

RDEPENDS:${PN}-upload = "curl systemd-extra-utils"
RDEPENDS:${PN}-download = "curl systemd-extra-utils"

SRC_URI = "\
    file://50-stresstest-download.sh \
    file://50-stresstest-upload.sh \
    file://stresstest-download.sh \
    file://stresstest-upload.sh \
"

PACKAGES = "${PN}-upload ${PN}-download"

S = "${UNPACKDIR}"

inherit allarch

do_install() {
    # Upload files
    install -d ${D}${bindir}/
    install -m 0755 ${UNPACKDIR}/stresstest-upload.sh ${D}${bindir}/stresstest-upload

    install -d ${D}${libexecdir}/dhcpcd-hooks
    install -m 0644 ${UNPACKDIR}/50-stresstest-upload.sh ${D}${libexecdir}/dhcpcd-hooks/50-stresstest-upload

    # Download files
    install -d ${D}${bindir}/
    install -m 0755 ${UNPACKDIR}/stresstest-download.sh ${D}${bindir}/stresstest-download

    install -d ${D}${libexecdir}/dhcpcd-hooks
    install -m 0644 ${UNPACKDIR}/50-stresstest-download.sh ${D}${libexecdir}/dhcpcd-hooks/50-stresstest-download
}

FILES:${PN}-upload = "\
    ${bindir}/stresstest-upload \
    ${libexecdir}/dhcpcd-hooks/50-stresstest-upload \
"

FILES:${PN}-download = "\
    ${bindir}/stresstest-download \
    ${libexecdir}/dhcpcd-hooks/50-stresstest-download \
"
