# AU-02 – Không có quyền truy cập

> Trạng thái: **Mô tả xong · Chưa thiết kế** · Người viết: FE-2 · Ngày: 17/11/2026
>
> Vị trí trong sitemap: [sitemap.md § 4 – AU-02](../sitemap.md#4-danh-mục-màn-hình-ánh-xạ-sitemap--thiết-kế) · Màn hình cha: portal.html, admin.html

| Thuộc tính | Giá trị |
|---|---|
| Mã | AU-02 |
| Trang / route | `portal.html`, `admin.html` |
| Loại | screen |
| Mức | Must |
| Người dùng | Người dùng đăng nhập sai vai trò (vd. tài khoản EMPLOYEE mở Admin) |
| Thành phần code | `features/auth` – `Forbidden`, `RequireRole` |
| Yêu cầu, kịch bản demo | NFR-SEC-01, DP-10; kịch bản S1 |

## 1. Mục đích

Báo rõ tài khoản hiện tại không được dùng trang này và cho phép đăng xuất để đổi tài khoản.

## 2. Điều hướng

| Vào màn hình khi | Rời màn hình khi |
|---|---|
| `GET /auth/me` trả vai trò khác vai trò của trang | Đăng xuất → AU-01 |
| API trả 403 |  |

## 3. API sử dụng

- [`GET /auth/me`](../../api/central.md#2-get-authme)

## 4. Trạng thái màn hình

| Trạng thái | Nội dung chính |
|---|---|
| Mặc định | "Không có quyền truy cập", tên tài khoản và vai trò hiện tại, nút Đăng xuất. |

## 5. Dữ liệu hiển thị

- `username`, `role`

## 6. Quy tắc hành vi

- Không gọi API nghiệp vụ nào khi đang ở màn hình này.

## 7. Thiết kế chi tiết

> **TODO – chưa thiết kế.** Điền khi bắt đầu bước thiết kế giao diện.

### 7.1. Wireframe
_TODO: wireframe cho từng trạng thái ở mục 4 (`../../images/screens/AU-02-forbidden.png`)._

### 7.2. Bố cục và thành phần UI
_TODO: vùng, component, kích thước, typography, màu theo trạng thái._

### 7.3. Tương tác chi tiết
_TODO: thao tác chuột / bàn phím / máy quét, chuyển trạng thái, thời gian hiệu ứng._

### 7.4. Responsive
_TODO: kích thước mục tiêu – điện thoại (Portal) và desktop (Admin)._

### 7.5. Khả năng tiếp cận
_TODO: tương phản màu, cỡ chữ, focus, nhãn cho trình đọc màn hình._

## 8. Tiêu chí chấp nhận (nháp)

_TODO: hoàn thiện cùng thiết kế; mỗi trạng thái ở mục 4 có ít nhất một test UI (Vitest) hoặc một bước trong kịch bản demo._
