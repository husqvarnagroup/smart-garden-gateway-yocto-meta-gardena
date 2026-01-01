DESCRIPTION = "Basic firewall"
MAINTAINER = "Gardena GmbH"
HOMEPAGE = "https://www.gardena.com/"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28"

PV = "1.4.0"
PR = "r0"

RDEPENDS:${PN} = "iptables iptables-modules kernel-module-xt-dscp"

SRC_URI = "\
    file://firewall.sh \
    file://firewall.service \
    file://keep.d/firewall \
"

S = "${WORKDIR}"

FILES:${PN} += " \
    ${base_libdir}/upgrade/keep.d \
"

do_install() {
    install -d ${D}${sbindir}
    install -m 755 ${S}/firewall.sh ${D}${sbindir}/firewall

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/firewall.service ${D}${systemd_unitdir}/system

    # Keep file that indicates that local SSH access is allowed
    install -d ${D}${base_libdir}/upgrade/keep.d
    install -m 0644 ${WORKDIR}/keep.d/firewall ${D}${base_libdir}/upgrade/keep.d
}

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = "firewall.service"
