
SUMMARY = "OpenTelemetry Python API"
HOMEPAGE = "https://github.com/open-telemetry/opentelemetry-python/tree/main/opentelemetry-api"
AUTHOR = "OpenTelemetry Authors <cncf-opentelemetry-contributors@lists.cncf.io>"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=5527ceb746f9fa9d6a341c4a813b4ce3"

SRC_URI = "https://files.pythonhosted.org/packages/75/87/10798069493919e264e392246f8dca3abe2475c50c637e382e62b8af5e03/opentelemetry-api-1.8.0.tar.gz"
SRC_URI[md5sum] = "eea598967000eb455eccb7e8ca4b8a1a"
SRC_URI[sha256sum] = "758f73610c08a03e9c8f29cfb364bc75ce35e9a62136ba98b82423d3d27498f5"

S = "${WORKDIR}/opentelemetry-api-1.8.0"

RDEPENDS:${PN} = "python3-deprecated python3-pkg-resources"

inherit setuptools3
