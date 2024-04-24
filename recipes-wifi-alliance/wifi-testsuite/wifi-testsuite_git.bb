SUMMARY = "Wi-Fi Test Suite Linux Control Agent"
LICENSE = "ISC"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=0542427ed5c315ca34aa09ae7a85ed32"

SRC_URI = " \
  git://ssh.dev.azure.com/v3/HQV-Gardena/SG-Gateway/sg-gateway-wifi-alliance-test-suite;protocol=ssh;branch=main \
  file://eth0.network.disabled \
  file://wifi-testsuite-install.sh \
"

PR = "r6"
PV = "10.10.1+git${SRCPV}"
SRCREV = "63cd320851e875ee278a4b019b9ad537104cc21b"

DEPENDS = "libtirpc"

# procps supports the arguments 'a' and 'x' which are used by WTS
RDEPENDS:${PN} = "\
  procps \
  wpa-supplicant-cli \
"

S = "${WORKDIR}/git"
FILES:${PN} += "\
    /usr/local/sbin/ \
    ${systemd_unitdir}/network \
"

do_install () {
    install -d ${D}${bindir}
    install -m 0755 ${S}/ca/wfa_ca ${D}${bindir}
    install -m 0755 ${S}/dut/wfa_dut ${D}${bindir}
    install -m 0755 ${WORKDIR}/wifi-testsuite-install.sh ${D}${bindir}/wifi-testsuite-install
    install -m 0755 -D ${S}/scripts/*.sh ${D}${bindir}
    # Workaround for the hardcoded paths
    install -d ${D}/usr/local/sbin
    ln -s ${bindir}/getipconfig.sh ${D}/usr/local/sbin/
    ln -s ${bindir}/findprocess.sh ${D}/usr/local/sbin/

    # Configure static address on eth0
    install -d 0755 ${D}${systemd_unitdir}/network
    install -m 0644 ${WORKDIR}/eth0.network.disabled ${D}${systemd_unitdir}/network
}
