LICENSE = "LGPL-2.1"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/LGPL-2.1-only;md5=1a6d268fd218675ffea8be556788b780"
SECTION = "devel/python"

PR = "r0"

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
