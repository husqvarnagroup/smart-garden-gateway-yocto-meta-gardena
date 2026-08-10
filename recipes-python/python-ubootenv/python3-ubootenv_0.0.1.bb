LICENSE = "LGPL-2.1-or-later"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/LGPL-2.1-or-later;md5=2a4f4fd2128ea2f65047ee63fbca9f68"
SECTION = "devel/python"

PR = "r2"

S = "${UNPACKDIR}"

inherit setuptools3

SRC_URI = " \
    file://build_ubootenv.py \
    file://setup.py \
    file://ubootenv.py \
"

DEPENDS += " \
    ${PYTHON_PN}-cffi \
    ${PYTHON_PN}-cffi-native \
    libubootenv \
"

RDEPENDS:${PN} += " \
    ${PYTHON_PN}-cffi \
"
