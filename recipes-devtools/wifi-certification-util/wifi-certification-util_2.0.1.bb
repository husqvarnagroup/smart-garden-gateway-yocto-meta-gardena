DESCRIPTION = "Configuration tool for 802.11 EMV certification"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

RDEPENDS:${PN} = "python3-multiprocessing iperf3 dnsmasq hostapd tcpdump"

SRC_URI = "file://wifi-certification-util.py"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/wifi-certification-util.py ${D}${bindir}/wifi-certification-util
}
