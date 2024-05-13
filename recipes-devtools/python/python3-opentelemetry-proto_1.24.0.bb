SUMMARY = "OpenTelemetry Python Proto"
HOMEPAGE = "https://github.com/open-telemetry/opentelemetry-python/tree/main/opentelemetry-proto"
AUTHOR = "OpenTelemetry Authors <cncf-opentelemetry-contributors@lists.cncf.io>"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=5527ceb746f9fa9d6a341c4a813b4ce3"

inherit pypi python_hatchling

PYPI_PACKAGE = "opentelemetry_proto"
SRC_URI[sha256sum] = "ff551b8ad63c6cabb1845ce217a6709358dfaba0f75ea1fa21a61ceddc78cab8"

RDEPENDS:${PN} = "python3-protobuf"
