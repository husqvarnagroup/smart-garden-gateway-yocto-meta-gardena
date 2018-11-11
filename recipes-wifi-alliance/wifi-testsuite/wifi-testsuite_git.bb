SUMMARY = "Wi-Fi Test Suite Linux Control Agent"
LICENSE = "ISC"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=0542427ed5c315ca34aa09ae7a85ed32"

SRC_URI = "git://stash.dss.husqvarnagroup.com/scm/sg/smart-garden-gateway-wifi-alliance-test-suite.git;protocol=https \
           "
PR = "r0"
PV = "1.0+git${SRCPV}"
SRCREV = "a16500480b91d04a5e89a27ba4bc890ac9707ee0"

DEPENDS = "libtirpc"

S = "${WORKDIR}/git"

do_install () {
	install -d ${D}${bindir}
	install -m 0755 ${S}/ca/wfa_ca ${D}${bindir}
	install -m 0755 ${S}/dut/wfa_dut ${D}${bindir}
	install -m 0755 -D ${S}/scripts/*.sh ${D}${bindir}
}

