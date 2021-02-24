DESCRIPTION = "Portable network interface information for Python"
SECTION = "devel/python"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://PKG-INFO;beginline=8;endline=8;md5=8227180126797a0148f94f483f3e1489"

PR = "r0"

SRC_URI[md5sum] = "0b4f458648cce0defd2555b8f5288718"
SRC_URI[sha256sum] = "40920554979b49241d98b483383e97885f681a78a24273d51e0f341da8c026f7"

SRC_URI += "file://0001-Link-libatomic.patch"

DEPENDS += "${PYTHON_PN}-pytest-runner-native ${PYTHON_PN}-cffi-native cmake-native"

RDEPENDS_${PN} += "libatomic \
                   python3-cffi \
                   python3-asyncio \
                   python3-sniffio \
                   python3-typing"

inherit pypi setuptools3
