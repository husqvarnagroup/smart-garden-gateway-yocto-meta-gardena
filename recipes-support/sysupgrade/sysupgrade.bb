SUMMARY = "Manage data migration on system upgrade"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

FILESEXTRAPATHS:append := "${THISDIR}/files:"

SRC_URI = " \
    file://keep.d/sysupgrade \
    file://sysupgrade.conf \
    file://sysupgrade.sh \
"

PR = "r1"
PV = "2.0"

RDEPENDS:${PN} = "initscripts-readonly-rootfs-overlay overlayfs-purge"

FILES:${PN} += " \
    ${base_libdir}/upgrade/keep.d \
"

do_install () {
    install -d ${D}${sbindir}
    install -m 0755 ${WORKDIR}/sysupgrade.sh ${D}${sbindir}/sysupgrade

    install -d ${D}${base_libdir}/upgrade/keep.d
    install -m 0644 ${WORKDIR}/keep.d/sysupgrade ${D}${base_libdir}/upgrade/keep.d

    install -d ${D}${sysconfdir}
    install -m 0644 ${WORKDIR}/sysupgrade.conf ${D}${sysconfdir}

    # Create persistent log dir
    install -d ${D}${localstatedir}/lib/sysupgrade
}

inherit allarch
