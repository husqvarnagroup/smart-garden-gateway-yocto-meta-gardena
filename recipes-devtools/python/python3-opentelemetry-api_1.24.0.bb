SUMMARY = "OpenTelemetry Python API"
HOMEPAGE = "https://github.com/open-telemetry/opentelemetry-python/tree/main/opentelemetry-api"
AUTHOR = "OpenTelemetry Authors <cncf-opentelemetry-contributors@lists.cncf.io>"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=5527ceb746f9fa9d6a341c4a813b4ce3"

inherit pypi python_hatchling

SRC_URI += "file://_importlib_metadata.py.patch"
PYPI_PACKAGE = "opentelemetry_api"
SRC_URI[sha256sum] = "42719f10ce7b5a9a73b10a4baf620574fb8ad495a9cbe5c18d76b75d8689c67e"

RDEPENDS:${PN} += "\
    python3-core (>= 3.7) \
    python3-deprecated \
"
