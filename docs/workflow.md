# Quy trình làm việc: analysis → spec → api → test

> Áp dụng cho mọi phần của repo `tmc`. Phân công xem ở [tmc-checklist.md](tmc-checklist.md).

---

## 1. Ý đồ

Mỗi phần của hệ thống (một package của `gate-agent`, một module của `central`, một màn hình của `web`, ...) đều đi qua **bốn bước tài liệu theo thứ tự cố định**, rồi mới đến bước code:

```
01-analysis  ──►  02-spec  ──►  03-api  ──►  04-test  ──►  code  ──►  04-test (kết quả)
 hiểu đúng        thiết kế      chốt hợp      viết test      hiện       chạy test, ghi số liệu
 báo cáo          chi tiết      đồng với      case trước     thực       → phần 9 của báo cáo
                                người khác    khi code
```

Lý do chọn cách làm này:

1. **Code khớp báo cáo.** Bước analysis bắt mỗi người đọc lại phần báo cáo liên quan và trích đúng component, yêu cầu, bất biến, ADR trước khi thiết kế. Những gì code làm khác báo cáo sẽ lộ ra ngay ở bước này thay vì đến lúc viết phần 9.
2. **Làm độc lập.** Bước api là **hợp đồng giữa các thành viên**. Khi hợp đồng của một phần đã được duyệt, người dùng nó (frontend, hoặc backend bên kia) có thể làm song song bằng mock mà không phải chờ code.
3. **Test có trước code.** Test case viết từ yêu cầu và hợp đồng, không viết từ code đã có, nên kiểm tra được đúng yêu cầu của đề bài (≤ 2 giây, không mất dữ liệu, chạy khi mất kết nối, QR an toàn).
4. **Có sẵn tư liệu cho báo cáo.** `04-test` lưu kết quả và số liệu đo; `02-spec` lưu các quyết định nhỏ. Phần 9 và Reflection của báo cáo được tổng hợp trực tiếp từ đây.
5. **Nhìn theo bước.** Vì mỗi bước là một thư mục chung, mở `03-api/` là thấy toàn bộ hợp đồng của hệ thống, mở `04-test/` là thấy toàn bộ test case. Nhờ vậy dễ duyệt chéo và dễ biết bước nào của phần nào còn trống.

---

## 2. Cấu trúc thư mục `docs/`

Cấp đầu tiên là **4 bước**. Trong mỗi bước, các thư mục con **phản chiếu đúng cấu trúc mã nguồn**, mỗi phần một thư mục chứa file markdown của phần đó.

```
docs/
├── tmc-checklist.md                bảng phân công
├── workflow.md                     tài liệu này
├── 01-analysis/
│   ├── system/                     phần xuyên suốt (mục 4)
│   ├── sync-contract/
│   ├── gate-agent/
│   │   ├── access/                 G1
│   │   ├── turnstile/              G2
│   │   ├── localstore/             G3
│   │   ├── sync/                   G4
│   │   └── guard/                  G5 (rút gọn)
│   ├── central/
│   │   ├── ingestion/              C1
│   │   ├── employee/               C2
│   │   ├── station/                C3 (tối thiểu)
│   │   └── iam/                    C7 (đăng nhập, vai trò EMPLOYEE/ADMIN)
│   ├── web/
│   │   ├── shared/
│   │   ├── auth/                   màn hình đăng nhập dùng chung cho Portal và Admin
│   │   ├── kiosk/
│   │   ├── portal/
│   │   └── admin/                  (rút gọn)
│   └── simulators/
│       └── turnstile-sim/
├── 02-spec/                        cùng cấu trúc con như 01-analysis
├── 03-api/                         cùng cấu trúc con như 01-analysis
└── 04-test/                        cùng cấu trúc con như 01-analysis
```

Ví dụ, tài liệu của G1 `access` gồm 4 file ở 4 nơi:

```
docs/01-analysis/gate-agent/access/analysis.md
docs/02-spec/gate-agent/access/spec.md
docs/03-api/gate-agent/access/api.md
docs/04-test/gate-agent/access/test-plan.md   (+ test-report.md)
```

