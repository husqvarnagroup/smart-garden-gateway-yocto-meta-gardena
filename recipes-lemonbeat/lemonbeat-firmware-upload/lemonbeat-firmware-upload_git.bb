DESCRIPTION = "Upload script for FOTA device update."
LICENSE = "CLOSED"

RDEPENDS_${PN} += " \
    lsdl-serializer-lib \
    python3-crcmod \
    python3-lemonbeat \
    python3-multiprocessing \
"

SRC_URI += " \
    git://stash.dss.husqvarnagroup.com/scm/sg/smart-garden-lemonbeat-tools.git;protocol=https \
"

PR = "r0"
SRCREV = "193af22bc23b2b8c9f9c9a64f41adcd1b6021577"
PV = "2019-03-15-2+git${SRCPV}"

S = "${WORKDIR}/git"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${S}/otau/upload.py ${D}${bindir}/upload
}