LICENSE = "EPL-1.0 & EDL-1.0"
LIC_FILES_CHKSUM = "file://core/er-coap-13/LICENSE;md5=bd9db1399d32da2d482fb0afb64b3d20 \
                    file://examples/shared/tinydtls/LICENSE;md5=ffb073dbb36e7ec5e091047332f302c5"

SRC_URI = "gitsm://github.com/eclipse/wakaama.git;protocol=https"

PV = "2020-02-21+git${SRCPV}"
PR = "r0"
SRCREV = "31d64c0c41fae9653c1fa53ef58d1a44e49017fa"

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
