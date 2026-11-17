# K-01 – Chờ quét

> Trạng thái: **Mô tả xong · Chưa thiết kế** · Người viết: FE-1 · Ngày: 17/11/2026
>
> Vị trí trong sitemap: [sitemap.md § 4 – K-01](../sitemap.md#4-danh-mục-màn-hình-ánh-xạ-sitemap--thiết-kế) · Màn hình cha: kiosk.html

| Thuộc tính | Giá trị |
|---|---|
| Mã | K-01 |
| Trang / route | `kiosk.html?station=<stationId>` |
| Loại | screen |
| Mức | Must |
| Người dùng | Người qua cổng (máy quét USB đặt tại trạm) |
| Thành phần code | `features/kiosk` – `KioskPage`, `ScanInput`, `OfflineBanner` |
| Yêu cầu, kịch bản demo | FR-11, FR-14, NFR-AVL-04, NFR-PER-01; demo D1, D4 |

## 1. Mục đích

Màn hình thường trực của kiosk: chờ máy quét gửi chuỗi QR, gửi `POST /scan`, và luôn cho biết tình trạng kết nối của gate (biểu ngữ OFFLINE).

## 2. Điều hướng

| Vào màn hình khi | Rời màn hình khi |
|---|---|
| Mở `kiosk.html?station=…` với trạm hợp lệ | Nhận chuỗi từ máy quét → K-02 |
| K-02 tự đóng sau 3 s | `stationId` không có trong `GET /status` → K-03 |

## 3. API sử dụng

- [`GET /status`](../../api/gate-agent.md#2-get-status)
- [`POST /scan`](../../api/gate-agent.md#1-post-scan)

## 4. Trạng thái màn hình

| Trạng thái | Nội dung chính |
|---|---|
| Sẵn sàng (online) | Hướng dẫn "Đưa mã QR vào máy quét", tên trạm và chiều (VÀO/RA). |
| Đang gửi | Đã nhận chuỗi, chờ phản hồi `POST /scan` (≤ 2 s); bỏ qua chuỗi mới (chặn quét đúp). |
| OFFLINE | `online = false`: biểu ngữ "OFFLINE – {outboxDepth} bản ghi chờ đồng bộ"; vẫn quét được. |
| Chiều vào tạm ngừng | `offlineThresholdExceeded = true` và trạm `IN`: thêm cảnh báo "Chiều vào tạm ngừng". |
| Mất liên lạc với gate | `GET /status` lỗi hoặc quá 2 s: giữ trạng thái cũ, hiện "Kiosk không kết nối được dịch vụ cổng". |

## 5. Dữ liệu hiển thị

- Mã trạm và chiều (từ URL và `GET /status`)
- `online`, `outboxDepth`, `offlineThresholdExceeded`, `turnstiles[stationId]`

## 6. Quy tắc hành vi

- Ô nhập ẩn **luôn giữ focus** (máy quét gõ chuỗi + Enter); trim, bỏ chuỗi rỗng.
- Hỏi `GET /status` mỗi 3 s.
- Một lượt quét tại một thời điểm; không tự gửi lại `/scan`.

## 7. Thiết kế chi tiết

> **TODO – chưa thiết kế.** Điền khi bắt đầu bước thiết kế giao diện.

### 7.1. Wireframe
_TODO: wireframe cho từng trạng thái ở mục 4 (`../../images/screens/K-01-kiosk-idle.png`)._

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
