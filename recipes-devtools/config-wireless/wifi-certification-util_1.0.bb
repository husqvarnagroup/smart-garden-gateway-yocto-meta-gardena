DESCRIPTION = "Configuration tool for testing wireless adapter in EMV Testhaus"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

RDEPENDS_${PN} = "python3 python3-multiprocessing iperf3 dnsmasq hostapd tcpdump"

SRC_URI = "file://config_wireless.py"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/config_wireless.py ${D}${bindir}/config_wireless
}
