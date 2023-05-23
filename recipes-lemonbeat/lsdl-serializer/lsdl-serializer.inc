LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=bc41bb7fa5bd6d6c2b47de1cc649e107"

SRC_URI = " \
  git://ssh.dev.azure.com/v3/HQV-Gardena/SG-Embedded/sg-lemonbeat-serializer;protocol=ssh;branch=main \
"

SRCREV = "v${PV}"

INC_PR = "r2"

S = "${WORKDIR}/git"

EXTRA_OECMAKE = " -DBUILD_SHARED_LIBS=y"

PACKAGES =+ "${PN}-lib"
PROVIDES =+ "${PN}-lib"

FILES:${PN}-lib += "\
  ${libdir}/liblsdl-serializer.so \
"

inherit cmake
