DESCRIPTION = "Brave New World IPSO Registry"
LICENSE = "GPL-3.0-or-later & BSD-3-Clause & CC0-1.0"
LIC_FILES_CHKSUM = " \
file://LICENSES/GPL-3.0-or-later.txt;md5=75d892af193fd5a298f724c4377d8f62 \
file://LICENSES/BSD-3-Clause.txt;md5=71f739ef75581cae312e8c711bcdab16 \
file://LICENSES/CC0-1.0.txt;md5=65d3616852dbf7b1a6d4b53b00626032 \
"
SRC_URI += " \
    git://github.com/husqvarnagroup/smart-garden-ipso-registry.git;protocol=https;branch=main \
"

# Referenced commits must be in the `main` branch.
SRCREV = "83dbd603b849dfdfb693fad52d82c3dc7abb04ce"

FILES:${PN} += " \
    ${localstatedir}/lib/${PN} \
"

# The IPSO registry is not versioned. Use non-semantic versioning for this Yocto recipe.
PV = "2026-07-01"
PR = "r0"


do_install:append() {
    # Ensure registry directories exist
    install -d ${D}${localstatedir}/lib/ipso_definitions/base

    # Copy definition files
    install -m 0644 ${S}/definitions/*.xml ${D}${localstatedir}/lib/ipso_definitions/base
}

PACKAGE_ARCH = "${MACHINE_ARCH}"
