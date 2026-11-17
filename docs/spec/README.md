# Đặc tả yêu cầu phần mềm (SRS) và thiết kế chi tiết theo module

> Trạng thái: Draft · Người viết: nhóm TMC · Người duyệt: người cùng nhóm (BE-1 ↔ BE-2, FE ↔ BE dùng hợp đồng) · Ngày: 17/11/2026
>
> Nguồn: báo cáo `KTPM_report/` (mục 3 yêu cầu, 4 domain, 6 kiến trúc, 7 ADR, 8 nguyên tắc), [DacTa_TrienKhai_Code_M2.md](../../../DacTa_TrienKhai_Code_M2.md). Khi hai nguồn khác nhau thì **báo cáo là chuẩn**; chỗ code khác báo cáo được ghi ở mục "Quyết định thiết kế và độ lệch" của từng spec.

## 1. Tổ chức thư mục

```
tmc/docs/
├── spec/      README.md (tài liệu này) + một file SRS cho mỗi module: <module>.md
├── api/       đặc tả REST API chuẩn OpenAPI 3.0.3 (tiếng Anh) – hợp đồng giữa frontend và backend, xem api/README.md
├── drawio/    class-diagram.drawio – mọi sơ đồ lớp, mỗi module một tab (tên tab = <module>)
└── images/    class-diagram-<module>.png – ảnh xuất từ từng tab
```

Quy ước vẽ sơ đồ lớp:
- Package xếp theo tầng; lớp tô màu theo tầng: **api** xanh dương, **application** xanh lá, **domain** vàng, **infrastructure** cam, module khác / thư viện xám (package viền nét đứt). Riêng `sync-contract` tô theo package chủ đề.
- Record/DTO không vẽ (chỉ xuất hiện làm kiểu trong chữ ký), trừ `sync-contract`, vì nội dung của module này toàn là record.
- Đường nối gãy khúc vuông góc, góc bo; mỗi đường có điểm ra/vào riêng và làn riêng nên không có đoạn nào chồng lên nhau; chỗ hai đường cắt nhau có cầu vượt.

Sơ đồ được sinh bằng script. Sau khi sửa thiết kế, chạy lại từ thư mục `KTPM-261/`:

```bash
python artifacts/drawio/02_spec/class_diagrams.py tmc/docs/drawio/class-diagram.drawio
# xuất PNG tab thứ <p> (bắt đầu từ 1, theo thứ tự ở bảng dưới):
draw.io -x -f png -b 20 -p <p> -o tmc/docs/images/class-diagram-<module>.png tmc/docs/drawio/class-diagram.drawio
```

## 2. Danh mục spec

| Tab | Module | Component (báo cáo) | Mức | Spec | Ảnh sơ đồ lớp |
|---|---|---|---|---|---|
| 1 | `sync-contract` | Hợp đồng Q1 ↔ Q2 | Must | [sync-contract.md](sync-contract.md) | [png](../images/class-diagram-sync-contract.png) |
| 2 | `gate-agent-access` | G1 Access Control | Must | [gate-agent-access.md](gate-agent-access.md) | [png](../images/class-diagram-gate-agent-access.png) |
| 3 | `gate-agent-turnstile` | G2 Turnstile Adapter | Must | [gate-agent-turnstile.md](gate-agent-turnstile.md) | [png](../images/class-diagram-gate-agent-turnstile.png) |
| 4 | `gate-agent-localstore` | G3 Local Store & Outbox | Must | [gate-agent-localstore.md](gate-agent-localstore.md) | [png](../images/class-diagram-gate-agent-localstore.png) |
| 5 | `gate-agent-sync` | G4 Gate Sync Agent | Must | [gate-agent-sync.md](gate-agent-sync.md) | [png](../images/class-diagram-gate-agent-sync.png) |
| 6 | `gate-agent-guard` | G5 Guard Console (rút gọn) | Should | [gate-agent-guard.md](gate-agent-guard.md) | [png](../images/class-diagram-gate-agent-guard.png) |
| 7 | `central-ingestion` | C1 Gate Sync & Ingestion | Must | [central-ingestion.md](central-ingestion.md) | [png](../images/class-diagram-central-ingestion.png) |
| 8 | `central-employee` | C2 Employee & QR Management | Must | [central-employee.md](central-employee.md) | [png](../images/class-diagram-central-employee.png) |
| 9 | `central-station` | C3 Gate Station Management (tối thiểu) | Must | [central-station.md](central-station.md) | [png](../images/class-diagram-central-station.png) |
| 10 | `central-iam` | C7 IAM / RBAC (tối thiểu) | Must | [central-iam.md](central-iam.md) | [png](../images/class-diagram-central-iam.png) |
| 11 | `turnstile-sim` | Bộ giả lập cổng xoay | Must | [turnstile-sim.md](turnstile-sim.md) | [png](../images/class-diagram-turnstile-sim.png) |

