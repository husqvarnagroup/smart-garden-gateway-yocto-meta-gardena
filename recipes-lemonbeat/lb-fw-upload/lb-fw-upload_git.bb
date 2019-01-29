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
PV = "1.0+git${SRCPV}"
SRCREV = "6d8b9d67f8c2871b1beff9591299655099"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${S}/otau/upload.py ${D}${bindir}/upload
}
