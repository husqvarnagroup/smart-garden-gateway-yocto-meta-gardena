LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=bc41bb7fa5bd6d6c2b47de1cc649e107"

SRC_URI = " \
  git://github.com/husqvarnagroup/smart-garden-lemonbeat-serializer;protocol=https;branch=main \
"

SRCREV = "ec56850a47eb539a677f35b428a1db4880be7452"

INC_PR = "r4"


EXTRA_OECMAKE = " -DBUILD_SHARED_LIBS=y"

PACKAGES =+ "${PN}-lib"
PROVIDES =+ "${PN}-lib"

FILES:${PN}-lib += "\
  ${libdir}/liblsdl-serializer.so \
"

inherit cmake
