# K-03 – Lỗi cấu hình trạm

> Trạng thái: **Mô tả xong · Chưa thiết kế** · Người viết: FE-1 · Ngày: 17/11/2026
>
> Vị trí trong sitemap: [sitemap.md § 4 – K-03](../sitemap.md#4-danh-mục-màn-hình-ánh-xạ-sitemap--thiết-kế) · Màn hình cha: kiosk.html

| Thuộc tính | Giá trị |
|---|---|
| Mã | K-03 |
| Trang / route | `kiosk.html` (thiếu/sai `?station`) |
| Loại | screen |
| Mức | Must |
| Người dùng | Kỹ thuật viên lắp đặt kiosk |
| Thành phần code | `features/kiosk` – `StationError` |
| Yêu cầu, kịch bản demo | INV-03, FR-14 |

## 1. Mục đích

Chặn việc dùng kiosk khi URL không chỉ rõ một trạm hợp lệ, tránh gửi lượt quét với trạm sai.

## 2. Điều hướng

| Vào màn hình khi | Rời màn hình khi |
|---|---|
| Thiếu tham số `station` | Sửa URL và tải lại trang → K-01 |
| `station` sai định dạng `^[A-Z]{2}-(IN|OUT)-[0-9]{2}$` |  |
| `station` không có trong `GET /status.turnstiles` |  |

## 3. API sử dụng

- [`GET /status`](../../api/gate-agent.md#2-get-status)

## 4. Trạng thái màn hình

| Trạng thái | Nội dung chính |
|---|---|
| Thiếu / sai định dạng | Hướng dẫn cú pháp `kiosk.html?station=BD-IN-01`. |
| Trạm không thuộc gate | Liệt kê các trạm hợp lệ lấy từ `GET /status.turnstiles`. |
| Không gọi được gate | Thông báo lỗi kết nối, tự thử lại mỗi 3 s. |

## 5. Dữ liệu hiển thị

- Danh sách trạm của gate (các khóa của `turnstiles`)

## 6. Quy tắc hành vi

- Không có ô nhập quét trên màn hình này.

## 7. Thiết kế chi tiết

> **TODO – chưa thiết kế.** Điền khi bắt đầu bước thiết kế giao diện.

### 7.1. Wireframe
_TODO: wireframe cho từng trạng thái ở mục 4 (`../../images/screens/K-03-station-error.png`)._

### 7.2. Bố cục và thành phần UI
_TODO: vùng, component, kích thước, typography, màu theo trạng thái._

### 7.3. Tương tác chi tiết
_TODO: thao tác chuột / bàn phím / máy quét, chuyển trạng thái, thời gian hiệu ứng._

### 7.4. Responsive
_TODO: kích thước mục tiêu – màn hình kiosk ngang, toàn màn hình._

### 7.5. Khả năng tiếp cận
_TODO: tương phản màu, cỡ chữ, focus, nhãn cho trình đọc màn hình._

## 8. Tiêu chí chấp nhận (nháp)

_TODO: hoàn thiện cùng thiết kế; mỗi trạng thái ở mục 4 có ít nhất một test UI (Vitest) hoặc một bước trong kịch bản demo._
