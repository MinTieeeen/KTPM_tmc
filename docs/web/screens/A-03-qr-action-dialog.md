# A-03 – Hộp thoại thao tác QR

> Trạng thái: **Mô tả xong · Chưa thiết kế** · Người viết: FE-2 · Ngày: 17/11/2026
>
> Vị trí trong sitemap: [sitemap.md § 4 – A-03](../sitemap.md#4-danh-mục-màn-hình-ánh-xạ-sitemap--thiết-kế) · Màn hình cha: A-02 Tab Nhân viên

| Thuộc tính | Giá trị |
|---|---|
| Mã | A-03 |
| Trang / route | dialog trên `admin.html#/employees` |
| Loại | dialog |
| Mức | Should |
| Người dùng | Quản trị viên |
| Thành phần code | `features/admin` – `QrActionDialog` |
| Yêu cầu, kịch bản demo | FR-04, INV-01, NFR-SEC-06; demo D3, T2 |

## 1. Mục đích

Xác nhận trước khi thu hồi (bắt buộc nhập lý do) hoặc cấp lại QR credential của một nhân viên.

## 2. Điều hướng

| Vào màn hình khi | Rời màn hình khi |
|---|---|
| Bấm Thu hồi hoặc Cấp lại trên một dòng của A-02 | Thành công / Hủy → A-02 |

## 3. API sử dụng

- [`POST /employees/{code}/qr/revoke`](../../api/central.md#6-post-employeescodeqrrevoke)
- [`POST /employees/{code}/qr/issue`](../../api/central.md#5-post-employeescodeqrissue)

## 4. Trạng thái màn hình

| Trạng thái | Nội dung chính |
|---|---|
| Thu hồi | Mã + tên nhân viên, ô lý do 3–200 ký tự (bắt buộc), nút Xác nhận / Hủy. |
| Cấp lại | Cảnh báo mã hiện tại (nếu có) sẽ bị thu hồi; nút Xác nhận / Hủy. |
| Đang gửi | Khóa nút. |
| Lỗi | 404/409: câu theo `code` (`NO_ACTIVE_CREDENTIAL`, `EMPLOYEE_INACTIVE`; `CONCURRENT_UPDATE` → tải lại). |

## 5. Dữ liệu hiển thị

- Dòng `EmployeeView` đang chọn

## 6. Quy tắc hành vi

- Sau khi thu hồi thành công: "Thu hồi có hiệu lực tại các trạm trong ≤ 60 giây".

## 7. Thiết kế chi tiết

> **TODO – chưa thiết kế.** Điền khi bắt đầu bước thiết kế giao diện.

### 7.1. Wireframe
_TODO: wireframe cho từng trạng thái ở mục 4 (`../../images/screens/A-03-qr-action-dialog.png`)._

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
