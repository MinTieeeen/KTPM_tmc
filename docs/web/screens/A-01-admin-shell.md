# A-01 – Khung trang quản trị

> Trạng thái: **Mô tả xong · Chưa thiết kế** · Người viết: FE-2 · Ngày: 17/11/2026
>
> Vị trí trong sitemap: [sitemap.md § 4 – A-01](../sitemap.md#4-danh-mục-màn-hình-ánh-xạ-sitemap--thiết-kế) · Màn hình cha: admin.html

| Thuộc tính | Giá trị |
|---|---|
| Mã | A-01 |
| Trang / route | `admin.html` |
| Loại | layout |
| Mức | Should |
| Người dùng | Quản trị viên |
| Thành phần code | `features/admin` – `AdminShell` |
| Yêu cầu, kịch bản demo | FR-25 |

## 1. Mục đích

Khung chung của Admin: tiêu đề, người đang đăng nhập, thanh tab Nhân viên / Trạm, nút Đăng xuất.

## 2. Điều hướng

| Vào màn hình khi | Rời màn hình khi |
|---|---|
| Đăng nhập thành công với vai trò ADMIN | Chọn tab → A-02 / A-04 |
|  | Đăng xuất / 401 → AU-01 |

## 3. API sử dụng

- [`GET /auth/me`](../../api/central.md#2-get-authme)

## 4. Trạng thái màn hình

| Trạng thái | Nội dung chính |
|---|---|
| Mặc định | Tab theo hash; không có hash → `#/employees`. |

## 5. Dữ liệu hiển thị

- `username`

## 6. Quy tắc hành vi

- Hash routing `#/employees`, `#/stations`; tải lại trang giữ nguyên tab.

## 7. Thiết kế chi tiết

> **TODO – chưa thiết kế.** Điền khi bắt đầu bước thiết kế giao diện.

### 7.1. Wireframe
_TODO: wireframe cho từng trạng thái ở mục 4 (`../../images/screens/A-01-admin-shell.png`)._

### 7.2. Bố cục và thành phần UI
_TODO: vùng, component, kích thước, typography, màu theo trạng thái._

### 7.3. Tương tác chi tiết
_TODO: thao tác chuột / bàn phím / máy quét, chuyển trạng thái, thời gian hiệu ứng._

### 7.4. Responsive
_TODO: kích thước mục tiêu – desktop ≥ 1280 px._

### 7.5. Khả năng tiếp cận
_TODO: tương phản màu, cỡ chữ, focus, nhãn cho trình đọc màn hình._

## 8. Tiêu chí chấp nhận (nháp)

_TODO: hoàn thiện cùng thiết kế; mỗi trạng thái ở mục 4 có ít nhất một test UI (Vitest) hoặc một bước trong kịch bản demo._
