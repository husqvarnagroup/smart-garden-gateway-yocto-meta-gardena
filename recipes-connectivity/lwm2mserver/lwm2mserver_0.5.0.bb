DESCRIPTION = "Brave New World LWM2M Server"
LICENSE = "EPL-1.0 & EPL-2.0 & EDL-1.0"
LIC_FILES_CHKSUM = " \
    file://../wakaama/wakaama-c/coap/er-coap-13/LICENSE;md5=bd9db1399d32da2d482fb0afb64b3d20 \
    file://../wakaama/wakaama-c/LICENSE.edl-v1.0.md;md5=ce96a44bdd528b21c008d7cb818439e1 \
    file://../wakaama/wakaama-c/LICENSE-epl-v2.0.md;md5=6654f12d7f7ba53cf796b622931e86d4 \
"

inherit python3native
inherit setuptools3

SRC_URI += " \
    gitsm://git@ssh.dev.azure.com/v3/HQV-Gardena/SG-Gateway/sg-bnw-lwm2m-server;protocol=ssh;branch=main;tag=v${PV} \
    file://lwm2mserver.service \
    file://keep.d/lwm2mserver \
"

PR = "r1"

DEPENDS = " \
    cmake-native \
    python3 \
    python3-cython-native \
    python3-distro-native \
    python3-native \
    python3-ninja-native \
    python3-scikit-build-native \
    python3-wheel-native \
    virtual/crypt \
"

RDEPENDS:${PN} += " \
    lwm2mserver-foss-dependencies \
    virtual/gardena-lemonbeatd \
"

S = "${WORKDIR}/git/lwm2mserver"

FILES:${PN} += " \
    ${localstatedir}/lib/${PN} \
    ${base_libdir}/upgrade/keep.d \
"

do_install:append() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/lwm2mserver.service ${D}${systemd_unitdir}/system

    # Move executables to /usr/bin/
    mv ${D}${datadir}/bin/${PN}_native ${D}${bindir}
    rmdir ${D}${datadir}/bin ${D}${datadir}

    # Ensure lwm2mserver data dir exists
    install -d ${D}${localstatedir}/lib/${PN}

    # Retain persisted data
    install -d ${D}${base_libdir}/upgrade/keep.d
    install -m 0644 ${WORKDIR}/keep.d/lwm2mserver ${D}${base_libdir}/upgrade/keep.d
}

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = " \
    lwm2mserver.service \
"
