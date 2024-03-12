SUMMARY = "Firmware files for the MT7688"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "\
    file://firmware/LICENSE;md5=1bff2e28f0929e483370a43d4d8b6f8e \
"

COMPATIBLE_MACHINE = "mt7688"

SRC_URI = "\
    git://github.com/openwrt/mt76.git;protocol=https;branch=master \
"

SRCREV = "b6673b0057703fa59ed6d561bddba212b5710c53"
PV = "2020-12-04+git${SRCPV}"
PR = "r2"

inherit allarch

S = "${WORKDIR}/git"

CLEANBROKEN = "1"

do_compile() {
  :
}

do_install () {
	  install -d  ${D}${base_libdir}/firmware/
	  install -m 0644 ${S}/firmware/mt7628_e1.bin ${D}${base_libdir}/firmware/
	  install -m 0644 ${S}/firmware/mt7628_e2.bin ${D}${base_libdir}/firmware/
}

FILES:${PN} = "${base_libdir}/firmware/mt7628*"
