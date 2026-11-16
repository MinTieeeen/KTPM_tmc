# TMC – Bảng phân công

> Quy trình làm tài liệu và code của từng phần được mô tả trong [workflow.md](workflow.md). `<bước>` là một trong [01-analysis](01-analysis/), [02-spec](02-spec/), [03-api](03-api/), [04-test](04-test/).

| Vai trò | Họ và tên | MSSV | Phạm vi | Tài liệu liên quan |
|---|---|---|---|---|
| **BE-1 – Backend Gate Agent** | - | - | • `gate-agent/access` (G1)<br>• `gate-agent/turnstile` (G2)<br>• `gate-agent/localstore` (G3)<br>• `gate-agent/sync` (G4)<br>• `gate-agent/guard` (G5 rút gọn)<br>• `system`: Docker Compose, Dockerfile, CI | `<bước>/gate-agent/*`<br>`<bước>/system` |
| **BE-2 – Backend Central** | - | - | • `sync-contract`<br>• `central/ingestion` (C1)<br>• `central/employee` (C2)<br>• `central/station` (C3 tối thiểu)<br>• `central/iam` (C7)<br>• `simulators/turnstile-sim` | `<bước>/sync-contract`<br>`<bước>/central/*`<br>`<bước>/simulators/turnstile-sim` |
| **FE-1 – Frontend tại cổng** | - | - | • `web/shared`<br>• `web/kiosk` | `<bước>/web/shared`<br>`<bước>/web/kiosk` |
| **FE-2 – Frontend trung tâm** | - | - | • `web/auth`<br>• `web/portal`<br>• `web/admin` (rút gọn)<br>• Cấu hình build Vite | `<bước>/web/auth`<br>`<bước>/web/portal`<br>`<bước>/web/admin` |
| **DOC – Tài liệu và tích hợp báo cáo** | - | - | • Duyệt `01-analysis`<br>• Tổng hợp `04-test` vào phần 9 báo cáo<br>• README, video demo | [workflow.md](workflow.md)<br>`<bước>/system` |
