# TMC – Bảng phân công

> Quy trình làm việc: [workflow.md](workflow.md). Đặc tả (SRS) của từng module nằm trong [spec/](spec/README.md); sơ đồ lớp ở [drawio/](drawio/) (nguồn) và [images/](images/) (ảnh).

| Vai trò | Họ và tên | MSSV | Phạm vi | Tài liệu liên quan |
|---|---|---|---|---|
| **BE-1 – Backend Gate Agent** | - | - | • `gate-agent/access` (G1)<br>• `gate-agent/turnstile` (G2)<br>• `gate-agent/localstore` (G3)<br>• `gate-agent/sync` (G4)<br>• `gate-agent/guard` (G5 rút gọn)<br>• `system`: Docker Compose, Dockerfile, CI | `spec/gate-agent-*.md` |
| **BE-2 – Backend Central** | - | - | • `sync-contract`<br>• `central/ingestion` (C1)<br>• `central/employee` (C2)<br>• `central/station` (C3 tối thiểu)<br>• `central/iam` (C7)<br>• `simulators/turnstile-sim` | `spec/sync-contract.md`<br>`spec/central-*.md`<br>`spec/turnstile-sim.md` |
| **FE-1 – Frontend tại cổng** | - | - | • `web/shared`<br>• `web/kiosk` | — |
| **FE-2 – Frontend trung tâm** | - | - | • `web/auth`<br>• `web/portal`<br>• `web/admin` (rút gọn)<br>• Cấu hình build Vite | — |
| **DOC – Tài liệu và tích hợp báo cáo** | - | - | • Duyệt `spec/`<br>• Tổng hợp kết quả test vào phần 9 báo cáo<br>• README, video demo | [workflow.md](workflow.md)<br>[spec/README.md](spec/README.md) |
