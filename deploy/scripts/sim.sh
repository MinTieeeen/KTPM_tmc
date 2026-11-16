#!/usr/bin/env bash
# Đổi hành vi của bộ giả lập cổng xoay (T7).
# Dùng: deploy/scripts/sim.sh bd|la NORMAL|TIMEOUT|ERROR|EXECUTE_THEN_TIMEOUT [failureRate]
set -euo pipefail
site="${1:?Cần chỉ định nhà máy: bd hoặc la}"
mode="${2:?Cần chỉ định chế độ}"
rate="${3:-0.0}"
case "$site" in
  bd) port=9001 ;;
  la) port=9002 ;;
  *) echo "Nhà máy không hợp lệ: $site" >&2; exit 1 ;;
esac
curl -sS -X PUT "http://localhost:${port}/admin/behavior" \
  -H 'Content-Type: application/json' \
  -d "{\"latencyMs\":[300,500],\"failureRate\":${rate},\"mode\":\"${mode}\"}"
echo