| Phần (thư mục con trong mỗi bước) | Thư mục code | Người phụ trách | Mức |
|---|---|---|---|
| `sync-contract` | `sync-contract/` | BE-2 | Must |
| `gate-agent/access`, `turnstile`, `localstore`, `sync` | `gate-agent/src/main/java/com/tmc/gate/<module>` | BE-1 | Must |
| `gate-agent/guard` | `.../gate/guard` (chỉ `GET /guard/pending`) | BE-1 | Should (MVP) |
| `central/ingestion`, `employee`, `station`, `iam` | `central/src/main/java/com/tmc/central/<module>` | BE-2 | Must |
| `web/shared`, `web/kiosk` | `web/src/shared`, `web/src/features/kiosk` | FE-1 | Must |
| `web/auth`, `web/portal` | `web/src/features/auth`, `web/src/features/portal` | FE-2 | Must |
| `web/admin` | `web/src/features/admin` (tab Nhân viên, tab Trạm) | FE-2 | Should (MVP) |
| `simulators/turnstile-sim` | `simulators/turnstile-sim` | BE-2 | Must |
| `system` | `deploy/`, `tests/` | BE-1 (hạ tầng), DOC (kịch bản demo) | Must |

Các component C4, C5, C6, C8, C9 (chốt kỳ, HR/Payroll, báo cáo, audit, cấu hình), Guard Console UI và `hr-sim` **nằm ngoài phạm vi MVP** nên không có thư mục. Lý do không hiện thực được ghi một lần trong `01-analysis/system/analysis.md`.

---

## 3. Bốn bước

Trong mục này, `<phần>` là đường dẫn của phần theo bảng ở mục 2, ví dụ `gate-agent/access`.

### 3.1. `01-analysis` – Hiểu đúng báo cáo

| | |
|---|---|
| **Mục tiêu** | Xác định phần này phải làm gì **theo báo cáo**, và phạm vi demo cắt đến đâu |
| **Đầu vào** | Báo cáo (mục 3 yêu cầu, 4 domain, 6 kiến trúc, 7 ADR, 8 nguyên tắc), đặc tả triển khai M2 |
| **Đầu ra** | `01-analysis/<phần>/analysis.md` |
| **Người viết / duyệt** | Người phụ trách phần / DOC (đối chiếu với báo cáo) |

Nội dung bắt buộc của `analysis.md`:
1. **Component** trong báo cáo (mã G/C, tên, trách nhiệm, dữ liệu sở hữu) và **package** tương ứng.
2. **Yêu cầu được truy vết:** FR, NFR, INV, ADR, DP liên quan, mỗi mục một dòng, ghi rõ mục của báo cáo.
3. **Phạm vi demo:** việc nào Must / Should / Could / Won't; phần nào của báo cáo không làm và lý do.
4. **Phụ thuộc:** phần này dùng gì của phần khác và cung cấp gì cho phần khác (interface, sự kiện, endpoint).
5. **Liên hệ với hai vấn đề của đề bài** (mất kết nối, mã QR tĩnh), nếu có.
6. **Câu hỏi mở và rủi ro:** điểm báo cáo chưa nói rõ, cần nhóm quyết định.

**Điều kiện chuyển bước:** DOC xác nhận không mâu thuẫn với báo cáo; câu hỏi mở đã có câu trả lời hoặc đã được ghi nhận là giả định.

### 3.2. `02-spec` – Thiết kế chi tiết

| | |
|---|---|
| **Mục tiêu** | Thiết kế đủ chi tiết để bước code chỉ còn là viết theo |
| **Đầu vào** | `01-analysis/<phần>/analysis.md` đã duyệt |
| **Đầu ra** | `02-spec/<phần>/spec.md` (hình, nếu có, đặt cùng thư mục: nguồn `.drawio` + ảnh `.png`) |
| **Người viết / duyệt** | Người phụ trách / người cùng nhóm (BE-1 ↔ BE-2, FE-1 ↔ FE-2) |

Nội dung bắt buộc:
- **Backend:** lớp theo 4 tầng `api / application / domain / infrastructure`; port và adapter (port nào cài đặt bởi lớp nào); use case và luồng xử lý (thứ tự bước, điểm ghi bền vững, transaction); dữ liệu sở hữu (bảng, cột, ràng buộc, migration); cấu hình (`tmc.*`, giá trị mặc định); cách xử lý lỗi và timeout.
- **Frontend:** các màn hình và trạng thái (chờ, đang gửi, thành công, lỗi, offline, ...); luồng người dùng; dữ liệu lấy từ API nào, chu kỳ làm mới; xử lý lỗi mạng.
- **Quyết định nhỏ** trong phạm vi phần (ví dụ: thứ tự kiểm tra quy tắc, xử lý RA sau RA) kèm lý do.
- **Độ lệch so với báo cáo** (nếu có): báo cáo nói gì, demo làm gì, vì sao.

