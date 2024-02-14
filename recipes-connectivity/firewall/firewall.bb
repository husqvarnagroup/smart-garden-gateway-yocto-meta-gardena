DESCRIPTION = "Basic firewall"
MAINTAINER = "Gardena GmbH"
HOMEPAGE = "https://www.gardena.com/"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28"

PV = "1.2.0"
PR = "r0"

RDEPENDS:${PN} = "iptables iptables-modules kernel-module-xt-dscp"

SRC_URI = "\
    file://firewall.nft \
    file://firewall.sh \
    file://firewall.service \
"

S = "${WORKDIR}"

do_install() {
    install -d ${D}${sysconfdir}/nftables
    install -m 0644 ${WORKDIR}/firewall.nft  ${D}/${sysconfdir}/nftables/gardena-firewall.nft

    install -d ${D}${sbindir}
    install -m 755 ${S}firewall.sh ${D}${sbindir}/firewall

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/firewall.service ${D}${systemd_unitdir}/system
}

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = "firewall.service"
