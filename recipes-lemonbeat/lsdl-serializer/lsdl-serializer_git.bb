LICENSE = "CLOSED"
LIC_FILES_CHKSUM = ""

SRC_URI = " \
  git://stash.dss.husqvarnagroup.com/scm/sg/smart-garden-lemonbeat-serializer.git;protocol=https \
"

PV = "0.2.3+git${SRCPV}"
SRCREV = "fd3d6e7b60863b93cc423777785fc57382c18cb4"

S = "${WORKDIR}/git"

EXTRA_OECMAKE = " -DBUILD_SHARED_LIBS=y"

PACKAGES =+ "${PN}-lib"

FILES_${PN}-lib += "\
  ${libdir}/liblsdl-serializer.so \
"

inherit cmake
