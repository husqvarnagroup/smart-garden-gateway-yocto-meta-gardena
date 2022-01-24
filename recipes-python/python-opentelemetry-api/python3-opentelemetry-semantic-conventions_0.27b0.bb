
SUMMARY = "OpenTelemetry Semantic Conventions"
HOMEPAGE = "https://github.com/open-telemetry/opentelemetry-python/tree/main/opentelemetry-semantic-conventions"
AUTHOR = "OpenTelemetry Authors <cncf-opentelemetry-contributors@lists.cncf.io>"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=5527ceb746f9fa9d6a341c4a813b4ce3"

SRC_URI = "https://files.pythonhosted.org/packages/62/ad/766e36cb294a5cc226ac0d1ae0c56064a53d29ff4e3966d5c2f35179b5a2/opentelemetry-semantic-conventions-0.27b0.tar.gz"
SRC_URI[md5sum] = "d5eb081e932c4c64f0835ce8a09ff820"
SRC_URI[sha256sum] = "6f4cbef478c09056e76248a78cd2590dad6cd54f3854f713af155c82639e11eb"

S = "${WORKDIR}/opentelemetry-semantic-conventions-0.27b0"

RDEPENDS_${PN} = ""

inherit setuptools3
