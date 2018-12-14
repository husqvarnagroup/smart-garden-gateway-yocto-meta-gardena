LICENSE = "GPLv2"
DEPENDS = "libnl"
RDEPENDS_${PN} = "kernel"
SRC_URI = "\
    git://git.openwrt.org/openwrt/openwrt.git;protocol=https;branch=openwrt-18.06 \
    file://0001-fix-build.patch \
"

PR = "r1"
PV = "18.06+git${SRCPV}"
SRCREV = "70255e3d624cd393612069aae0a859d1acbbeeae"
S = "${WORKDIR}/git/package/network/config/swconfig/src"
LIC_FILES_CHKSUM = "file://${WORKDIR}/git/LICENSE;md5=94d55d512a9ba36caa9b7df079bae19f"

CFLAGS_append = " -I ${WORKDIR}/include -I ${STAGING_INCDIR}/libnl3"

do_configure() {
	mkdir -p "${WORKDIR}/include/linux"
	cp "${STAGING_KERNEL_DIR}/include/uapi/linux/switch.h" "${WORKDIR}/include/linux/"
}

do_compile () {
	oe_runmake
}

do_install () {
	install -d ${D}${bindir}
	install -m 0755 ${S}/swconfig ${D}${bindir}/
}
