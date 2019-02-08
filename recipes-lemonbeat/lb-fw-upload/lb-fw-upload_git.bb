DESCRIPTION = "Upload script for FOTA device update."
LICENSE = "CLOSED"

RDEPENDS_${PN} += " \
    lsdl-serializer-lib \
    python3-lemonbeat \
    python3-multiprocessing \
    python3-crc16 \
"

SRC_URI += " \
    git://stash.dss.husqvarnagroup.com/scm/sg/smart-garden-lemonbeat-tools.git;protocol=https \
"

PR = "r2"
SRCREV = "8438131ca2a7b30df2b1450cbb56a759f95fe28f"
PV = "1.0+git${SRCPV}"

S = "${WORKDIR}/git"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${S}/otau/upload.py ${D}${bindir}/upload
}
