DESCRIPTION = "Portable network interface information for Python"
SECTION = "devel/python"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://PKG-INFO;beginline=8;endline=8;md5=8227180126797a0148f94f483f3e1489"

PR = "r1"

SRC_URI[md5sum] = "2a1fc4c42acef5cf080b618e800ee0d2"
SRC_URI[sha256sum] = "5edd3ccbeda2264d750d81533a11d45b5efbf8d99da65ba1f4569636f0eb9b70"

SRC_URI += " \
    file://0001-sg-noup-generate_api.sh-Customizable-nng-location.patch \
    file://0002-sg-noup-Link-against-shared-libraries.patch \
    file://0003-sg-noup-generate_api.sh-Drop-declared-but-undefined-.patch \
"

DEPENDS += "${PYTHON_PN}-pytest-runner-native ${PYTHON_PN}-cffi-native cmake-native mbedtls nng"

RDEPENDS:${PN} += " \
    libatomic \
    python3-cffi \
    python3-asyncio \
    python3-sniffio \
"

do_compile:prepend () {
    prefix="${RECIPE_SYSROOT}/${prefix}" outfile="${S}/nng_api.h" tmpfile="${S}/nng_api.h.tmp" "${S}/generate_api.sh"
}

inherit pypi setuptools3
