DESCRIPTION = "Brave New World IPSO Registry"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = " \
    file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28 \
"
SRC_URI += " \
    git://git@ssh.dev.azure.com/v3/HQV-Gardena/SG-Gateway/sg-bnw-ipso-registry;protocol=ssh;branch=main \
"

# Referenced commits must be in the `main` branch.
SRCREV = "5efca7d4c797ce2255adaab7021803b1101099d8"

FILES:${PN} += " \
    ${localstatedir}/lib/${PN} \
"

# The IPSO registry is not versioned. Use non-semantic versioning for this Yocto recipe.
PV = "2024-10-02"
PR = "r0"

S = "${WORKDIR}/git"

do_install:append() {
    # Ensure registry directories exist
    install -d ${D}${localstatedir}/lib/ipso_definitions/base

    # Copy definition files
    install -m 0644 ${S}/definitions/*.xml ${D}${localstatedir}/lib/ipso_definitions/base
}

PACKAGE_ARCH = "${MACHINE_ARCH}"
