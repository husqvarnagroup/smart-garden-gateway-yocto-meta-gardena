# Usage: Include this file and specify SRCREV:
# SRCREV = "my-hash"

LICENSE = "CLOSED"
LIC_FILES_CHKSUM = ""

SRC_URI = " \
  git://stash.dss.husqvarnagroup.com:7999/sg/smart-garden-lemonbeat-serializer.git;protocol=ssh \
"

INC_PR = "r1"

S = "${WORKDIR}/git"

EXTRA_OECMAKE = " -DBUILD_SHARED_LIBS=y"

PACKAGES =+ "${PN}-lib"
PROVIDES =+ "${PN}-lib"

FILES_${PN}-lib += "\
  ${libdir}/liblsdl-serializer.so \
"

inherit cmake
