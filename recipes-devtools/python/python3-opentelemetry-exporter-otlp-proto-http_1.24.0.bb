SUMMARY = "OpenTelemetry Collector Protobuf over HTTP Exporter"
HOMEPAGE = "https://github.com/open-telemetry/opentelemetry-python/tree/main/exporter/opentelemetry-exporter-otlp-proto-http"
AUTHOR = "OpenTelemetry Authors <cncf-opentelemetry-contributors@lists.cncf.io>"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=5527ceb746f9fa9d6a341c4a813b4ce3"

inherit pypi python_hatchling

PYPI_PACKAGE = "opentelemetry_exporter_otlp_proto_http"
SRC_URI[sha256sum] = "704c066cc96f5131881b75c0eac286cd73fc735c490b054838b4513254bd7850"

RDEPENDS:${PN} = " \
    python3-requests \
    python3-googleapis-common-protos \
    python3-opentelemetry-api \
    python3-opentelemetry-sdk \
    python3-opentelemetry-proto \
    python3-backoff \
"
