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
SRCREV = "1ba71bb48f5e316f1c7ff871e6555879ef6b9f64"
PV = "2020-12-01+git${SRCPV}"

S = "${WORKDIR}/git"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${S}/otau/upload.py ${D}${bindir}/upload
}
