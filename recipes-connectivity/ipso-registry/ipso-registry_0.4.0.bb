DESCRIPTION = "Brave New World IPSO Registry"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = " \
    file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28 \
"
SRC_URI += " \
    git://git@ssh.dev.azure.com/v3/HQV-Gardena/SG-Gateway/sg-bnw-ipso-registry;protocol=ssh;branch=main \
"

SRCREV = "3bab8e7b0434a4b44fc0c35f40d4ce4ac155a41b"

FILES:${PN} += " \
    ${localstatedir}/lib/${PN} \
"

PR = "r0"

S = "${WORKDIR}/git"

do_install:append() {
    # Ensure registry directories exist
    install -d ${D}${localstatedir}/lib/ipso_definitions/base

    # Copy definition files
    install -m 0644 ${S}/definitions/*.xml ${D}${localstatedir}/lib/ipso_definitions/base
}

PACKAGE_ARCH = "${MACHINE_ARCH}"