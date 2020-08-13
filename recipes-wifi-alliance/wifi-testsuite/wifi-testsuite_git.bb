SUMMARY = "Wi-Fi Test Suite Linux Control Agent"
LICENSE = "ISC"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=0542427ed5c315ca34aa09ae7a85ed32"

SRC_URI = "git://stash.dss.husqvarnagroup.com:7999/sg/smart-garden-gateway-wifi-alliance-test-suite.git;protocol=ssh \
           "
PR = "r2"
PV = "10.10.1+git${SRCPV}"
SRCREV = "63cd320851e875ee278a4b019b9ad537104cc21b"

DEPENDS = "libtirpc"
RDEPENDS_${PN} = "wpa-supplicant-cli"

S = "${WORKDIR}/git"

FILES_${PN} += "\
    /usr/local/sbin/ \
"

do_install () {
    install -d ${D}${bindir}
    install -m 0755 ${S}/ca/wfa_ca ${D}${bindir}
    install -m 0755 ${S}/dut/wfa_dut ${D}${bindir}
    install -m 0755 -D ${S}/scripts/*.sh ${D}${bindir}
    # Workaround for the hardcoded paths
    install -d ${D}/usr/local/sbin
    ln -s ${bindir}/getipconfig.sh ${D}/usr/local/sbin/
    ln -s ${bindir}/findprocess.sh ${D}/usr/local/sbin/
}
