SUMMARY = "OpenTelemetry Python SDK"
HOMEPAGE = "https://github.com/open-telemetry/opentelemetry-python/tree/main/opentelemetry-sdk"
AUTHOR = "OpenTelemetry Authors <cncf-opentelemetry-contributors@lists.cncf.io>"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=5527ceb746f9fa9d6a341c4a813b4ce3"

inherit pypi python_hatchling

PYPI_PACKAGE = "opentelemetry_sdk"
SRC_URI[sha256sum] = "75bc0563affffa827700e0f4f4a68e1e257db0df13372344aebc6f8a64cde2e5"

RDEPENDS:${PN} = "python3-opentelemetry-api python3-opentelemetry-semantic-conventions"

