SUMMARY = "The Homekit Accessory Server"
MAINTAINER = "Gardena GmbH"
LICENSE = "CLOSED"

SRC_URI = " \
    gitsm://ssh.dev.azure.com/v3/HQV-Gardena/SG-Gateway/sg-homekit-accessory-server;protocol=ssh;branch=main \
    file://accessory-server.service \
    file://keep.d/accessory-server \
"

PR = "r0"
PV = "0.20.1"
SRCREV = "1fb3e3edfa645c2c2eed27065b4e801690f298b1"

S = "${WORKDIR}/git"

inherit cmake systemd

FILES:${PN} += " \
    ${base_libdir}/upgrade/keep.d \
    ${systemd_unitdir}/system/accessory-server.service \
"

DEPENDS = "mdns cjson mbedtls openssl curl util-linux gnutls nng systemd libevdev"
RDEPENDS:${PN} += "accessory-server-foss-dependencies"

EXTRA_OECMAKE += " \
  -DHAVE_MFI_HW_AUTH=${@oe.utils.conditional('HOMEKIT_HWAUTH','1','1','0',d)} \
  -DCMAKE_BUILD_TYPE=${@oe.utils.conditional('DEBUG_BUILD','1','Debug','RelWithDebInfo',d)} \
  -DHAP_LOG_LEVEL=${@oe.utils.conditional('DEBUG_BUILD','1','3','2',d)} \
"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${B}/main/gardena-accessory-server ${D}${bindir}/
    install -m 0755 ${B}/tools/provision/gardena-provision ${D}${bindir}/

    install -d ${D}${includedir}/accessoryserver
    install -m 0644 ${S}/main/include/wifiapi_public.h ${D}${includedir}/accessoryserver/

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/accessory-server.service ${D}${systemd_unitdir}/system/

    # Keep HomeKit data from being erased on update
    install -d ${D}${base_libdir}/upgrade/keep.d
    install -m 0644 ${WORKDIR}/keep.d/accessory-server ${D}${base_libdir}/upgrade/keep.d
}

SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = "accessory-server.service"
