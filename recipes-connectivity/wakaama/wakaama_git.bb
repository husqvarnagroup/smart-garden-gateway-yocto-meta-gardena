LICENSE = "EPL-1.0 & EDL-1.0"
LIC_FILES_CHKSUM = "file://coap/er-coap-13/LICENSE;md5=bd9db1399d32da2d482fb0afb64b3d20 \
                    file://examples/shared/tinydtls/LICENSE;md5=ffb073dbb36e7ec5e091047332f302c5"

SRC_URI = "gitsm://github.com/sbertin-telular/wakaama.git;protocol=https;branch=lwm2m_1.1"

PV = "2020-06-26+git${SRCPV}"
PR = "r0"
SRCREV = "d37af90783883393c0cb7fbecb5b6e2e1fa9f026"

S = "${WORKDIR}/git"
B = "${WORKDIR}/build"

inherit cmake

OECMAKE_SOURCEPATH = "${S}/examples"

EXTRA_OECMAKE += " \
    -DCMAKE_BUILD_TYPE=${@oe.utils.conditional('DEBUG_BUILD','1','Debug','RelWithDebInfo',d)} \
"

# Enable outdated features/formats
TARGET_CFLAGS += " \
    -DLWM2M_OLD_CONTENT_FORMAT_SUPPORT \
    -DLWM2M_SUPPORT_SENML_JSON \
"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${B}/bootstrap_server/bootstrap_server ${D}${bindir}/wakaama_bootstrap_server
    install -m 0755 ${B}/client/lwm2mclient ${D}${bindir}/wakaama_lwm2mclient
    install -m 0755 ${B}/lightclient/lightclient ${D}${bindir}/wakaama_lightclient
    install -m 0755 ${B}/server/lwm2mserver ${D}${bindir}/wakaama_lwm2mserver
}
