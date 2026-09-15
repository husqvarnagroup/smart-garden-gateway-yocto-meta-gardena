DESCRIPTION = "Reset Radio Module service"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PV = "1.1"

PR = "r1"

SRC_URI = "\
    file://reset-rm.c \
    file://reset-rm.service \
    file://reset-rm.cfg \
"

S = "${UNPACKDIR}"

DEPENDS += " \
    libcyaml \
    libgpiod \
"

RDEPENDS:${PN} += " \
    libcyaml \
    libgpiod \
"

do_compile() {
    ${CC} ${CFLAGS} ${LDFLAGS} ${UNPACKDIR}/reset-rm.c -lcyaml -lyaml -lgpiod -o reset-rm -Wall -Wextra -Wpedantic -Werror
}

do_install() {
    install -d ${D}${bindir}
    install -m 755 ${UNPACKDIR}/reset-rm ${D}${bindir}/
    ln -s reset-rm ${D}${bindir}/rm-reset

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/reset-rm.service ${D}${systemd_unitdir}/system

    install -d ${D}${sysconfdir}
    install -m 0644 ${UNPACKDIR}/reset-rm.cfg ${D}${sysconfdir}/reset-rm.cfg
}

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = "reset-rm.service"
