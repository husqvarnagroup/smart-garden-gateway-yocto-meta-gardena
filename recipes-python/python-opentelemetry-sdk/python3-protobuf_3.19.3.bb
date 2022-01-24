
SUMMARY = "Protocol Buffers"
HOMEPAGE = "https://developers.google.com/protocol-buffers/"
AUTHOR = " <>"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://LICENSE;md5=37b5762e07f0af8c74ce80a8bda4266b"

SRC_URI = "https://files.pythonhosted.org/packages/d9/d5/bf6c307f58b4c486f6517341d2f2673cd889b7d3a83cae78a9081233c679/protobuf-3.19.3.tar.gz"
SRC_URI[md5sum] = "4b50706c04d32073b5ebe570fe5c37d3"
SRC_URI[sha256sum] = "d975a6314fbf5c524d4981e24294739216b5fb81ef3c14b86fb4b045d6690907"

S = "${WORKDIR}/protobuf-3.19.3"

RDEPENDS_${PN} = ""

inherit setuptools3
