SUMMARY = "OpenTelemetry Semantic Conventions"
HOMEPAGE = "https://github.com/open-telemetry/opentelemetry-python/tree/main/opentelemetry-semantic-conventions"
AUTHOR = "OpenTelemetry Authors <cncf-opentelemetry-contributors@lists.cncf.io>"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=5527ceb746f9fa9d6a341c4a813b4ce3"

inherit pypi python_hatchling

PYPI_PACKAGE = "opentelemetry_semantic_conventions"
SRC_URI[sha256sum] = "7c84215a44ac846bc4b8e32d5e78935c5c43482e491812a0bb8aaf87e4d92118"

RDEPENDS:${PN} = "python3-opentelemetry-api python3-opentelemetry-semantic-conventions"
