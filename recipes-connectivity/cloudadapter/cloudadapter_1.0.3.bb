DESCRIPTION = "Brave New World Cloudadapter"
LICENSE = "GPL-3.0-only"

LIC_FILES_CHKSUM = " \
    file://${COMMON_LICENSE_DIR}/GPL-3.0-only;md5=c79ff39f19dfec6d293b95dea7b07891 \
"

inherit python_poetry_core

SRCREV = "7e60f0ab4337dfe17e9812c2207f396056145850"
SRC_URI += " \
    gitsm://git@ssh.dev.azure.com/v3/HQV-Gardena/SG-Gateway/sg-bnw-cloud-adapter;protocol=ssh;branch=main \
    file://cloudadapter.service \
    file://aws-root-ca.crt \
    file://keep.d/cloudadapter \
"

PR = "r1"

DEPENDS = " \
    python3 \
    python3-poetry-core \
    python3-poetry-core-native \
    virtual/crypt \
"

RDEPENDS:${PN} += " \
    cloudadapter-foss-dependencies \
"


FILES:${PN} += " \
    ${localstatedir}/lib/${PN} \
    ${base_libdir}/upgrade/keep.d \
"

do_install:append() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/cloudadapter.service ${D}${systemd_unitdir}/system

    install -d ${D}${sysconfdir}/ssl/certs
    install -m 0644 ${UNPACKDIR}/aws-root-ca.crt ${D}${sysconfdir}/ssl/certs

    # Create work directory for cloudadapter
    install -d ${D}${localstatedir}/lib/${PN}

    # Retain persisted data
    install -d ${D}${base_libdir}/upgrade/keep.d
    install -m 0644 ${UNPACKDIR}/keep.d/cloudadapter ${D}${base_libdir}/upgrade/keep.d
}

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = " \
    cloudadapter.service \
"
