#!/usr/bin/env bash
# Nối lại Gate Agent vào mạng WAN.
# Dùng: deploy/scripts/online.sh bd|la
set -euo pipefail
site="${1:?Cần chỉ định nhà máy: bd hoặc la}"
docker network connect --alias "gate-${site}" tmc_wan "tmc-gate-${site}-1"
echo "gate-${site}: đã nối lại WAN"
