# A-02 – Tab Nhân viên

> Trạng thái: **Mô tả xong · Chưa thiết kế** · Người viết: FE-2 · Ngày: 17/11/2026
>
> Vị trí trong sitemap: [sitemap.md § 4 – A-02](../sitemap.md#4-danh-mục-màn-hình-ánh-xạ-sitemap--thiết-kế) · Màn hình cha: A-01 Khung trang quản trị

| Thuộc tính | Giá trị |
|---|---|
| Mã | A-02 |
| Trang / route | `admin.html#/employees` |
| Loại | tab |
| Mức | Should |
| Người dùng | Quản trị viên |
| Thành phần code | `features/admin` – `EmployeesTab` |
| Yêu cầu, kịch bản demo | FR-02, FR-04; demo D3 |

## 1. Mục đích

Xem nhân viên và trạng thái QR credential; khởi động thao tác thu hồi / cấp lại (demo D3).

## 2. Điều hướng

| Vào màn hình khi | Rời màn hình khi |
|---|---|
| Mặc định khi mở Admin | Thu hồi / Cấp lại → A-03 |
| Chọn tab Nhân viên |  |

## 3. API sử dụng

- [`GET /employees`](../../api/central.md#4-get-employees)

## 4. Trạng thái màn hình

| Trạng thái | Nội dung chính |
|---|---|
| Đang tải | Lần tải đầu. |
| Danh sách | Mã, họ tên, bộ phận, nhà máy, trạng thái nhân viên, trạng thái credential, ngày cấp, nút thao tác. |
| Không có kết quả | Bộ lọc không khớp nhân viên nào. |
| Lỗi tải | Thông báo + nút Thử lại. |

## 5. Dữ liệu hiển thị

- `EmployeeView[]`
- Bộ lọc bộ phận lấy từ danh sách chưa lọc

## 6. Quy tắc hành vi

- Tìm kiếm debounce 300 ms (`q`), lọc theo `department`.
- Nút **Thu hồi** khi `credentialStatus = ACTIVE`; **Cấp lại** khi nhân viên `active`.
- Sau A-03 thành công, thay dòng bằng `EmployeeView` trả về.

## 7. Thiết kế chi tiết

> **TODO – chưa thiết kế.** Điền khi bắt đầu bước thiết kế giao diện.

### 7.1. Wireframe
_TODO: wireframe cho từng trạng thái ở mục 4 (`../../images/screens/A-02-employees-tab.png`)._

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
