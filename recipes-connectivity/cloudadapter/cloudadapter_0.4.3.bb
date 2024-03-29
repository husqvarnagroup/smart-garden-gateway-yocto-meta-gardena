DESCRIPTION = "Brave New World Cloudadapter"
LICENSE = "Proprietary"

LIC_FILES_CHKSUM = " \
    file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28 \
"

inherit python3native
inherit setuptools3

SRC_URI += " \
    gitsm://git@ssh.dev.azure.com/v3/HQV-Gardena/SG-Gateway/sg-bnw-cloud-adapter;protocol=ssh;branch=main;tag=v${PV} \
    file://cloudadapter.service \
    file://aws-root-ca.crt \
    file://keep.d/cloudadapter \
"

PR = "r1"

DEPENDS = " \
    python3 \
    python3-native \
    cmake-native \
    python3-cython-native \
    python3-ubootenv \
    python3-wheel-native \
    python3-distro-native \
    python3-ninja-native \
    python3-scikit-build-native \
    virtual/crypt \
"

S = "${WORKDIR}/git"

FILES:${PN} += " \
    ${localstatedir}/lib/${PN} \
    ${base_libdir}/upgrade/keep.d \
"

do_install:append() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/cloudadapter.service ${D}${systemd_unitdir}/system

    install -d ${D}${sysconfdir}/ssl/certs
    install -m 0644 ${WORKDIR}/aws-root-ca.crt ${D}${sysconfdir}/ssl/certs

    install -d ${D}${localstatedir}/lib/${PN}

    # Move executables to /usr/bin/
    mv ${D}${datadir}/bin/${PN}_native ${D}${bindir}
    rmdir ${D}${datadir}/bin ${D}${datadir}

    # Retain persisted data
    install -d ${D}${base_libdir}/upgrade/keep.d
    install -m 0644 ${WORKDIR}/keep.d/cloudadapter ${D}${base_libdir}/upgrade/keep.d
}

RDEPENDS:${PN} += " \
    cloudadapter-foss-dependencies \
"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = " \
    cloudadapter.service \
"
