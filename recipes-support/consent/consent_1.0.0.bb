DESCRIPTION = "Allow the user to provide consent for support data collection on the gateway."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

FILESEXTRAPATHS:append := "${THISDIR}/files:"

SRC_URI = "\
    file://keep.d/consent \
"

do_install() {
    # We will remove these files from the image eventually. Then they will be written only
    # if customer opt's in to allow us to collect support data.
    install -d ${D}${sysconfdir}/
    touch ${D}${sysconfdir}/consent-to-telemetry
    touch ${D}${sysconfdir}/consent-to-support-bundle

    # Keep the files after update if the client allows us to collect data.
    install -d ${D}${base_libdir}/upgrade/keep.d
    install -m 0644 ${UNPACKDIR}/keep.d/consent ${D}${base_libdir}/upgrade/keep.d
}

FILES:${PN} += " \
    ${sysconfdir}/consent-to-telemetry \
    ${sysconfdir}/consent-to-support-bundle \
    ${base_libdir}/upgrade/keep.d \
"

inherit allarch