**Điều kiện chuyển bước:** người duyệt đồng ý; mọi port, bảng và cấu hình đều có tên chính thức.

### 3.3. `03-api` – Chốt hợp đồng

| | |
|---|---|
| **Mục tiêu** | Chốt mọi thứ mà **người khác** sẽ dùng từ phần này |
| **Đầu vào** | `02-spec/<phần>/spec.md` đã duyệt |
| **Đầu ra** | `03-api/<phần>/api.md`; nếu là REST thì kèm `openapi.yaml` (viết tay hoặc xuất từ springdoc) |
| **Người viết / duyệt** | Người phụ trách / **người dùng hợp đồng** (xem 3.3.1) |

Nội dung theo loại hợp đồng:

| Loại | Phải có |
|---|---|
| REST endpoint | Method, path, quyền truy cập, request/response JSON mẫu, mã HTTP, danh sách `reason`/mã lỗi, timeout mong đợi |
| Port (interface Java) | Chữ ký hàm, hợp đồng hành vi (tiền điều kiện, hậu điều kiện, lỗi được phép, giới hạn thời gian) – đây là căn cứ cho LSP |
| Sự kiện trong tiến trình | Tên sự kiện, payload, module phát, module nghe, thời điểm phát |
| DTO sync-contract | Record, trường, kiểu, ví dụ JSON, `contractVersion` |
| Màn hình (frontend) | Tham số URL, API mà màn hình gọi, dữ liệu cần backend trả |

#### 3.3.1. Ai duyệt hợp đồng của ai

| Hợp đồng | Người viết | Người duyệt |
|---|---|---|
| `03-api/sync-contract` | BE-2 | BE-1 |
| `03-api/gate-agent/access` (`/scan`, `/status`), `03-api/gate-agent/sync` (`/admin/simulate-offline`), `03-api/gate-agent/guard` (`/guard/pending`) | BE-1 | FE-1 |
| `03-api/central/employee` (`/portal/qr-token`, `/employees`), `03-api/central/station` (`/stations`), `03-api/central/iam` (`/auth/login`, `/auth/me`) | BE-2 | FE-2 |
| `03-api/central/ingestion` (`/sync/*`) | BE-2 | BE-1 |
| `03-api/simulators/turnstile-sim` | BE-2 | BE-1 |
| `03-api/web/shared` (client và component dùng chung, cách gắn token `Authorization`) | FE-1 | FE-2 |
| `03-api/web/auth` (component đăng nhập, lưu và gửi JWT, xử lý 401) | FE-2 | FE-1 |

**Điều kiện chuyển bước:** người duyệt đã đồng ý trên PR. Từ thời điểm này hợp đồng **được đóng băng**. Muốn đổi thì mở PR riêng, người dùng hợp đồng phải duyệt, và ghi lại lịch sử thay đổi ở cuối `api.md`.

### 3.4. `04-test` – Viết test case trước, ghi kết quả sau

| | |
|---|---|
| **Mục tiêu** | Biết trước "thế nào là đúng", và lưu bằng chứng cho báo cáo |
| **Đầu vào** | `01-analysis/<phần>` (yêu cầu) và `03-api/<phần>` (hợp đồng) |
| **Đầu ra** | `04-test/<phần>/test-plan.md` (trước khi code) và `04-test/<phần>/test-report.md` (sau khi chạy) |
| **Người viết / duyệt** | Người phụ trách / người dùng hợp đồng |

`test-plan.md` gồm bảng test case:

| Cột | Ý nghĩa |
|---|---|
| ID | `TC-<phần>-<số>`, ví dụ `TC-GA-ACCESS-07`, `TC-CE-INGESTION-02`, `TC-WEB-KIOSK-03` |
| Truy vết | FR / NFR / INV / kịch bản demo (T1–T11, D1–D6) mà test này chứng minh |
| Loại | unit / integration / architecture / e2e / UI |
| Tiền điều kiện, bước, kết quả mong đợi | Đủ để người khác chạy lại |
| Vị trí test trong code | Lớp test hoặc script tương ứng |

`test-report.md` ghi: ngày chạy, commit, kết quả từng test case (đạt / không đạt), số liệu đo (thời gian, số bản ghi, p95), ảnh chụp nếu là UI. DOC lấy trực tiếp từ đây để viết phần 9 của báo cáo.

