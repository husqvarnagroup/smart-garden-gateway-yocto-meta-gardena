SUMMARY = "LibCYAML: Schema-based YAML parsing and serialisation"
DESCRIPTION = "LibCYAML is a C library for reading and writing structured YAML documents. \
It is written in ISO C11 and licensed under the ISC licence."
HOMEPAGE = "https://github.com/tlsa/libcyaml"
SECTION = "libs/devel"

LICENSE = "ISC"
LIC_FILES_CHKSUM = "file://LICENSE;md5=52a1707594d3c6694292db3dd1a7f960"

DEPENDS = "libyaml"
RDEPENDS_${PN} = "libyaml"

SRC_URI = "https://github.com/tlsa/libcyaml/archive/v${PV}.tar.gz"
SRC_URI[md5sum] = "f14c3280d76dedfc1821c9cb20d878e3"
SRC_URI[sha256sum] = "89a90d4304f6df311a117c8a960719505ada2614fb5090f69e1c20aad09908ef"

S = "${WORKDIR}/libcyaml-${PV}"

do_compile() {
        oe_runmake
}

do_install() {
        oe_runmake install \
                DESTDIR="${D}" \
                PREFIX="${prefix}"
}

BBCLASSEXTEND = "native nativesdk"
