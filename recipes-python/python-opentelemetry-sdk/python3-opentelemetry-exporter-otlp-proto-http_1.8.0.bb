
SUMMARY = "OpenTelemetry Collector Protobuf over HTTP Exporter"
HOMEPAGE = "https://github.com/open-telemetry/opentelemetry-python/tree/main/exporter/opentelemetry-exporter-otlp-proto-http"
AUTHOR = "OpenTelemetry Authors <cncf-opentelemetry-contributors@lists.cncf.io>"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=5527ceb746f9fa9d6a341c4a813b4ce3"

SRC_URI = "https://files.pythonhosted.org/packages/42/4e/887ba6235af5568f3dc60208512580b3f96f1dbcf7f99de4b51a1f626c7a/opentelemetry-exporter-otlp-proto-http-1.8.0.tar.gz"
SRC_URI[md5sum] = "81a90cc9ce7e53138b81f733698d3e38"
SRC_URI[sha256sum] = "ad6107569b9ba8d10e836c03bb66ac258864972a765c6518d0c89db755717ddb"

S = "${WORKDIR}/opentelemetry-exporter-otlp-proto-http-1.8.0"

RDEPENDS:${PN} = "python3-requests python3-googleapis-common-protos python3-opentelemetry-api python3-opentelemetry-sdk python3-opentelemetry-proto python3-backoff"

inherit setuptools3
