DESCRIPTION = "Reset Radio Module service"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PV = "1.0"

PR = "r0"

SRC_URI = "\
    file://reset-rm.c \
    file://reset-rm.service \
    file://reset-rm.cfg \
"

S = "${WORKDIR}/"

DEPENDS += " \
    libcyaml \
    libgpiod \
"

RDEPENDS_${PN} += " \
    libcyaml \
    libgpiod \
"

do_compile() {
    ${CC} ${CFLAGS} ${LDFLAGS} ${WORKDIR}/reset-rm.c -lcyaml -lyaml -lgpiod -o reset-rm -Wall -Wextra -Wpedantic -Werror
}

do_install() {
    install -d ${D}${bindir}
    install -m 755 ${WORKDIR}/reset-rm ${D}${bindir}/

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/reset-rm.service ${D}${systemd_unitdir}/system

    install -d ${D}${sysconfdir}
    install -m 0644 ${WORKDIR}/reset-rm.cfg ${D}${sysconfdir}/reset-rm.cfg
}

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE_${PN} = "reset-rm.service"
