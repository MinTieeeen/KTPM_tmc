# TMC – Bảng phân công

> Quy trình làm việc: [workflow.md](workflow.md). Đặc tả (SRS) của từng module nằm trong [spec/](spec/README.md); đặc tả API trong [api/](api/README.md); sitemap và màn hình web trong [web/sitemap.md](web/sitemap.md); sơ đồ lớp ở [drawio/class-diagram.drawio](drawio/class-diagram.drawio) (mỗi module một tab) và [images/](images/) (ảnh).

| Vai trò | Họ và tên | MSSV | Phạm vi | Tài liệu liên quan |
|---|---|---|---|---|
| **BE-1 – Backend Gate Agent** | Huỳnh Minh Tiến | 2313425 | • `gate-agent/access` (G1)<br>• `gate-agent/turnstile` (G2)<br>• `gate-agent/localstore` (G3)<br>• `gate-agent/sync` (G4)<br>• `gate-agent/guard` (G5 rút gọn)<br>• `system`: Docker Compose, Dockerfile, CI | `spec/gate-agent-*.md` |
| **BE-2 – Backend Central** | Nguyễn Ngọc Trúc Quỳnh | 2312912 | • `sync-contract`<br>• `central/ingestion` (C1)<br>• `central/employee` (C2)<br>• `central/station` (C3 tối thiểu)<br>• `central/iam` (C7)<br>• `simulators/turnstile-sim` | `spec/sync-contract.md`<br>`spec/central-*.md`<br>`spec/turnstile-sim.md` |
| **FE-1 – Frontend tại cổng** | Phạm Ngọc Tây | 2313063 | • `web/shared`<br>• `web/kiosk` | — |
| **FE-2 – Frontend trung tâm** | Nguyễn Đức Nghĩa | 2312266 | • `web/auth`<br>• `web/portal`<br>• `web/admin` | — |