LICENSE = "CLOSED"

SRC_URI = "git://stash.dss.husqvarnagroup.com/scm/sg/smart-garden-emc-testing-tools.git;protocol=https"
SRCREV = "9d9480ca5f7082affb6ee9559c03a7c6eb4a0c6c"

PR = "r0"
PV = "1.0+git${SRCPV}"

S = "${WORKDIR}/git"

FILES_${PN} += " \
    ${libdir}/python3.5/site-packages/emc_testing/* \
    /tmp/gateway_autoconfig.sh \
"

RDEPENDS_emc-tools += " \
    python3-core \
    python3-misc \
    python3-xml \
    python3-threading \
    python3-netclient \
    python3-netserver \
"

do_install () {
    # scripts
    install -d ${D}${bindir}
    install -m 0755 ${S}/battery.py ${D}${bindir}/battery
    install -m 0755 ${S}/ic24.py ${D}${bindir}/ic24
    install -m 0755 ${S}/power.py ${D}${bindir}/power
    install -m 0755 ${S}/sensor.py ${D}${bindir}/sensor
    install -m 0755 ${S}/watercontrol.py ${D}${bindir}/watercontrol

    # module
    install -d ${D}${libdir}/python3.5/site-packages/emc_testing
    # TODO should we put config.py outside of site-packages?
    install -m 0755 ${S}/emc_testing/config.py ${D}${libdir}/python3.5/site-packages/emc_testing/config.py
    install -m 0755 ${S}/emc_testing/error_codes.py ${D}${libdir}/python3.5/site-packages/emc_testing/error_codes.py
    install -m 0755 ${S}/emc_testing/lbtool.py ${D}${libdir}/python3.5/site-packages/emc_testing/lbtool.py
    install -m 0755 ${S}/emc_testing/status_level.py ${D}${libdir}/python3.5/site-packages/emc_testing/status_level.py

    # lsdl-serializer
    # TODO, once we have it for MIPS architecture

    # script for initial configuration
    install -d ${D}/tmp
    install -m 0755 ${S}/scripts/gateway_autoconfig.sh ${D}/tmp/gateway_autoconfig.sh

    # symlink for lbtool
    cd ${D}${bindir}
    ln -s -r ${D}${libdir}/python3.5/site-packages/lbtool.py ${D}${bindir}/lbtool
}

pkg_postinst_${PN} () {
#!/bin/sh
    # TODO this might not be the best way to do this; tmp file probably should not be in package
    /tmp/gateway_autoconfig.sh /usr/lib/python3.5/site-packages/emc_testing/config.py
    rm /tmp/gateway_autoconfig.sh
}
