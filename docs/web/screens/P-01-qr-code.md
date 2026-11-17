# P-01 – Mã QR cá nhân

> Trạng thái: **Mô tả xong · Chưa thiết kế** · Người viết: FE-2 · Ngày: 17/11/2026
>
> Vị trí trong sitemap: [sitemap.md § 4 – P-01](../sitemap.md#4-danh-mục-màn-hình-ánh-xạ-sitemap--thiết-kế) · Màn hình cha: portal.html

| Thuộc tính | Giá trị |
|---|---|
| Mã | P-01 |
| Trang / route | `portal.html` |
| Loại | screen |
| Mức | Must |
| Người dùng | Nhân viên (điện thoại) |
| Thành phần code | `features/portal` – `QrPage`, `QrCountdown` (`qrcode.react`) |
| Yêu cầu, kịch bản demo | FR-03, NFR-SEC-05; demo D1, D2 |

## 1. Mục đích

Hiển thị mã QR động của chính nhân viên đang đăng nhập; mã tự làm mới nên ảnh chụp lại nhanh chóng mất giá trị.

## 2. Điều hướng

| Vào màn hình khi | Rời màn hình khi |
|---|---|
| Đăng nhập thành công với vai trò EMPLOYEE | 409 → P-02 |
| P-02 nhận lại 200 | 401 / Đăng xuất → AU-01 |

## 3. API sử dụng

- [`GET /portal/qr-token`](../../api/central.md#3-get-portalqr-token)

## 4. Trạng thái màn hình

| Trạng thái | Nội dung chính |
|---|---|
| Đang tải | Lần lấy mã đầu tiên. |
| Hiển thị mã | QR mức sửa lỗi M, đếm ngược tới `expiresAt`, mã nhân viên. |
| Đang làm mới | Giữ mã cũ cho tới khi có mã mới. |
| Mất mạng | Giữ mã hiện tại tới `expiresAt`, sau đó "Không lấy được mã QR, kiểm tra kết nối". |

## 5. Dữ liệu hiển thị

- `raw`, `expiresAt`, `refreshAfterSec`
- `employeeCode` (từ `GET /auth/me`)

## 6. Quy tắc hành vi

- Làm mới mỗi `refreshAfterSec` (30 s) và khi tab hiện lại (`visibilitychange`).
- Nút "Sao chép chuỗi" copy `raw`.
- Không log `raw`.

## 7. Thiết kế chi tiết

> **TODO – chưa thiết kế.** Điền khi bắt đầu bước thiết kế giao diện.

### 7.1. Wireframe
_TODO: wireframe cho từng trạng thái ở mục 4 (`../../images/screens/P-01-qr-code.png`)._

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
