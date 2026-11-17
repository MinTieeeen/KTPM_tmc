# TMC – Hệ thống kiểm soát ra vào và chấm công bằng mã QR

Mã nguồn Milestone 2 của bài tập lớn Kiến trúc phần mềm (CO3017).

## Kiến trúc tóm tắt

- **Q1 Gate Quantum** – `gate-agent`: chạy tại mỗi nhà máy (Bình Dương, Long An), tự xác thực QR và ghi chấm công kể cả khi mất kết nối; SQLite + transactional outbox.
- **Q2 Central Quantum** – `central`: modular monolith (Spring Modulith) tại trung tâm; PostgreSQL; ký mã QR động Ed25519.
- Hai quantum chỉ giao tiếp qua `sync-contract` (REST/JSON, bất đồng bộ, idempotent).

## Ánh xạ component ↔ thư mục

| Component | Package | Thư mục | Port / interface chính |
|---|---|---|---|
| G1 Access Control | `access` | `gate-agent/src/main/java/com/tmc/gate/access` | `QrTokenVerifier`, `CredentialReader`, `ScanAttemptStore`, `TransactionStore`, `TurnstileGateway` |
| G2 Turnstile Adapter | `turnstile` | `gate-agent/.../gate/turnstile` | `HttpTurnstileAdapter`, `FakeTurnstileGateway` |
| G3 Local Store & Outbox | `localstore` | `gate-agent/.../gate/localstore` | `SqliteCredentialStore`, `SqliteScanStore`, `SqliteOutboxStore` |
| G4 Gate Sync Agent | `sync` | `gate-agent/.../gate/sync` | `SyncClient`, `OutboxStore`, `CredentialWriter` |
| G5 Guard Console (rút gọn) | `guard` | `gate-agent/.../gate/guard` | `GET /guard/pending` (đối soát lượt PENDING) |
| C1 Gate Sync & Ingestion | `ingestion` | `central/src/main/java/com/tmc/central/ingestion` | `SyncAPI`, `IngestionApi` |
| C2 Employee & QR Management | `employee` | `central/.../central/employee` | `QrTokenSigner`, `EmployeeApi`, `QrTokenAPI` |
| C3 Gate Station Management (tối thiểu) | `station` | `central/.../central/station` | `StationApi` |
| C7 IAM (đăng nhập, vai trò EMPLOYEE/ADMIN) | `iam` | `central/.../central/iam` | `AuthApi`, `/auth/login`, `/auth/me` |
| Hợp đồng Q1 ↔ Q2 | — | `sync-contract` | `EventBatch`, `BatchAck`, `SnapshotDelta`, `Heartbeat` |
| Giao diện | — | `web` | Kiosk (đóng gói vào gate-agent); Portal giả lập, Admin rút gọn (đóng gói vào central) |
| Bộ giả lập | — | `simulators/turnstile-sim` | — |

C4, C5, C6, C8, C9 (chốt kỳ, HR/Payroll, báo cáo, audit, cấu hình) có trong báo cáo nhưng **không thuộc phạm vi MVP**.

Tài khoản demo (seed): nhân viên `e0001` … `e0020`, quản trị `admin`; mật khẩu do migration seed quy định (xem `docs/api/README.md` § 3.1).

Bên trong mỗi package có 4 tầng `api / application / domain / infrastructure`. `domain` là Java thuần, không phụ thuộc Spring hay `infrastructure` (kiểm tra bằng ArchUnit và Spring Modulith trong CI).

## Cấu trúc thư mục

```
tmc/
├── pom.xml                  parent POM (Java 21, Spring Boot 3.5)
├── sync-contract/           thư viện DTO dùng chung
├── gate-agent/              Q1 – G1..G4, G5 rút gọn
├── central/                 Q2 – C1..C3, C7
├── simulators/              turnstile-sim
├── docs/                    phân công, quy trình; spec/ (SRS), api/ (OpenAPI), web/ (sitemap, màn hình), drawio/ + images/ (sơ đồ lớp)
├── web/                     React + TypeScript + Vite (hai build target: gate, central)
├── deploy/                  docker-compose.yml, Dockerfile, env, script demo
├── tests/                   e2e (T1–T11), load (k6)
└── .github/workflows/ci.yml
```

## Yêu cầu

- JDK 21+ (build với `release 21`), Maven 3.9+
- Node.js 22, npm 10
- Docker + Docker Compose v2
- Git Bash (Windows) để chạy script trong `deploy/scripts`

## Chạy khi phát triển

```bash
# Backend
mvn -q -DskipTests install                       # build toàn bộ
docker compose -f deploy/docker-compose.yml up -d postgres
mvn -pl central spring-boot:run -Dspring-boot.run.profiles=demo
mvn -pl simulators/turnstile-sim spring-boot:run
mvn -pl gate-agent spring-boot:run -Dspring-boot.run.profiles=demo

# Frontend (proxy tới :8081 và :8080, xem web/vite.config.ts)
cd web && npm install && npm run dev
# http://localhost:5173/kiosk.html?station=BD-IN-01 · /portal.html · /admin.html
```

## Chạy toàn bộ bằng Docker

```bash
docker compose -f deploy/docker-compose.yml up --build
```

| Dịch vụ | Địa chỉ |
|---|---|
| Central (Admin, Portal giả lập, API) | http://localhost:8080 – `/admin.html`, `/portal.html`, `/swagger-ui.html` |
| Gate Bình Dương (Kiosk) | http://localhost:8081 – `/kiosk.html?station=BD-IN-01`, `/status`, `/guard/pending` |
| Gate Long An | http://localhost:8082 |
| Turnstile sim BD / LA | http://localhost:9001, http://localhost:9002 |
| PostgreSQL | localhost:5432 (tmc/tmc) |

## Script demo

| Script | Mục đích | Kịch bản |
|---|---|---|
| `deploy/scripts/offline.sh bd` / `online.sh bd` | Ngắt / nối lại gate khỏi WAN | D4, T5, T11 |
| `deploy/scripts/kill-gate.sh bd` | Kill -9 rồi khởi động lại gate | D6, T8 |
| `deploy/scripts/sim.sh bd TIMEOUT` | Đổi hành vi cổng xoay giả lập | D5, T7 |
| `deploy/scripts/gen-keys.sh` | Sinh khóa Ed25519 (QR) và RSA (JWT) vào `deploy/env/secrets/` | — |

## Các bước demo D1–D6

_Bổ sung khi hoàn thành luồng MVP._

## Quy ước

- Nhánh `feat/<mã-gói>-mo-ta`; commit `<mã-gói>: <mô tả>`; PR sửa `sync-contract` cần cả hai người backend duyệt.
- Không viết cứng ngưỡng/timeout/chu kỳ (dùng `tmc.*` trong `application.yml`).
- Không commit secret: khóa nằm trong `deploy/env/secrets/` (đã gitignore). Giá trị trong `deploy/env/*.env` chỉ dành cho demo.
