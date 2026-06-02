DESCRIPTION = "Basic firewall"
MAINTAINER = "Gardena GmbH"
HOMEPAGE = "https://www.gardena.com/"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PV = "1.4.0"
PR = "r1"

RDEPENDS:${PN} = "iptables iptables-modules kernel-module-xt-dscp"

SRC_URI = "\
    file://firewall.sh \
    file://firewall.service \
    file://keep.d/firewall \
"

S = "${UNPACKDIR}"

FILES:${PN} += " \
    ${base_libdir}/upgrade/keep.d \
"

do_install() {
    install -d ${D}${sbindir}
    install -m 755 ${S}/firewall.sh ${D}${sbindir}/firewall

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/firewall.service ${D}${systemd_unitdir}/system

    # Keep file that indicates that local SSH access is allowed
    install -d ${D}${base_libdir}/upgrade/keep.d
    install -m 0644 ${UNPACKDIR}/keep.d/firewall ${D}${base_libdir}/upgrade/keep.d
}

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = "firewall.service"
