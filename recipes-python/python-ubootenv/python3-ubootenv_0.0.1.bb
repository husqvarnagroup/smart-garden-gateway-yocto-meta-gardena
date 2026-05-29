LICENSE = "LGPL-2.1-or-later"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/LGPL-2.1-or-later;md5=4fbd65380cdd255951079008b364516c"
SECTION = "devel/python"

PR = "r1"

S = "${WORKDIR}"

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
