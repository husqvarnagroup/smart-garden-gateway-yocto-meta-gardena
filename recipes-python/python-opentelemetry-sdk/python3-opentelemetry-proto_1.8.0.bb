
SUMMARY = "OpenTelemetry Python Proto"
HOMEPAGE = "https://github.com/open-telemetry/opentelemetry-python/tree/main/opentelemetry-proto"
AUTHOR = "OpenTelemetry Authors <cncf-opentelemetry-contributors@lists.cncf.io>"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=5527ceb746f9fa9d6a341c4a813b4ce3"

SRC_URI = "https://files.pythonhosted.org/packages/1e/b9/2e041ab508a5394872dcf08414d4d3d228cbda21a6168ad0647549ffd124/opentelemetry-proto-1.8.0.tar.gz"
SRC_URI[md5sum] = "8aa2b9e71f51a11d3f4f2be5411565cf"
SRC_URI[sha256sum] = "7908c201b4eea566fa2b844e937b0ea76ca173b20ce8e1fc429099e8f5d0cc98"

S = "${WORKDIR}/opentelemetry-proto-1.8.0"

RDEPENDS:${PN} = "python3-protobuf"

inherit setuptools3
