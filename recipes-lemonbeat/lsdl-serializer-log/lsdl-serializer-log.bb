DESCRIPTION = "Logger for lsdl-serializer, to be used via LD_PRELOAD"
LICENSE = "CLOSED"

PV = "0.1"

PR = "r0"

DEPENDS = "lsdl-serializer-lib"

SRC_URI = "\
    file://lsdl-serializer-log.c \
"

S = "${WORKDIR}/"

do_compile() {
    ${CC} -fPIC -shared ${WORKDIR}/lsdl-serializer-log.c -o liblsdl-serializer-log.so -Wall -Wextra -Werror
}

do_install() {
    install -d ${D}${libdir}
    install -m 755 ${WORKDIR}/liblsdl-serializer-log.so ${D}${libdir}/liblsdl-serializer-log.so
}

FILES_${PN} = "\
    ${libdir}/liblsdl-serializer-log.so \
"

# We do not want a -dev package
FILES_${PN}-dev = ""
