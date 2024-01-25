DESCRIPTION = "Brave New World LWM2M Server"
LICENSE = "EPL-1.0 & EPL-2.0 & BSD-3-Clause"
LIC_FILES_CHKSUM = " \
    file://../wakaama/wakaama-c/coap/er-coap-13/LICENSE;md5=bd9db1399d32da2d482fb0afb64b3d20 \
    file://../wakaama/wakaama-c/LICENSE.BSD-3-Clause;md5=fdff207498fc09f895880fe73373bae2 \
    file://../wakaama/wakaama-c/LICENSE.EPL-2;md5=6654f12d7f7ba53cf796b622931e86d4 \
"

inherit python3native
inherit setuptools3

SRC_URI += " \
    gitsm://git@ssh.dev.azure.com/v3/HQV-Gardena/SG-Gateway/sg-bnw-lwm2m-server;protocol=ssh;branch=main;tag=v${PV} \
    file://lwm2mserver.service \
    file://keep.d/lwm2mserver \
    file://0001-packaging-Exclude-lwm2mserver-IPSO-definitions-from-.patch;patchdir=.. \
"

PR = "r0"

DEPENDS = " \
    cmake-native \
    python3 \
    python3-cython-native \
    python3-distro-native \
    python3-importlib-resources \
    python3-native \
    python3-ninja-native \
    python3-scikit-build-native \
    python3-wheel-native \
    virtual/crypt \
"

RDEPENDS:${PN} += " \
    ipso-registry \
    lwm2mserver-foss-dependencies \
    virtual/gardena-lemonbeatd \
"

S = "${WORKDIR}/git/lwm2mserver"

FILES:${PN} += " \
    ${localstatedir}/lib/${PN} \
    ${base_libdir}/upgrade/keep.d \
"

do_install:append() {
    # Remove IPSO registry Python module only used during development
    rm -r ${S}/lwm2mserver_registry

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

    # Ensure required IPSO directories exist
    install -d ${D}${localstatedir}/lib/ipso_definitions/base
    install -d ${D}${localstatedir}/lib/ipso_definitions/fwrolloutd
    install -d ${D}${localstatedir}/lib/ipso_definitions/dev
}

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = " \
    lwm2mserver.service \
"
