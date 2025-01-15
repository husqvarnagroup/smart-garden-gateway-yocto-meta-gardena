DESCRIPTION = "Generic MQTT client"
HOMEPAGE = "https://github.com/easybe/smart-garden-gateway-mqtt-client"
LICENSE = "GPL-3.0-or-later"
LIC_FILES_CHKSUM = " \
    file://LICENSE;md5=1ebbd3e34237af26da5dc08a4e440464 \
"

PR = "r1"
SRCREV = "17eff60bbd2a5bfac9dd5549a8e0805368c906c8"
SRC_URI = " \
    git://github.com/easybe/smart-garden-gateway-mqtt-client.git;protocol=https;branch=main \
    file://keep.d/sg-mqtt-client \
    file://sg-mqtt-client.service \
"

S = "${WORKDIR}/git"

inherit cargo cargo-update-recipe-crates

CARGO_SRC_DIR = ""

RUSTFLAGS += "-latomic"

do_install:append() {
    install -Dm 0644 ${WORKDIR}/sg-mqtt-client.service ${D}${systemd_unitdir}/system/sg-mqtt-client.service
    install -Dm 0644 ${WORKDIR}/keep.d/sg-mqtt-client ${D}${base_libdir}/upgrade/keep.d/sg-mqtt-client
}

FILES:${PN} += " \
    ${base_libdir}/upgrade/keep.d/sg-mqtt-client \
"

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = " \
    sg-mqtt-client.service \
"

include sg-mqtt-client-crates.inc
