DESCRIPTION = "Upload script for FOTA device update."
LICENSE = "CLOSED"

RDEPENDS_${PN} += " \
    lsdl-serializer-lib \
    python3-lemonbeat \
    python3-multiprocessing \
    python3-crc16 \
"

SRC_URI += " \
    git://git@stash.dss.husqvarnagroup.com/scm/sg/smart-garden-lemonbeat-tools.git;protocol=ssh \
"

PR = "r0"
SRCREV = "bd0696e1c0ef723da69b9e1c80e675d43ee93f62"
PV = "1.0+git${SRCPV}"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${S}/otau/upload.py ${D}${bindir}/upload
}
