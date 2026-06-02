DESCRIPTION = "Automatic Development Package Installer"
MAINTAINER = "Gardena GmbH"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
PR = "r0"

inherit allarch systemd

SRC_URI = " \
    file://devpkginstaller.service \
    file://devpkginstaller.sh \
    file://keep.d/${BPN} \
"

FILES:${PN} = " \
    ${base_libdir}/upgrade/keep.d \
    ${bindir}/devpkginstaller \
    ${systemd_unitdir}/system/devpkginstaller.service \
"

RDEPENDS:${PN} = " \
    busybox \
    opkg \
"

do_install() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/devpkginstaller.service ${D}${systemd_unitdir}/system

    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/devpkginstaller.sh ${D}${bindir}/devpkginstaller

    install -d ${D}${base_libdir}/upgrade/keep.d
    install -m 0644 ${UNPACKDIR}/keep.d/${PN} ${D}${base_libdir}/upgrade/keep.d
}

SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = " \
    devpkginstaller.service \
"
