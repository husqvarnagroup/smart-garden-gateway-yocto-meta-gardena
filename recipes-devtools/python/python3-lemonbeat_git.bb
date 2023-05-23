DESCRIPTION = "Lemonbeat Python library"
LICENSE = "CLOSED"

inherit python3-dir

SRC_URI += " \
    git://ssh.dev.azure.com/v3/HQV-Gardena/SG-Embedded/sg-lemonbeat-python;protocol=ssh;branch=main \
"

PR = "r0"
PV = "2022-03-28+git${SRCPV}"

SRCREV = "b23cdbb60d19a2d0d7a39e9962d499db8757fd74"

S = "${WORKDIR}/git"

RDEPENDS:${PN} += " \
    lsdl-serializer-lib \
    python3-core \
    python3-ctypes \
    python3-fcntl \
    python3-pycryptodomex \
    python3-threading \
    python3-xml \
"

do_install() {
    # Files for python3-lemonbeat
    install -d ${D}${PYTHON_SITEPACKAGES_DIR}/lemonbeat
    install -m 0755 ${S}/lemonbeat/*.py ${D}${PYTHON_SITEPACKAGES_DIR}/lemonbeat/

    # Files for lemonbeat-firmware-upload
    install -d ${D}${bindir}
    install -m 0755 ${S}/examples/gateway_scripts/upload.py ${D}${bindir}/upload
}

FILES:${PN} += " \
    ${PYTHON_SITEPACKAGES_DIR}/lemonbeat/* \
"

RDEPENDS:lemonbeat-firmware-upload = " \
    ${PN} \
    lsdl-serializer-lib \
    python3-crcmod \
    python3-multiprocessing \
"

FILES:lemonbeat-firmware-upload = "${bindir}/upload"

PROVIDES =+ "lemonbeat-firmware-upload"
PACKAGES =+ "lemonbeat-firmware-upload"
