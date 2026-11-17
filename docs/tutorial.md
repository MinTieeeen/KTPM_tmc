# Hướng dẫn sử dụng thư mục `docs/`

> Dành cho: mọi thành viên nhóm TMC – backend (BE-1, BE-2), frontend (FE-1, FE-2) và người viết tài liệu (R1, R2, R3, R4).
> Cập nhật: 18/11/2026.

Tài liệu này trả lời bốn câu hỏi: **trong `docs/` có gì**, **tôi cần đọc gì trước khi code**, **khi thay đổi
thiết kế thì sửa tài liệu nào**, và **cách sửa từng loại tài liệu (markdown, OpenAPI, sơ đồ draw.io)**.

---

## Mục lục

1. [Bản đồ thư mục](#1-bản-đồ-thư-mục)
2. [Đọc gì trước theo vai trò](#2-đọc-gì-trước-theo-vai-trò)
3. [Quy trình làm việc](#3-quy-trình-làm-việc)
4. [Đặc tả module – `spec/`](#4-đặc-tả-module--spec)
5. [Hợp đồng API – `api/`](#5-hợp-đồng-api--api)
6. [Màn hình web – `web/`](#6-màn-hình-web--web)
7. [Sơ đồ – `drawio/` và `images/`](#7-sơ-đồ--drawio-và-images)
8. [Dùng tài liệu khi code](#8-dùng-tài-liệu-khi-code)
9. [Kiểm thử và số liệu cho báo cáo](#9-kiểm-thử-và-số-liệu-cho-báo-cáo)
10. [Quy ước chung](#10-quy-ước-chung)
11. [Checklist trước khi tạo PR](#11-checklist-trước-khi-tạo-pr)
12. [Câu hỏi thường gặp](#12-câu-hỏi-thường-gặp)

---

## 1. Bản đồ thư mục

```
docs/
├── tutorial.md            ← tài liệu này
├── tmc-checklist.md       phân công: ai phụ trách phần nào
├── spec/                  SRS + thiết kế chi tiết của từng module backend (tiếng Việt)
│   ├── README.md          khung SRS, quy ước mã yêu cầu, danh mục module
│   └── <module>.md        11 file: sync-contract, gate-agent-*, central-*, turnstile-sim
├── api/                   hợp đồng REST API (tiếng Anh)
│   ├── README.md          quy ước chung, xác thực, mã lỗi, câu hiển thị, quy trình đổi hợp đồng
│   ├── <api>.md           bản cho người đọc: gate-agent, central, sync, turnstile-sim
│   └── <api>.openapi.yaml bản cho máy (OpenAPI 3.0.3): sinh type, mock, kiểm tra hợp đồng
├── web/                   giao diện web (tiếng Việt)
│   ├── sitemap.md         danh sách màn hình, điều hướng, phân quyền, sơ đồ frontend → backend
│   └── screens/           mỗi màn hình một file (K-xx, AU-xx, P-xx, A-xx) + README
├── drawio/                nguồn sơ đồ (mở bằng draw.io)
│   ├── class-diagram.drawio   sơ đồ lớp UML – mỗi module một tab
│   ├── web.drawio             tab "sitemap" và tab "frontend-backend"
│   └── wireframes.drawio      wireframe – mỗi màn hình một tab
└── images/                ảnh PNG xuất từ drawio/ (được nhúng trong các file .md)
    ├── class-diagram-<module>.png
    ├── web-sitemap.png, web-frontend-backend.png
    └── screens/<mã>-<tên>.png
```

| Thư mục | Nội dung | Ngôn ngữ | Người viết chính | Người duyệt |
|---|---|---|---|---|
| `spec/` | Yêu cầu (FR/NFR) và thiết kế (lớp, luồng, dữ liệu, cấu hình) của từng module backend | Việt | BE phụ trách module | BE còn lại |
| `api/` | Hợp đồng giữa frontend ↔ backend và giữa hai quantum | Anh | BE phụ trách server | Bên dùng API (§ 5.4) |
| `web/` | Danh sách màn hình và thiết kế từng màn hình | Việt | FE-1 (Kiosk), FE-2 (Portal, Admin) | BE cung cấp API của màn hình |
| `drawio/`, `images/` | Sơ đồ lớp, sitemap, component diagram, wireframe | Việt/Anh | Người sở hữu tài liệu chứa sơ đồ | Cùng người duyệt tài liệu đó |

**Nguồn gốc:** mọi tài liệu ở đây được rút ra từ báo cáo (`KTPM_report/`, mục 3 yêu cầu, 4 domain, 6 kiến trúc,
7 ADR, 8 nguyên tắc) và đặc tả triển khai M2 (`DacTa_TrienKhai_Code_M2.md`). **Khi mâu thuẫn, báo cáo là chuẩn**;
chỗ nào cố ý làm khác báo cáo thì ghi ở mục "Quyết định thiết kế và độ lệch" của spec tương ứng.

## 2. Đọc gì trước theo vai trò

| Vai trò | Đọc theo thứ tự |
|---|---|
| **BE-1** (Gate Agent) | [spec/README.md](spec/README.md) → [spec/sync-contract.md](spec/sync-contract.md) → `spec/gate-agent-*.md` (bắt đầu từ [access](spec/gate-agent-access.md)) → [api/gate-agent.md](api/gate-agent.md), [api/sync.md](api/sync.md), [api/turnstile-sim.md](api/turnstile-sim.md) → tab `gate-agent-*` của sơ đồ lớp |
| **BE-2** (Central) | [spec/README.md](spec/README.md) → [spec/sync-contract.md](spec/sync-contract.md) → `spec/central-*.md`, [spec/turnstile-sim.md](spec/turnstile-sim.md) → [api/central.md](api/central.md), [api/sync.md](api/sync.md) → tab `central-*` của sơ đồ lớp |
| **FE-1** (Kiosk) | [web/sitemap.md](web/sitemap.md) → `web/screens/K-*.md` → [api/README.md](api/README.md) (§ 2 quy ước, § 5 câu hiển thị) → [api/gate-agent.md](api/gate-agent.md) |
| **FE-2** (Portal, Admin) | [web/sitemap.md](web/sitemap.md) → `web/screens/AU-*`, `P-*`, `A-*` → [api/README.md](api/README.md) (§ 3 xác thực, § 4 mã lỗi) → [api/central.md](api/central.md) |
| **DOC** | Tài liệu này → [spec/README.md](spec/README.md) (khung, mục "độ lệch" của từng spec) → [api/README.md](api/README.md) → [web/sitemap.md](web/sitemap.md) → § 9 tài liệu này |

## 3. Quy trình làm việc

### 3.1. Thứ tự tài liệu → code

```
báo cáo + đặc tả M2
      │
      ▼
 spec/<module>.md ──────────► api/<api>.md + .openapi.yaml ──────────► web/sitemap.md + screens/
 (yêu cầu, lớp, luồng,          (hợp đồng: path, field,                  (màn hình, trạng thái,
  dữ liệu, cấu hình)             mã lỗi, ví dụ)                           thành phần, hành động)
      │                                │                                        │
      ▼                                ▼                                        ▼
 code backend                     type TS + mock MSW                        code frontend
      └──────────────────────────────────┬───────────────────────────────────────┘
                                         ▼
                        test theo § 8 của spec / screens  →  số liệu cho báo cáo (§ 9)
```

- Hợp đồng API là **điểm khóa**: khi file `api/` đã được duyệt, frontend làm song song với mock, không chờ backend.
- Code phải theo tài liệu. Nếu khi code phát hiện thiết kế cần đổi, **sửa tài liệu trong cùng PR** với code.

### 3.2. Một thay đổi phải sửa những tài liệu nào

| Thay đổi | Tài liệu phải sửa (cùng PR) |
|---|---|
| Thêm/đổi lớp, port, bảng, cấu hình trong một module | `spec/<module>.md` (§ 6) + tab sơ đồ lớp của module (`drawio/class-diagram.drawio` + PNG) |
| Đổi endpoint, field, mã lỗi | `api/<api>.md` **và** `api/<api>.openapi.yaml` + changelog `api/README.md` § 9; nếu màn hình bị ảnh hưởng: `web/screens/*.md` § 3, § 7 |
| Đổi DTO đồng bộ Q1 ↔ Q2 | `spec/sync-contract.md` + `api/sync.md` + `api/sync.openapi.yaml`; tăng `contractVersion` nếu không tương thích |
| Thêm/đổi màn hình | `web/sitemap.md` (§ 3 cây, § 4 bảng, § 5 điều hướng, § 6.1 ánh xạ API) + tab `sitemap` trong `drawio/web.drawio` + `web/screens/<mã>.md` + tab trong `drawio/wireframes.drawio` + PNG |
| Đổi câu hiển thị / mã lý do | `api/README.md` § 4–5 + `web/screens/K-02-scan-result.md` (hoặc màn hình liên quan) |
| Code khác báo cáo | Mục "Quyết định thiết kế và độ lệch" của spec + báo DOC để đưa vào phần 9 báo cáo |

### 3.3. Nhánh, commit, PR

- `main` luôn chạy được. Tài liệu: nhánh `docs/<phần>-<mô-tả>` (ví dụ `docs/api-central-errors`); code: `feat/<phần>-<mô-tả>`.
- Commit: `<phần>: <mô tả>` (ví dụ `api/central: add CONCURRENT_UPDATE`).
- Mỗi PR có ít nhất 1 người duyệt (bảng § 1); PR sửa `sync-contract` hoặc `api/sync.*` cần **cả BE-1 và BE-2**.
- Dòng đầu mỗi tài liệu ghi trạng thái: `Draft` → `Review` (đang có PR) → `Approved` (đã merge, đã duyệt).

## 4. Đặc tả module – `spec/`

**Mục đích:** mô tả đủ chi tiết để bước code chỉ còn là viết theo. Một file cho mỗi module backend (package
`gate-agent/<module>`, `central/<module>`, thư viện `sync-contract`, bộ giả lập `turnstile-sim`).

**Khung 8 mục** (giải thích đầy đủ ở [spec/README.md](spec/README.md) § 3):

| Mục | Viết gì | Ghi chú |
|---|---|---|
| 1. Giới thiệu | Mục đích, phạm vi (có / không), tài liệu tham chiếu | Ghi rõ phần báo cáo **không** làm |
| 2. Mô tả tổng quan | Vị trí trong kiến trúc, tác nhân, ràng buộc, giả định, phụ thuộc | |
| 3. Yêu cầu chức năng | Bảng `SRS-<MÃ>-FR-xx`, mức Must/Should/Could, truy vết FR/INV/ADR/DP | Mỗi dòng kiểm thử được |
| 4. Yêu cầu phi chức năng | `SRS-<MÃ>-NFR-xx` có số đo cụ thể (ms, %, số bản ghi) | |
| 5. Giao diện ngoài | REST (tóm tắt, chi tiết ở `api/`), port Java và hợp đồng hành vi, sự kiện | |
| 6. Thiết kế | 6.1 thành phần theo tầng · 6.2 danh sách lớp · 6.3 sơ đồ lớp · 6.4 luồng · 6.5 dữ liệu · 6.6 cấu hình `tmc.*` · 6.7 xử lý lỗi | Lớp mới so với báo cáo ghi "bổ sung" |
| 7. Quyết định thiết kế và độ lệch | Bảng `D1, D2…`: quyết định, lý do | DOC lấy mục này cho báo cáo |
| 8. Tiêu chí chấp nhận | Yêu cầu → test case `TC-<MÃ>-xx` → kịch bản T/D/S | |

**Mã phần** dùng trong mã yêu cầu và test: `SC`, `GA-ACCESS`, `GA-TURN`, `GA-STORE`, `GA-SYNC`, `GA-GUARD`,
`CE-ING`, `CE-EMP`, `CE-STA`, `CE-IAM`, `SIM-TS`.

**Thêm một module mới:** copy một spec có cấu trúc gần nhất → đổi tên file `<quantum>-<module>.md` → thêm dòng vào
bảng § 2 của [spec/README.md](spec/README.md) → thêm tab sơ đồ lớp (§ 7) → nếu có REST thì thêm hợp đồng ở `api/`.

## 5. Hợp đồng API – `api/`

### 5.1. Hai bản của mỗi API

| File | Dùng để | Ai đọc |
|---|---|---|
| `<api>.md` | Đọc và thống nhất: bảng field (kiểu, bắt buộc, khi nào có mặt), mã lỗi, quy tắc phía client | Người |
| `<api>.openapi.yaml` | Sinh type TypeScript, mock MSW, so sánh với `/v3/api-docs` của server | Công cụ |

Hai bản **phải giống nhau**; sửa cả hai trong cùng PR. Khi lỡ lệch, bản YAML là chuẩn cho công cụ, bản MD là chuẩn
khi thảo luận – sửa ngay bản còn lại.

### 5.2. Quy ước bắt buộc (chi tiết ở [api/README.md](api/README.md) § 2)

- JSON `camelCase`, enum `UPPER_SNAKE_CASE`, thời gian ISO-8601 UTC có `Z`, trường rỗng thì **bỏ hẳn** (không gửi `null`).
- Kết quả nghiệp vụ của `/scan` luôn HTTP 200 (đọc `result`); lỗi kỹ thuật dùng Problem Details RFC 9457 với
  trường `code` ổn định – client rẽ nhánh theo `code`, không theo `detail`.
- Câu hiển thị tiếng Việt cho người dùng cuối nằm trong [api/README.md](api/README.md) § 4–5 và là một phần hợp đồng.

### 5.3. Sửa một API

1. Sửa `<api>.md`: bảng field, ví dụ JSON, bảng lỗi, quy tắc client.
2. Sửa `<api>.openapi.yaml` tương ứng (schema, `required`, `enum`, `examples`).
3. Kiểm tra YAML hợp lệ (trong `web/`, cần `npm install` trước):
   ```bash
   npx openapi-typescript ../docs/api/central.openapi.yaml -o /tmp/check.ts   # lỗi cú pháp/$ref sẽ báo ngay
   npx @redocly/cli lint ../docs/api/*.openapi.yaml                           # lint đầy đủ (cần mạng lần đầu)
   ```
4. Thêm một dòng vào changelog [api/README.md](api/README.md) § 9 và tăng `info.version` trong YAML
   (thêm trường tùy chọn → minor; xóa/đổi tên/đổi nghĩa → major; với Sync API tăng thêm `contractVersion`).
5. Người duyệt theo bảng § 5.4.

> Lưu ý YAML: chuỗi có `: ` (dấu hai chấm + cách) phải đặt trong nháy đơn, ví dụ `description: 'Header: value'`.

### 5.4. Ai duyệt hợp đồng nào

| Hợp đồng | Người viết | Người duyệt |
|---|---|---|
| `gate-agent.*` | BE-1 | FE-1 |
| `central.*` | BE-2 | FE-2 |
| `sync.*` | BE-2 | BE-1 (và BE-2) |
| `turnstile-sim.*` | BE-2 | BE-1 |

## 6. Màn hình web – `web/`

### 6.1. `sitemap.md`

Là **mục lục** của giao diện: 3 trang (`kiosk.html`, `portal.html`, `admin.html`), cây màn hình, bảng danh mục
(mã, route, loại, mức, API, yêu cầu, link thiết kế), luồng guard, bảng chuyển màn hình, ma trận quyền và bảng ánh
xạ **màn hình → hàm `shared/api` → endpoint → controller backend**. Mã màn hình (`K-01`…) là khóa nối sitemap với
file trong `screens/`.

### 6.2. `screens/<mã>-<tên>.md`

| Mục | Nội dung |
|---|---|
| Đầu file | Mã, trang/route, loại (screen, overlay, dialog, tab, layout, mode, panel, state), mức, người dùng, thành phần code, yêu cầu |
| 1–6 | Mục đích, điều hướng (vào/rời), API, trạng thái màn hình, dữ liệu hiển thị, quy tắc hành vi |
| 7.1 Wireframe | Ảnh từ `drawio/wireframes.drawio`; **số đỏ** đánh dấu thành phần |
| 7.2 Thành phần giao diện | Mỗi số: tên, **loại** (button, input, select, table, badge, alert, toast…), dữ liệu và nguồn, cách hiển thị |
| 7.3 Hành động | Mã `<MÀN>-Ax`: input (thao tác/sự kiện) → xử lý/API → output |
| 8 Tiêu chí chấp nhận | Checklist `- [ ]` cho test UI và kịch bản demo |

**Thêm một màn hình:** chọn mã kế tiếp trong nhóm (`K-06`, `A-05`…) → thêm vào sitemap § 3, § 4, § 5, § 6.1 → thêm
hộp vào tab `sitemap` của `drawio/web.drawio` → copy một file trong `screens/` cùng loại, sửa nội dung → thêm tab
wireframe (§ 7.3) → xuất PNG → thêm dòng vào bảng của [screens/README.md](web/screens/README.md).

**Số chú thích:** số đỏ trên wireframe phải trùng cột `#` ở § 7.2; khi thêm thành phần, dùng số kế tiếp, không đánh
lại số cũ (tránh lệch với test và trao đổi trước đó).

## 7. Sơ đồ – `drawio/` và `images/`

### 7.1. Danh sách

| File nguồn | Tab | Ảnh xuất | Được nhúng ở |
|---|---|---|---|
| `drawio/class-diagram.drawio` | 11 tab, tên = module (`sync-contract`, `gate-agent-access`, …) | `images/class-diagram-<module>.png` | `spec/<module>.md` § 6.3 |
| `drawio/web.drawio` | `sitemap`, `frontend-backend` | `images/web-sitemap.png`, `images/web-frontend-backend.png` | `web/sitemap.md` § 3, § 6 |
| `drawio/wireframes.drawio` | 13 tab, tên = `<mã> <tên màn hình>` | `images/screens/<mã>-<tên>.png` | `web/screens/<mã>-<tên>.md` § 7.1 |

### 7.2. Quy ước vẽ

**Sơ đồ lớp (UML):**

| Ký hiệu | Ý nghĩa |
|---|---|
| Khung hình thư mục (package) | Một tầng của module: `<module>::api / application / domain / infrastructure`; nét đứt, nền xám = module khác hoặc thư viện |
| Màu lớp | **api** xanh dương · **application** xanh lá · **domain** vàng · **infrastructure** cam · ngoài module xám |
| `«interface»`, `«enumeration»`, `«record»`, `«Entity»`, `«Service»`… | Loại lớp / stereotype Spring |
| Nét đứt, mũi tam giác rỗng | Realization (lớp cài đặt interface) |
| Nét liền, mũi tam giác rỗng | Generalization (kế thừa lớp, interface kế thừa interface) |
| Nét đứt, mũi mở | Dependency (`«create»`, `«throw»`… khi cần) |
| Nét liền, mũi mở + bội số | Association; hình thoi đặc = composition |
| Thành viên gạch chân | `static` |
| `{ADMIN}`, `{transactional}`, `{scheduled}` | Ràng buộc / annotation |

- Không vẽ record/DTO (chỉ xuất hiện làm kiểu trong chữ ký), trừ `sync-contract`.
- Đường nối gãy khúc vuông góc; mỗi đường có điểm ra/vào riêng, không chồng lên đường khác; chỗ cắt nhau có cầu vượt.
- Lớp gọi đặt phía trên (hoặc bên trái) lớp được gọi; adapter đặt thẳng dưới port nó cài đặt.

**Wireframe:** low-fidelity, khung thiết bị (kiosk ngang 800×450, điện thoại 300×600, desktop 960×560); màu chỉ dùng
cho ý nghĩa (xanh = thành công, đỏ = lỗi/từ chối, cam = cảnh báo); số đỏ = thành phần ở § 7.2 của file màn hình.

### 7.3. Sửa sơ đồ

**Cách 1 – sửa tay (khuyến nghị cho thay đổi nhỏ):**
1. Mở file `.drawio` bằng [draw.io Desktop](https://github.com/jgraph/drawio-desktop/releases) hoặc extension VS Code
   *Draw.io Integration* (`hediet.vscode-drawio`); chọn đúng tab.
2. Sửa theo quy ước § 7.2, lưu.
3. Xuất PNG của tab đó (số tab tính từ 1):
   ```bash
   # Windows: "%LOCALAPPDATA%\Programs\draw.io\draw.io.exe"; macOS: /Applications/draw.io.app/Contents/MacOS/draw.io
   draw.io -x -f png -b 20 -p <số tab> -o docs/images/<ảnh>.png docs/drawio/<file>.drawio
   ```
   Hoặc trong draw.io: *File → Export as → PNG*, viền 20, chỉ trang hiện tại.
4. Commit cả `.drawio` và `.png`.

**Cách 2 – sinh lại bằng script (khi thay đổi lớn về bố cục):** các sơ đồ ban đầu được sinh bằng Python trong
`KTPM-261/artifacts/drawio/02_spec/` (**ngoài** repo `tmc`): `uml_layout.py` (thư viện bố cục), `class_diagrams.py`,
`web_diagrams.py`, `wireframes.py`.
```bash
python artifacts/drawio/02_spec/class_diagrams.py tmc/docs/drawio/class-diagram.drawio
python artifacts/drawio/02_spec/web_diagrams.py   tmc/docs/drawio/web.drawio
python artifacts/drawio/02_spec/wireframes.py     tmc/docs/drawio/wireframes.drawio
```
> ⚠ Chạy script sẽ **ghi đè** mọi chỉnh sửa tay trên file `.drawio` đó. Đã sửa tay thì tiếp tục sửa tay; chỉ dùng
> script khi đồng thời cập nhật dữ liệu trong script.

## 8. Dùng tài liệu khi code

### 8.1. Backend

| Cần biết | Lấy ở |
|---|---|
| Tên package, lớp, port, tầng | `spec/<module>.md` § 6.1–6.2 + sơ đồ lớp; package `com.tmc.<gate\|central>.<module>.<tầng>` |
| Luồng xử lý, thứ tự bước, transaction | `spec/<module>.md` § 6.4 |
| Bảng, cột, ràng buộc, migration | `spec/<module>.md` § 6.5 (Flyway: `V1_x__<module>.sql`, SQLite `V1__init.sql`) |
| Khóa cấu hình, giá trị mặc định | `spec/<module>.md` § 6.6 (`tmc.*`, không viết cứng – DP-17) |
| Endpoint, field, mã lỗi | `api/<api>.md`; springdoc phải sinh ra **đúng** `operationId`, path, schema, `required`, enum như file YAML |
| Lỗi trả về | `ProblemDetail` có thuộc tính `code`, kể cả 401/403 của Spring Security ([api/README.md](api/README.md) § 7) |
| Test cần viết | `spec/<module>.md` § 8 (`TC-<MÃ>-xx`) |

Kiểm tra hợp đồng khi server chạy: so `http://localhost:8080/v3/api-docs` (hoặc `:8081`) với file YAML – mỗi path,
schema thiếu hay khác là lệch hợp đồng.

### 8.2. Frontend

| Việc | Cách làm |
|---|---|
| Sinh type từ hợp đồng (không cần backend) | `cd web && npm run gen:api:docs` → `src/shared/api/gate.schema.ts`, `central.schema.ts` |
| Sinh type từ server đang chạy | `npm run gen:api` (kết quả phải tương đương bản trên) |
| Mock khi chưa có backend | MSW trong `src/mocks`, trả các `examples` của file YAML |
| Gọi API | Chỉ qua `src/shared/api` (`gateApi`, `centralApi`, `authSession`); ESLint chặn `fetch` trong component |
| Màn hình, trạng thái, thành phần | `web/screens/<mã>.md` § 4, § 7.2; đặt tên component theo cột "Thành phần code" ở đầu file |
| Câu chữ hiển thị | `api/README.md` § 4–5 → một bảng duy nhất `shared/ui` (`reasonTexts`) |
| Test UI | Mỗi dòng § 8 của file màn hình → ít nhất một test Vitest hoặc một bước demo |

Ghi mã màn hình trong comment đầu component (ví dụ `// K-02 Kết quả quét – docs/web/screens/K-02-scan-result.md`)
để tìm ngược từ code về thiết kế.

## 9. Kiểm thử và số liệu cho báo cáo

| Loại | Nguồn test case | Kết quả ghi ở |
|---|---|---|
| Unit / tích hợp / kiến trúc (backend) | `spec/<module>.md` § 8 (`TC-…`) | Báo cáo CI (JUnit, JaCoCo, ArchUnit) |
| UI (frontend) | `web/screens/<mã>.md` § 8 | Vitest |
| E2E, kịch bản demo T1–T11, D1–D6, S1–S3 | Đặc tả M2 mục 12 | `docs/test-report.md` (tạo khi bắt đầu chạy, mẫu dưới) |

Mẫu `docs/test-report.md`:

```markdown
| Ngày | Commit | Kịch bản | Kết quả | Số liệu | Ghi chú |
|---|---|---|---|---|---|
| 2026-11-25 | abc1234 | T1 quét hợp lệ | Đạt | elapsedMs p95 = 640 ms (50 lượt) | |
| 2026-11-25 | abc1234 | T5 offline 50 lượt | Đạt | 50 giao dịch mới ở Central, 0 trùng, outbox về 0 sau 6 s | |
```

**DOC** tổng hợp phần 9 của báo cáo từ: bảng `test-report.md` (số liệu T1, T2, T5, T9), mục "Quyết định thiết kế và
độ lệch" của mọi spec, danh sách "Màn hình ngoài MVP" ở [web/sitemap.md](web/sitemap.md) § 7, và các lớp/port đánh dấu
"bổ sung" trong spec (để cập nhật Bảng port ở mục 8 báo cáo).

## 10. Quy ước chung

| Chủ đề | Quy ước |
|---|---|
| Ngôn ngữ | `spec/`, `web/`, tài liệu này: tiếng Việt. `api/`: tiếng Anh. Tên lớp, field, endpoint, mã lỗi: giữ nguyên tiếng Anh. |
| Dòng trạng thái | Dòng đầu mỗi tài liệu: `> Trạng thái: Draft \| Review \| Approved · Người viết · Người duyệt · Ngày` |
| Mã truy vết | Báo cáo: `FR-xx`, `NFR-…`, `INV-xx`, `ADR-xx`, `DP-xx`. Spec: `SRS-<MÃ>-FR-xx`. Test: `TC-<MÃ>-xx`. Màn hình: `K-xx`, `AU-xx`, `P-xx`, `A-xx`; hành động `<MÀN>-Ax`. Kịch bản: `T1–T11`, `D1–D6`, `S1–S3`. |
| Tên file | Chữ thường, gạch nối: `gate-agent-access.md`, `class-diagram-central-iam.png`; màn hình `K-01-kiosk-idle.md` |
| Liên kết | Đường dẫn tương đối; khi đổi tên/di chuyển file phải sửa mọi liên kết tới nó |
| Bảng markdown | Ký tự gạch đứng nằm trong một ô phải viết thoát thành `\|`, nếu không bảng sẽ vỡ cột |
| Thời gian | Trong tài liệu kỹ thuật dùng UTC ISO-8601; trên giao diện hiển thị giờ Việt Nam |
| Bảo mật | Không ghi mật khẩu thật, khóa, token vào tài liệu; giá trị demo phải ghi rõ "demo" |
| Thuật ngữ | Giữ đúng tên trong báo cáo (component G1–G5, C1–C9, port, sự kiện); đổi tên thì ghi vào mục "độ lệch" |

## 11. Checklist trước khi tạo PR

- [ ] Đã sửa **mọi** tài liệu liên quan theo bảng § 3.2 (spec ↔ api ↔ web ↔ sơ đồ).
- [ ] Sơ đồ đã sửa thì đã xuất lại PNG và commit cả `.drawio` lẫn `.png`.
- [ ] Sửa API: hai bản `.md` và `.openapi.yaml` khớp nhau, YAML parse được (§ 5.3), có dòng changelog.
- [ ] Không có liên kết hỏng (mở bản xem trước markdown của VS Code/GitHub và bấm thử các link đã sửa).
- [ ] Bảng markdown không bị vỡ cột (thường do ký tự `|` trong ô).
- [ ] Dòng trạng thái và ngày ở đầu file đã cập nhật.
- [ ] Thay đổi khác báo cáo đã ghi vào mục "độ lệch" và báo DOC.
- [ ] Chọn đúng người duyệt (§ 1, § 5.4).

## 12. Câu hỏi thường gặp

**Báo cáo và spec nói khác nhau thì theo đâu?** Theo báo cáo; nếu spec cố ý khác thì phải có dòng trong mục "Quyết
định thiết kế và độ lệch" giải thích lý do. Không có dòng đó thì coi là lỗi của spec.

**Frontend cần một field mà API chưa có?** Không tự thêm vào mock. Mở PR sửa `api/<api>.md` + YAML, nhờ người viết
API duyệt; sau khi merge mới dùng.

**Chỉ sửa code, không đổi thiết kế, có cần sửa docs?** Không, trừ khi tên lớp/port/bảng/cấu hình/endpoint thay đổi.

**Thư mục `01-analysis`, `03-api`, `04-test` và `workflow.md` đâu rồi?** Đã bỏ: phân tích được gộp vào `spec/`
(mục 1–3, 7), hợp đồng nằm ở `api/`, test case nằm ở § 8 của spec và file màn hình, kết quả test ghi ở
`test-report.md` (§ 9).

**Sửa file `.drawio` trên GitHub được không?** Được (mở bằng app.diagrams.net → *Open from GitHub*), nhưng nhớ xuất
lại PNG theo § 7.3.
