DESCRIPTION = "Lemonbeat Python library"
LICENSE = "CLOSED"

inherit python3-dir

SRC_URI += " \
    git://stash.dss.husqvarnagroup.com/scm/sg/lemonbeat-python.git;protocol=https \
"

PR = "r0"
PV = "2019-04-05+git${SRCPV}"

SRCREV = "ec2f4c8cc74f6acb5b50830e444cb0525befc7e6"

S = "${WORKDIR}/git"

RDEPENDS_${PN} += " \
    lsdl-serializer-lib \
    python3-core \
    python3-fcntl \
    python3-threading \
    python3-xml \
"

do_install() {
    install -d ${D}${PYTHON_SITEPACKAGES_DIR}/lemonbeat
    install -m 0755 ${S}/lemonbeat/*.py ${D}${PYTHON_SITEPACKAGES_DIR}/lemonbeat/
}

FILES_${PN} += " \
    ${PYTHON_SITEPACKAGES_DIR}/lemonbeat/* \
"
