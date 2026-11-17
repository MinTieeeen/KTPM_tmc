# K-05 – Bảng điều khiển demo

> Trạng thái: **Mô tả xong · Chưa thiết kế** · Người viết: FE-1 · Ngày: 17/11/2026
>
> Vị trí trong sitemap: [sitemap.md § 4 – K-05](../sitemap.md#4-danh-mục-màn-hình-ánh-xạ-sitemap--thiết-kế) · Màn hình cha: kiosk.html (bổ sung cho K-01)

| Thuộc tính | Giá trị |
|---|---|
| Mã | K-05 |
| Trang / route | `kiosk.html?station=…&demo=1` |
| Loại | panel |
| Mức | Could |
| Người dùng | Người trình diễn |
| Thành phần code | `features/kiosk` – `DemoPanel` |
| Yêu cầu, kịch bản demo | Demo D4, T11 |

## 1. Mục đích

Bật/tắt giả lập mất kết nối Gate–Central ngay trên kiosk khi trình diễn D4/T11, không cần chạy script.

## 2. Điều hướng

| Vào màn hình khi | Rời màn hình khi |
|---|---|
| Tham số `demo=1` | Thu gọn bảng (vẫn ở K-01) |

## 3. API sử dụng

- [`POST /admin/simulate-offline`](../../api/gate-agent.md#4-post-adminsimulate-offline)
- [`GET /status`](../../api/gate-agent.md#2-get-status)

## 4. Trạng thái màn hình

| Trạng thái | Nội dung chính |
|---|---|
| Online | Nút "Giả lập mất kết nối". |
| Đang offline | Nút "Nối lại", hiện `outboxDepth` tăng dần. |
| Không khả dụng | `POST /admin/simulate-offline` trả 404 (không phải profile demo): ẩn bảng. |

## 5. Dữ liệu hiển thị

- `online`, `outboxDepth`, `offlineThresholdExceeded`

## 6. Quy tắc hành vi

- Bảng thu gọn ở góc, không che vùng kết quả quét.

## 7. Thiết kế chi tiết

> **TODO – chưa thiết kế.** Điền khi bắt đầu bước thiết kế giao diện.

### 7.1. Wireframe
_TODO: wireframe cho từng trạng thái ở mục 4 (`../../images/screens/K-05-demo-panel.png`)._

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
