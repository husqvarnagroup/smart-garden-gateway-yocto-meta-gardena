DESCRIPTION = "Upload script for FOTA device update."
LICENSE = "CLOSED"

RDEPENDS_${PN} += " \
    lsdl-serializer-lib \
    python3-crcmod \
    python3-lemonbeat \
    python3-multiprocessing \
"

SRC_URI += " \
    git://stash.dss.husqvarnagroup.com:7999/sg/smart-garden-lemonbeat-tools.git;protocol=ssh \
"

PR = "r0"
SRCREV = "02a8ae245065e84d3d6928249be0059d0083923f"
PV = "2021-06-11+git${SRCPV}"

S = "${WORKDIR}/git"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${S}/otau/upload.py ${D}${bindir}/upload
}
