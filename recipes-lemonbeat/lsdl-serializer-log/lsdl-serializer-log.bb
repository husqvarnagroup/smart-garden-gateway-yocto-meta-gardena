DESCRIPTION = "Logger for lsdl-serializer, to be used via LD_PRELOAD"
LICENSE = "CLOSED"

PV = "0.2"

PR = "r0"

DEPENDS = "lsdl-serializer-lib"

SRC_URI = "\
    file://lsdl-serializer-log.c \
    file://lsdl-serializer-log.service \
    file://lsdl-serializer-log.sh \
"

S = "${WORKDIR}/"

do_compile() {
    ${CC} ${CFLAGS} ${LDFLAGS} -fPIC -Os -ggdb -shared ${WORKDIR}/lsdl-serializer-log.c -o liblsdl-serializer-log.so -Wall -Wextra -Werror
}

do_install() {
    install -d ${D}${libdir}
    install -m 755 ${WORKDIR}/liblsdl-serializer-log.so ${D}${libdir}/liblsdl-serializer-log.so

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/lsdl-serializer-log.service ${D}${systemd_unitdir}/system/

    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/lsdl-serializer-log.sh ${D}${bindir}/lsdl-serializer-log
}

FILES_${PN} += "\
    ${libdir}/liblsdl-serializer-log.so \
"

# We do not want a -dev package as there would be nothing of interest
FILES_${PN}-dev = ""

inherit systemd
SYSTEMD_SERVICE_${PN} = "${PN}.service"
