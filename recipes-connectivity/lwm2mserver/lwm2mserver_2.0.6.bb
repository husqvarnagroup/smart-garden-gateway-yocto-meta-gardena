DESCRIPTION = "Brave New World LWM2M Server"
LICENSE = "EPL-1.0 & EPL-2.0 & BSD-3-Clause"
LIC_FILES_CHKSUM = " \
    file://lwm2mserver/wakaama/src/wakaama-c/coap/er-coap-13/LICENSE;md5=bd9db1399d32da2d482fb0afb64b3d20 \
    file://lwm2mserver/wakaama/src/wakaama-c/LICENSE.BSD-3-Clause;md5=fdff207498fc09f895880fe73373bae2 \
    file://lwm2mserver/wakaama/src/wakaama-c/LICENSE.EPL-2;md5=6654f12d7f7ba53cf796b622931e86d4 \
"

inherit python_poetry_core python3-dir

SRCREV = "de2087bf50f643cbc2200805c3bd12bb93b74faa"
SRC_URI += " \
    gitsm://git@ssh.dev.azure.com/v3/HQV-Gardena/SG-Gateway/sg-bnw-lwm2m-server;protocol=ssh;branch=main \
    file://lwm2mserver.service \
    file://keep.d/lwm2mserver \
"

PR = "r0"

DEPENDS = " \
    cmake-native \
    ninja-native \
    python3-cmake-native \
    python3-cython-native \
    python3-poetry-core-native \
    virtual/crypt \
"

RDEPENDS:${PN} += " \
    ipso-registry \
    lwm2mserver-foss-dependencies \
    lemonbeatd \
"

S = "${WORKDIR}/git"

FILES:${PN} += " \
    ${localstatedir}/lib/${PN} \
    ${base_libdir}/upgrade/keep.d \
"

do_install:append() {
    # Remove IPSO registry Python module only used during development
    rm -r ${D}/${PYTHON_SITEPACKAGES_DIR}/lwm2mserver/wakaama/ipso_definitions
    rm -r ${D}/${PYTHON_SITEPACKAGES_DIR}/lwm2mserver_registry/ipso_registry
    # Remove Wakaama source not needed in the final build
    rm -r ${D}/${PYTHON_SITEPACKAGES_DIR}/lwm2mserver/wakaama/src

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/lwm2mserver.service ${D}${systemd_unitdir}/system

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

# Cython puts paths of input source files into the generated C code
INSANE_SKIP:${PN} = "buildpaths"
INSANE_SKIP:${PN}-src = "buildpaths"
