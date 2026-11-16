#!/usr/bin/env bash
# Tắt đột ngột (SIGKILL) rồi khởi động lại Gate Agent (T8 – khôi phục sau crash).
# Dùng: deploy/scripts/kill-gate.sh bd|la
set -euo pipefail
site="${1:?Cần chỉ định nhà máy: bd hoặc la}"
container="tmc-gate-${site}-1"
docker kill -s KILL "$container"
sleep 2
docker start "$container"
echo "gate-${site}: đã kill -9 và khởi động lại"
