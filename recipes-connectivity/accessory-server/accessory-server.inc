SUMMARY = "The Homekit Accessory Server"
MAINTAINER = "Gardena GmbH"
LICENSE = "CLOSED"

PV = "0.22.1"
PR = "r1"

SRC_URI = " \
    gitsm://ssh.dev.azure.com/v3/HQV-Gardena/SG-Gateway/sg-homekit-accessory-server;protocol=ssh;branch=main;tag=release/${PV} \
    file://accessory-server.service \
    file://keep.d/accessory-server \
"

S = "${WORKDIR}/git"

inherit cmake systemd pkgconfig

FILES:${PN} += " \
    ${base_libdir}/upgrade/keep.d \
    ${systemd_unitdir}/system/accessory-server.service \
"

DEPENDS = "mdns cjson mbedtls openssl curl util-linux gnutls nng systemd libevdev"
RDEPENDS:${PN} += "accessory-server-foss-dependencies"

EXTRA_OECMAKE += " \
  -DHAVE_MFI_HW_AUTH=${@oe.utils.conditional('HOMEKIT_HWAUTH','1','1','0',d)} \
  -DCMAKE_BUILD_TYPE=${@oe.utils.conditional('DEBUG_BUILD','1','Debug','RelWithDebInfo',d)} \
  -DHAP_LOG_LEVEL=${@oe.utils.conditional('DEBUG_BUILD','1','4','2',d)} \
"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/build/main/gardena-accessory-server ${D}${bindir}/
    install -m 0755 ${WORKDIR}/build/tools/provision/gardena-provision ${D}${bindir}/

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
