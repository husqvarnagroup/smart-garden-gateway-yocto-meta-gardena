SUMMARY = "Linux zram-init container script"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://${S}/README.md;beginline=5;endline=7;md5=1c6f4971407e5a5b1aa502b9badcdf98"

inherit allarch

RDEPENDS_${PN} = " \
    e2fsprogs-mke2fs \
    e2fsprogs-tune2fs \
    "

SRC_URI = "git://github.com/vaeth/zram-init.git;protocol=https;branch=main"
SRCREV = "ca2f79101c56f123d2d23f3f62f14569ad30000b"

S = "${WORKDIR}/git"

PR = "r0"

# compilation is not required
do_compile[noexec] = "1"

do_install () {
    install -d ${D}${base_sbindir}
    install -m 0755 ${S}/sbin/zram-init.in ${D}${base_sbindir}/zram-init
}

FILES_${PN} = " \
    ${base_sbindir} \
    "
