DESCRIPTION = "Brave New World IPSO Registry"
LICENSE = "GPL-2.0-or-later"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0-or-later;md5=fed54355545ffd980b814dab4a3b312c"
SRC_URI += " \
    git://git@ssh.dev.azure.com/v3/HQV-Gardena/SG-Gateway/sg-bnw-ipso-registry;protocol=ssh;branch=main \
"

# Referenced commits must be in the `main` branch.
SRCREV = "a8dabba6b439a5e3cd9665f4b524b57db45744d3"

FILES:${PN} += " \
    ${localstatedir}/lib/${PN} \
"

# The IPSO registry is not versioned. Use non-semantic versioning for this Yocto recipe.
PV = "2026-04-16"
PR = "r1"

S = "${WORKDIR}/git"

do_install:append() {
    # Ensure registry directories exist
    install -d ${D}${localstatedir}/lib/ipso_definitions/base

    # Copy definition files
    install -m 0644 ${S}/definitions/*.xml ${D}${localstatedir}/lib/ipso_definitions/base
}

PACKAGE_ARCH = "${MACHINE_ARCH}"
