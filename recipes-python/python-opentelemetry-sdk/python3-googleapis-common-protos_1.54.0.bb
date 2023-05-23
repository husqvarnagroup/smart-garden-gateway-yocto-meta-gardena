
SUMMARY = "Common protobufs used in Google APIs"
HOMEPAGE = "https://github.com/googleapis/python-api-common-protos"
AUTHOR = "Google LLC <googleapis-packages@google.com>"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

SRC_URI = "https://files.pythonhosted.org/packages/97/94/e55c0151d6665a5ff7305fef38c7e8f1defa4679f884aaf9812fb42a1109/googleapis-common-protos-1.54.0.tar.gz"
SRC_URI[md5sum] = "2dcb2220c3e31f334febc13464b88d89"
SRC_URI[sha256sum] = "a4031d6ec6c2b1b6dc3e0be7e10a1bd72fb0b18b07ef9be7b51f2c1004ce2437"

S = "${WORKDIR}/googleapis-common-protos-1.54.0"

RDEPENDS:${PN} = "python3-protobuf"

inherit setuptools3
