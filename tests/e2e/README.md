# Kịch bản E2E (T1–T11)

Chạy trên hệ thống đã `docker compose up` (đặc tả mục 12). Mỗi kịch bản một script hoặc một lớp JUnit + RestAssured.

| ID | Kịch bản | File dự kiến |
|---|---|---|
| T1 | Quét hợp lệ → xanh, < 2 s | `t01_valid_scan` |
| T2 | Thu hồi QR → trạm từ chối ≤ 60 s | `t02_revoke` |
| T3 | Dùng lại token / token hết hạn | `t03_replay_expired` |
| T5 | Ngắt WAN, quét 50 lượt, nối lại | `t05_offline_sync` |
| T7 | Cổng xoay timeout | `t07_turnstile_timeout` |
| T8 | Kill -9 gate, khởi động lại | `t08_crash_recovery` |
| T10 | Anti-passback | `t10_anti_passback` |
| T11 | Mất kết nối quá ngưỡng | `t11_offline_threshold` |

T4, T6 và unit test nằm trong `src/test` của `gate-agent` / `central`.
