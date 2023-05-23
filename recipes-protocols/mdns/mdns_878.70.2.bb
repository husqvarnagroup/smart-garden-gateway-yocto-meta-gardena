SUMMARY = "Publishes & browses available services on a link according to the Zeroconf / Bonjour protocol"
DESCRIPTION = "Bonjour, also known as zero-configuration networking, enables automatic discovery of computers, devices, and services on IP networks."
HOMEPAGE = "http://developer.apple.com/networking/bonjour/"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://../LICENSE;md5=31c50371921e0fb731003bbc665f29bf"

PR = "r1"

RPROVIDES:${PN} += "libdns_sd.so"

SRC_URI = "https://github.com/apple-oss-distributions/mDNSResponder/archive/refs/tags/mDNSResponder-${PV}.tar.gz \
           file://build.patch;patchdir=.. \
           file://mdns.service \
           file://0001-nss_mdns-Do-not-include-nss.h-when-libc-glibc.patch;patchdir=.. \
           file://0002-Don-t-call-memcpy-with-size-src.patch;patchdir=.. \
           file://0003-Don-t-enable-MDNS-on-non-multicast-non-broadcast-int.patch;patchdir=.. \
           file://0004-mDNS-Support-interface-names-larger-than-8-character.patch;patchdir=.. \
           file://0005-mDNSCore-fix-switch-fallthrough-warnings.patch;patchdir=.. \
           file://0006-Clients-fix-misleading-indentation-warning.patch;patchdir=.. \
           file://0007-mDNSCore-fix-Warray-bounds-warnings-for-HMAC_MD5_Alg.patch;patchdir=.. \
           "

SRC_URI[md5sum] = "7410c046a775113aa397dc215a4a5964"
SRC_URI[sha256sum] = "7eb6b5ffc16ef26a9ad50dac17615a09ea3fc228186bed3f8c55de320eda8f3e"

PARALLEL_MAKE = ""

S = "${WORKDIR}/mDNSResponder-mDNSResponder-${PV}/mDNSPosix"

EXTRA_OEMAKE += "os=linux DEBUG=0 'CC=${CC}' 'LD=${CCLD} ${LDFLAGS}'"

TARGET_CC_ARCH += "${LDFLAGS}"

do_install () {
    install -d ${D}${sbindir}
    install -m 0755 build/prod/mdnsd ${D}${sbindir}

    install -d ${D}${libdir}
    cp build/prod/libdns_sd.so ${D}${libdir}/libdns_sd.so.1
    chmod 0644 ${D}${libdir}/libdns_sd.so.1
    ln -s libdns_sd.so.1 ${D}${libdir}/libdns_sd.so

    install -d ${D}${includedir}
    install -m 0644 ../mDNSShared/dns_sd.h ${D}${includedir}

    install -d ${D}${mandir}/man8
    install -m 0644 ../mDNSShared/mDNSResponder.8 ${D}${mandir}/man8/mdnsd.8

    install -d ${D}${bindir}
    install -m 0755 ../Clients/build/dns-sd ${D}${bindir}

    install -d ${D}${libdir}
    oe_libinstall -C build/prod -so libnss_mdns-0.2 ${D}${libdir}
    ln -s libnss_mdns-0.2.so ${D}${libdir}/libnss_mdns.so.2

    install -d ${D}${sysconfdir}
    install -m 0644 nss_mdns.conf ${D}${sysconfdir}

    install -d ${D}${mandir}/man5
    install -m 0644 nss_mdns.conf.5 ${D}${mandir}/man5

    install -d ${D}${mandir}/man8
    install -m 0644 libnss_mdns.8 ${D}${mandir}/man8

    install -d ${D}${systemd_unitdir}/system/
    install -m 0644 ${WORKDIR}/mdns.service ${D}${systemd_unitdir}/system/
}

pkg_postinst:${PN} () {
    sed -e '/^hosts:/s/\s*\<mdns\>//' \
        -e 's/\(^hosts:.*\)\(\<files\>\)\(.*\)\(\<dns\>\)\(.*\)/\1\2 mdns\3\4\5/' \
        -i $D/etc/nsswitch.conf
}

pkg_prerm:${PN} () {
    sed -e '/^hosts:/s/\s*\<mdns\>//' \
        -e '/^hosts:/s/\s*mdns//' \
        -i $D/etc/nsswitch.conf
}

inherit systemd

SYSTEMD_SERVICE:${PN} = "mdns.service"

FILES:${PN} += "${systemd_unitdir}/system/mdns.service"
FILES:${PN} += "${libdir}/libdns_sd.so.1 \
                ${bindir}/dns-sd \
                ${libdir}/libnss_mdns-0.2.so \
                ${sysconfdir}/nss_mdns.conf"

FILES:${PN}-dev += "${libdir}/libdns_sd.so \
                    ${includedir}/dns_sd.h "

FILES:${PN}-man += "${mandir}/man8/mdnsd.8 \
                    ${mandir}/man5/nss_mdns.conf.5 \
                    ${mandir}/man8/libnss_mdns.8"

PACKAGES = "${PN} ${PN}-dev ${PN}-man ${PN}-dbg"
