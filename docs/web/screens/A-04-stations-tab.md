# A-04 – Tab Trạm

> Trạng thái: **Mô tả xong · Chưa thiết kế** · Người viết: FE-2 · Ngày: 17/11/2026
>
> Vị trí trong sitemap: [sitemap.md § 4 – A-04](../sitemap.md#4-danh-mục-màn-hình-ánh-xạ-sitemap--thiết-kế) · Màn hình cha: A-01 Khung trang quản trị

| Thuộc tính | Giá trị |
|---|---|
| Mã | A-04 |
| Trang / route | `admin.html#/stations` |
| Loại | tab |
| Mức | Should |
| Người dùng | Quản trị viên |
| Thành phần code | `features/admin` – `StationsTab` |
| Yêu cầu, kịch bản demo | FR-09, NFR-AVL-04; demo D4 |

## 1. Mục đích

Theo dõi online/offline và số bản ghi chờ đồng bộ của từng nhà máy (demo D4: outbox tăng khi mất kết nối, về 0 khi nối lại).

## 2. Điều hướng

| Vào màn hình khi | Rời màn hình khi |
|---|---|
| Chọn tab Trạm | Chọn tab khác |

## 3. API sử dụng

- [`GET /stations`](../../api/central.md#7-get-stations)

## 4. Trạng thái màn hình

| Trạng thái | Nội dung chính |
|---|---|
| Đang tải | Lần tải đầu. |
| Danh sách | Trạm, nhà máy, chiều, gate, online/offline, heartbeat cuối (thời gian tương đối), outbox, trạng thái cổng xoay. |
| Lỗi tải | Giữ dữ liệu cũ, đánh dấu "dữ liệu có thể đã cũ". |

## 5. Dữ liệu hiển thị

- `StationView[]`

## 6. Quy tắc hành vi

- Tự làm mới mỗi 5 s.
- Nhóm theo nhà máy (các trạm cùng nhà máy dùng chung số liệu heartbeat).

## 7. Thiết kế chi tiết

> **TODO – chưa thiết kế.** Điền khi bắt đầu bước thiết kế giao diện.

### 7.1. Wireframe
_TODO: wireframe cho từng trạng thái ở mục 4 (`../../images/screens/A-04-stations-tab.png`)._

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
