
SUMMARY = "OpenTelemetry Python SDK"
HOMEPAGE = "https://github.com/open-telemetry/opentelemetry-python/tree/main/opentelemetry-sdk"
AUTHOR = "OpenTelemetry Authors <cncf-opentelemetry-contributors@lists.cncf.io>"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=5527ceb746f9fa9d6a341c4a813b4ce3"

SRC_URI = "https://files.pythonhosted.org/packages/e3/6c/ef5b272ba79c9690dff2250d073afe616cb4ee726e42e98cd0452cd715ea/opentelemetry-sdk-1.8.0.tar.gz"
SRC_URI[md5sum] = "681c23748730bd86833bf66c6a8c78eb"
SRC_URI[sha256sum] = "7536c81f348e6c88c9dce4cd6eb5b09b422902bde8f76d7a0d4be2a0b9684b3f"

S = "${WORKDIR}/opentelemetry-sdk-1.8.0"

RDEPENDS:${PN} = "python3-opentelemetry-api python3-opentelemetry-semantic-conventions"

inherit setuptools3
