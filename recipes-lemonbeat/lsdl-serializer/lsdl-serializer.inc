# Usage: Include this file and specify SRCREV:
# SRCREV = "my-hash"

LICENSE = "CLOSED"
LIC_FILES_CHKSUM = ""

SRC_URI = " \
  git://stash.dss.husqvarnagroup.com/scm/sg/smart-garden-lemonbeat-serializer.git;protocol=https \
"

INC_PR = "r0"

S = "${WORKDIR}/git"

EXTRA_OECMAKE = " -DBUILD_SHARED_LIBS=y"

PACKAGES =+ "${PN}-lib"
PROVIDES =+ "${PN}-lib"

FILES_${PN}-lib += "\
  ${libdir}/liblsdl-serializer.so \
"

inherit cmake