Nguồn draw.io chung: [class-diagram.drawio](../drawio/class-diagram.drawio). `system` và `web` không thuộc phạm vi các spec này.

## 3. Khung SRS dùng chung

Mỗi file spec theo cùng một khung (rút gọn từ IEEE 830, bổ sung phần thiết kế chi tiết):

1. **Giới thiệu** – mục đích, phạm vi, tài liệu tham chiếu, thuật ngữ.
2. **Mô tả tổng quan** – vị trí trong kiến trúc, trách nhiệm, tác nhân, ràng buộc, giả định, phụ thuộc.
3. **Yêu cầu chức năng** – mỗi yêu cầu có mã, mức (Must/Should/Could), truy vết FR/NFR/INV/ADR/DP của báo cáo.
4. **Yêu cầu phi chức năng.**
5. **Giao diện ngoài** – REST, port Java, sự kiện, DTO.
6. **Thiết kế** – 6.1 mô tả thành phần theo tầng `api / application / domain / infrastructure`; 6.2 danh sách lớp; 6.3 sơ đồ lớp UML; 6.4 luồng xử lý; 6.5 dữ liệu; 6.6 cấu hình; 6.7 xử lý lỗi.
7. **Quyết định thiết kế và độ lệch so với báo cáo.**
8. **Tiêu chí chấp nhận** – ánh xạ yêu cầu → test case.

## 4. Quy ước

- **Mã yêu cầu:** `SRS-<MÃ PHẦN>-<FR|NFR>-<số>`. Mã phần: `SC` (sync-contract), `GA-ACCESS`, `GA-TURN`, `GA-STORE`, `GA-SYNC`, `GA-GUARD`, `CE-ING`, `CE-EMP`, `CE-STA`, `CE-IAM`, `SIM-TS`. Test case tương ứng dùng mã `TC-<MÃ PHẦN>-<số>`.
- **Ký hiệu UML** trong sơ đồ lớp: package (hình thư mục) = tầng của module; package nền xám = thuộc module khác (chỉ vẽ phần được dùng); `«interface»`, `«enumeration»`, `«record»` (Java record, coi như lớp bất biến); đường nét đứt mũi tam giác rỗng = realization; nét đứt mũi mở = dependency (`«use»`, `«create»`, `«publish»`, `«listen»`); nét liền mũi mở có bội số = association; hình thoi đặc = composition; nét liền tam giác rỗng = generalization. Thành viên gạch chân = `static`.
- **Tên lớp, port, sự kiện** giữ đúng báo cáo và đặc tả triển khai M2. Lớp/port mới (báo cáo chưa có) được đánh dấu "bổ sung" trong bảng danh sách lớp để DOC cập nhật Bảng port (mục 8 báo cáo).
- **Thời gian:** `Instant` (UTC) trong mã và hợp đồng; "bây giờ" lấy từ `java.time.Clock` được tiêm vào.
- **Cấu hình:** mọi ngưỡng, timeout, chu kỳ đọc từ `tmc.*` (DP-17); spec ghi tên khóa và giá trị mặc định.
