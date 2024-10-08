SUMMARY = "Simple test tool for serial connections"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE.MIT;md5=544799d0b492f119fa04641d1b8868ed"

SRC_URI = "git://github.com/cbrake/linux-serial-test.git;protocol=https;branch=master \
        file://0001-Fix-missing-CMSPAR-definition-in-glibc.patch \
"

PV = "1.0+git${SRCPV}"
PR = "r1"
SRCREV = "aed2a6e78160b63295368d70dbdbc19fe3a38225"

S = "${WORKDIR}/git"

inherit cmake
