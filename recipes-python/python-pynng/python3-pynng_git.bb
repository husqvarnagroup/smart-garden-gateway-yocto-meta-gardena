DESCRIPTION = "Python bindings for Nanomsg Next Generation"
SECTION = "devel/python"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=57ac8de47c94532bc709cda1fe21c1a5"

DEPENDS += " \
    nng \
    cmake-native \
    ninja-native \
    python3-cffi-native \
    python3-cmake-native \
"

RDEPENDS:${PN} += " \
    libatomic \
    python3-cffi \
    python3-asyncio \
    python3-sniffio \
"

SRC_URI += " \
    git://github.com/husqvarnagroup/pynng.git;branch=gardena/main/2024-04-08;protocol=https \
"
SRCREV="5a3792201808afbbadff79aa639cedb425cf63e8"

S = "${WORKDIR}/git"

PR = "r0"

inherit python_setuptools_build_meta

