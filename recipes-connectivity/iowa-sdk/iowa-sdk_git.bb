LICENSE = "Proprietary & Apache-2.0 & EPL-1.0 & EDL-1.0"
LIC_FILES_CHKSUM = "file://../LICENSE.txt;md5=6535f959570b3de6f034f1ad4f974bc8 \
                    file://../externals/mbedtls/LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57 \
                    file://../externals/tinydtls/LICENSE;md5=ffb073dbb36e7ec5e091047332f302c5"

SRC_URI = "git://git@stash.dss.husqvarnagroup.com:7999/sg/iowa-sdk.git;protocol=ssh"

# Modify these as desired
PV = "2020-07-22+git${SRCPV}"
PR = "r0"
SRCREV = "d2076ddfd9266fbef0dae89eeb53ff3ac80c56fb"

S = "${WORKDIR}/git/samples"
B = "${WORKDIR}/build"

inherit cmake

EXTRA_OECMAKE += " \
    -DCMAKE_BUILD_TYPE=${@oe.utils.conditional('DEBUG_BUILD','1','Debug','RelWithDebInfo',d)} \
"

TARGET_CFLAGS += " \
    -DIOWA_UDP_SUPPORT \
    -DIOWA_COAP_BLOCK_SUPPORT \
    -DLWM2M_SUPPORT_TIMESTAMP \
"

OECMAKE_TARGET_COMPILE = "all"

do_install() {
    install -d ${D}${bindir}
    for binary in bootstrap_client bootstrap_server client client_1_1 custom_object_client fw_update_client server server_1_1 user_security_client; do
        install -m 0755 ${B}/$binary/$binary ${D}${bindir}/iowa_$binary
    done
}
