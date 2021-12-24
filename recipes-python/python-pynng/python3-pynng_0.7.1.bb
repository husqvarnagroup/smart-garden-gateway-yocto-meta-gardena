DESCRIPTION = "Portable network interface information for Python"
SECTION = "devel/python"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://PKG-INFO;beginline=8;endline=8;md5=8227180126797a0148f94f483f3e1489"

PR = "r0"

SRC_URI[md5sum] = "2a1fc4c42acef5cf080b618e800ee0d2"
SRC_URI[sha256sum] = "5edd3ccbeda2264d750d81533a11d45b5efbf8d99da65ba1f4569636f0eb9b70"

DEPENDS += "${PYTHON_PN}-pytest-runner-native ${PYTHON_PN}-cffi-native cmake-native mbedtls nng"

RDEPENDS_${PN} += " \
    libatomic \
    python3-cffi \
    python3-asyncio \
    python3-sniffio \
    python3-typing \
"

inherit pypi setuptools3
