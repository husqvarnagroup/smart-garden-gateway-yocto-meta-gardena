DESCRIPTION = "Argtable is an ANSI C library for parsing GNU style command line options."
SECTION = "libs"
LICENSE = "LGPLv2.0"
HOMEPAGE = "http://argtable.sourceforge.net/"

LIC_FILES_CHKSUM = "file://COPYING;md5=f30a9716ef3762e3467a2f62bf790f0a"

PR = "r1"

DEPENDS = "libtool-cross"

PV_MAJOR = "${@d.getVar('PV').split('.')[0]}"
PV_MINOR = "${@d.getVar('PV').split('.')[1]}"

VERSION_STRING = "${PV_MAJOR}-${PV_MINOR}"

SRC_URI = "${SOURCEFORGE_MIRROR}/${BPN}/${BPN}${VERSION_STRING}.tar.gz"

S="${WORKDIR}/${PN}${VERSION_STRING}"

inherit autotools

SRC_URI[md5sum] = "156773989d0d6406cea36526d3926668"
SRC_URI[sha256sum] = "8f77e8a7ced5301af6e22f47302fdbc3b1ff41f2b83c43c77ae5ca041771ddbf"

