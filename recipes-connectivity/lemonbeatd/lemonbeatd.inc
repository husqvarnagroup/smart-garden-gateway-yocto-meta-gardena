DEPENDS += "openssl dbus"
RDEPENDS:${PN} += "lemonbeatd-foss-dependencies"
RCONFLICTS:${PN} = "shadoway"

SRC_URI += " \
    file://keep.d/lemonbeatd \
    file://lemonbeatd.service \
"

PR = "r3"

CARGO_FEATURES="release_max_level_debug"
CARGO_BUILD_FLAGS += "--features '${CARGO_FEATURES}'"

export CARGO_PROFILE_RELEASE_OPT_LEVEL="z"
export CARGO_PROFILE_RELEASE_CODEGEN_UNITS="1"

FILES:${PN} += " \
    ${base_libdir}/upgrade/keep.d \
    ${systemd_unitdir}/system/ \
"

do_install:append() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/lemonbeatd.service ${D}${systemd_unitdir}/system/

    # Retain network settings and logs (warnings, errors) on update
    install -d ${D}${base_libdir}/upgrade/keep.d
    install -m 0644 ${WORKDIR}/keep.d/lemonbeatd ${D}${base_libdir}/upgrade/keep.d
}

inherit systemd
SYSTEMD_SERVICE:${PN} = "lemonbeatd.service"

RPROVIDES:${PN} += "virtual/gardena-lemonbeatd"

RUSTFLAGS += "-latomic"
