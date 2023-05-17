SUMMARY = "Brave New World Cloudadapter FOSS dependencies"

inherit packagegroup

PR = "r0"

RDEPENDS:${PN} += " \
    python3-core \
    python3-typing \
    python3-pkg-resources \
    python3-ubootenv \
    python3-pkgutil \
    python3-pynng \
    python3-aiorun \
    python3-asyncinotify \
    python3-dbus-next \
    python3-json \
    python3-dictdiffer \
    python3-opentelemetry-api \
    python3-opentelemetry-semantic-conventions \
    aws-iot-device-sdk-python-v2 \
"
