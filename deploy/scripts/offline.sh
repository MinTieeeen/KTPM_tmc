#!/usr/bin/env bash
# Ngắt Gate Agent của một nhà máy khỏi mạng WAN (demo D4, T5, T11).
# Dùng: deploy/scripts/offline.sh bd|la
set -euo pipefail
site="${1:?Cần chỉ định nhà máy: bd hoặc la}"
docker network disconnect tmc_wan "tmc-gate-${site}-1"
echo "gate-${site}: đã ngắt khỏi WAN"
