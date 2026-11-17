# P-02 – Không có mã QR

> Trạng thái: **Mô tả xong · Chưa thiết kế** · Người viết: FE-2 · Ngày: 17/11/2026
>
> Vị trí trong sitemap: [sitemap.md § 4 – P-02](../sitemap.md#4-danh-mục-màn-hình-ánh-xạ-sitemap--thiết-kế) · Màn hình cha: P-01 Mã QR cá nhân

| Thuộc tính | Giá trị |
|---|---|
| Mã | P-02 |
| Trang / route | `portal.html` (409) |
| Loại | state |
| Mức | Must |
| Người dùng | Nhân viên |
| Thành phần code | `features/portal` – `NoQrNotice` |
| Yêu cầu, kịch bản demo | FR-04, NFR-SEC-06; demo D3, T2 |

## 1. Mục đích

Thay thế mã QR khi nhân viên không được cấp mã (bị thu hồi, chưa cấp, nghỉ việc); là bằng chứng của demo D3 phía Portal.

## 2. Điều hướng

| Vào màn hình khi | Rời màn hình khi |
|---|---|
| `GET /portal/qr-token` trả 409 | Lần hỏi lại (30 s) trả 200 → P-01 |
|  | Đăng xuất → AU-01 |

## 3. API sử dụng

- [`GET /portal/qr-token`](../../api/central.md#3-get-portalqr-token)

## 4. Trạng thái màn hình

| Trạng thái | Nội dung chính |
|---|---|
| CREDENTIAL_REVOKED | "Mã QR đã bị thu hồi, liên hệ HR". |
| NO_ACTIVE_CREDENTIAL | "Bạn chưa được cấp mã QR, liên hệ HR". |
| EMPLOYEE_INACTIVE | "Hồ sơ nhân viên không còn hiệu lực". |

## 5. Dữ liệu hiển thị

- `code` của Problem Details

## 6. Quy tắc hành vi

- **Không** hiển thị bất kỳ mã QR nào (kể cả mã cũ).
- Tiếp tục hỏi lại mỗi 30 s.

## 7. Thiết kế chi tiết

> **TODO – chưa thiết kế.** Điền khi bắt đầu bước thiết kế giao diện.

### 7.1. Wireframe
_TODO: wireframe cho từng trạng thái ở mục 4 (`../../images/screens/P-02-no-qr.png`)._

### 7.2. Bố cục và thành phần UI
_TODO: vùng, component, kích thước, typography, màu theo trạng thái._

### 7.3. Tương tác chi tiết
_TODO: thao tác chuột / bàn phím / máy quét, chuyển trạng thái, thời gian hiệu ứng._

### 7.4. Responsive
_TODO: kích thước mục tiêu – điện thoại dọc 360–430 px._

### 7.5. Khả năng tiếp cận
_TODO: tương phản màu, cỡ chữ, focus, nhãn cho trình đọc màn hình._

## 8. Tiêu chí chấp nhận (nháp)

_TODO: hoàn thiện cùng thiết kế; mỗi trạng thái ở mục 4 có ít nhất một test UI (Vitest) hoặc một bước trong kịch bản demo._
