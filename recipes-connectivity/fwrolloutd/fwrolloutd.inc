DEPENDS += "openssl"
RDEPENDS:${PN} += " \
     ipso-registry \
     openssl \
     openssl-bin openssl-conf \
"

SRC_URI += " \
    file://fwrolloutd.service \
"
CARGO_SRC_DIR = "fwrolloutd"

export CARGO_PROFILE_RELEASE_OPT_LEVEL="z"
export CARGO_PROFILE_RELEASE_CODEGEN_UNITS="1"

PR = "r4"

FILES:${PN} += " \
    ${systemd_unitdir}/system/ \
"

do_install:append() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/fwrolloutd.service ${D}${systemd_unitdir}/system/

    install -d ${D}${sysconfdir}
    install -m 0644 ${S}/fwrolloutd/config.yml ${D}${sysconfdir}/fwrolloutd.yml

    # Ensure required IPSO directories exist
    install -d ${D}${localstatedir}/lib/ipso_definitions/base
    install -d ${D}${localstatedir}/lib/ipso_definitions/fwrolloutd
}

inherit systemd
SYSTEMD_SERVICE:${PN} = "fwrolloutd.service"

RUSTFLAGS += "-latomic"
