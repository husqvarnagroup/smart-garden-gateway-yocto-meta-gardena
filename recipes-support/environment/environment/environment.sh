#!/bin/sh
#
# 1) Extract manufacturing step indicators from U-Boot and store them in the
#    file system. This allows systemd depending on the environment.service
#    units to make use of ConditionPathExists.
#
# 2) Extract X.509 client certificate and key from U-Boot.
#
# Since certificates files contain newlines and U-Boot variables can not contain
# such, newlines are encoded as the string "%".
#
# Precondition: The directories /etc/ssl/{certs,private} have to exist

set -eu -o pipefail

for var in fct_finalized eol_test_passed; do
    status="$(fw_printenv -n ${var} 2>/dev/null || echo 0)"
    if [ "${status}" = "1" ] || [ "$(uname -m)" = "armv5tejl" ]; then
        file="/etc/${var}"
        if [ ! -f ${file} ]; then
            touch ${file}
            sync
        fi
    fi
done

ssl_dir="/etc/ssl"
for ext in crt key; do
    if [ "${ext}" = crt ]; then
        file="${ssl_dir}/certs/client-prod.${ext}"
    else
        file="${ssl_dir}/private/client-prod.${ext}"
    fi

    if [ -s "${file}" ]; then
        echo "File '${file}' already exists and is not empty"
        if [ "${ext}" = "key" ] && [ "$(stat -c "%a" "${file}")" != "600" ]; then
            chmod 600 "${file}"
        fi
        continue
    fi

    if content="$(fw_printenv -n "x509_${ext}" 2>/dev/null)" ||
        content="$(fw_printenv -n "conf_openvpn_${ext}" 2>/dev/null)"
    then
        echo "${content}" | tr '%' '\n' > "${file}".tmp
        if [ "${ext}" = "key" ]; then
            chmod 600 "${file}".tmp
        fi
        sync
        mv "${file}".tmp "${file}"
    else
        echo "U-Boot variable 'x509_${ext}' is missing!" >&2
        # The x509_* variables are not set until IPR setup is done. Therefore,
        # only fail later in the manufacturing process.
        if fw_printenv ipr_setup_done >/dev/null 2>&1; then
            exit 1
        fi
    fi
done
