# AU-01 – Đăng nhập

> Trạng thái: **Mô tả xong · Chưa thiết kế** · Người viết: FE-2 · Ngày: 17/11/2026
>
> Vị trí trong sitemap: [sitemap.md § 4 – AU-01](../sitemap.md#4-danh-mục-màn-hình-ánh-xạ-sitemap--thiết-kế) · Màn hình cha: portal.html, admin.html

| Thuộc tính | Giá trị |
|---|---|
| Mã | AU-01 |
| Trang / route | `portal.html`, `admin.html` |
| Loại | screen |
| Mức | Must |
| Người dùng | Nhân viên, quản trị viên |
| Thành phần code | `features/auth` – `LoginForm`; `shared/api/authSession` |
| Yêu cầu, kịch bản demo | FR-25, NFR-SEC-04; kịch bản S2 |

## 1. Mục đích

Xác thực người dùng trước khi vào Portal hoặc Admin; một component dùng chung cho cả hai trang.

## 2. Điều hướng

| Vào màn hình khi | Rời màn hình khi |
|---|---|
| Chưa có token | Đăng nhập thành công, đúng vai trò → P-01 / A-02 |
| API trả 401 | Sai vai trò → AU-02 |
| Bấm Đăng xuất |  |

## 3. API sử dụng

- [`POST /auth/login`](../../api/central.md#1-post-authlogin)
- [`GET /auth/me`](../../api/central.md#2-get-authme)

## 4. Trạng thái màn hình

| Trạng thái | Nội dung chính |
|---|---|
| Nhập liệu | Tên đăng nhập, mật khẩu, nút Đăng nhập (vô hiệu khi trống). |
| Đang gửi | Khóa nút, chặn gửi lặp. |
| Sai thông tin | 401 `INVALID_CREDENTIALS`: "Sai thông tin đăng nhập hoặc tài khoản bị khóa" (một câu cho mọi trường hợp). |
| Lỗi mạng / 5xx | "Không kết nối được máy chủ, thử lại". |

## 5. Dữ liệu hiển thị

- Tên trang (Portal / Admin) để hiển thị tiêu đề

## 6. Quy tắc hành vi

- Token lưu trong `sessionStorage`.
- Không lưu, không log mật khẩu.

## 7. Thiết kế chi tiết

> **TODO – chưa thiết kế.** Điền khi bắt đầu bước thiết kế giao diện.

### 7.1. Wireframe
_TODO: wireframe cho từng trạng thái ở mục 4 (`../../images/screens/AU-01-login.png`)._

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