**Điều kiện chuyển bước sang code:** `test-plan.md` đã duyệt và mỗi yêu cầu Must trong `analysis.md` có ít nhất một test case.

### 3.5. Code

- Chỉ bắt đầu khi `03-api/<phần>` và `04-test/<phần>/test-plan.md` đã duyệt.
- Code theo đúng `02-spec/<phần>`. Nếu trong lúc code phải đổi thiết kế thì sửa `spec.md` (và `api.md` nếu ảnh hưởng hợp đồng) **trong cùng PR**.
- Phần xong khi: mọi test case Must trong `test-plan.md` đạt, CI xanh, `test-report.md` đã cập nhật.

---

## 4. Phần `system`

Thư mục `system` trong mỗi bước dành cho những gì không thuộc riêng một package:

| Thư mục | Nội dung |
|---|---|
| `01-analysis/system` | Phạm vi MVP tổng thể, các component không hiện thực và lý do, bảng độ lệch so với báo cáo, ánh xạ component ↔ package ↔ người |
| `02-spec/system` | Luồng end-to-end F1 (quét), F2 (đồng bộ) ghép từ các phần; hạ tầng demo (compose, mạng `wan`/`lan`, biến môi trường) |
| `03-api/system` | Bảng tổng hợp toàn bộ endpoint và cổng dịch vụ (liên kết tới `api.md` của từng phần) |
| `04-test/system` | Kịch bản demo D1–D6, kịch bản E2E T1–T8, T10, T11, load test T9, kiểm thử kiến trúc A1–A3, mốc tích hợp INT-1..3; báo cáo kết quả tổng hợp |

---

## 5. Thứ tự thực hiện

Các phần **không** cần làm tuần tự với nhau; chỉ cần mỗi phần đi đúng thứ tự 4 bước của nó. Thứ tự ưu tiên giữa các phần là theo **ai đang chờ hợp đồng của ai**:

| Thời gian | Bước cần xong | Lý do |
|---|---|---|
| **Tuần 0** | `01-analysis` của mọi phần; `03-api` của `sync-contract`, `gate-agent/access`, `central/employee`, `central/iam`, `central/ingestion`, `simulators/turnstile-sim` | Đây là các hợp đồng mà người khác phải chờ |
| **Tuần 1** | `02-spec`, `04-test` (test-plan) của các phần Must còn lại → code luồng Must | Mốc INT-1: luồng F1 chạy end-to-end |
| **Tuần 2** | `03-api` của `gate-agent/guard`, `central/station`; code offline và độ tin cậy; `test-report` của các phần Must | Mốc INT-2: T2, T3, T5, T7, T8, T10, T11 |
| **Tuần 3** | `web/admin` rút gọn, load test T9; `04-test/system/test-report.md` | Mốc INT-3: đóng băng code, demo, phần 9 |

Một phần nhỏ (ví dụ `web/shared`) có thể làm từ `01-analysis` tới `03-api` trong cùng một PR, nhưng không được bỏ qua bước.

---

## 6. Quy ước

- **Tên file cố định:** `analysis.md`, `spec.md`, `api.md` (+ `openapi.yaml`), `test-plan.md`, `test-report.md`.
- **Trạng thái tài liệu:** dòng đầu mỗi file ghi `> Trạng thái: Draft | Review | Approved · Người viết · Người duyệt · Ngày`.
- **Liên kết giữa các bước:** đầu mỗi file có dòng liên kết tới file cùng phần ở bước trước (ví dụ `spec.md` liên kết `../../../01-analysis/gate-agent/access/analysis.md`).
- **Nhánh và PR:** tài liệu đi trên nhánh `docs/<bước>-<phần>` (ví dụ `docs/api-gate-access`); code đi trên nhánh `feat/<phần>-<mô-tả>`. PR tài liệu cần người duyệt như ở mục 3.
- **Truy vết:** mọi tham chiếu tới báo cáo ghi kèm mã (FR-11, INV-05, ADR-03, DP-07) để DOC dò ngược được.
- **Thuật ngữ:** giữ đúng tên trong báo cáo (component, port, sự kiện). Đổi tên thì ghi vào `spec.md` mục "Độ lệch" và báo DOC.
- **Hình:** vẽ bằng draw.io, lưu nguồn `.drawio` cạnh ảnh `.png` trong cùng thư mục.
- Thư mục chỉ chứa `.gitkeep` nghĩa là bước đó của phần đó chưa bắt đầu. Khi thêm file đầu tiên thì xóa `.gitkeep`.
