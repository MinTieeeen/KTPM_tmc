# K-04 – Quét bằng webcam

> Trạng thái: **Mô tả xong · Chưa thiết kế** · Người viết: FE-1 · Ngày: 17/11/2026
>
> Vị trí trong sitemap: [sitemap.md § 4 – K-04](../sitemap.md#4-danh-mục-màn-hình-ánh-xạ-sitemap--thiết-kế) · Màn hình cha: kiosk.html (bổ sung cho K-01)

| Thuộc tính | Giá trị |
|---|---|
| Mã | K-04 |
| Trang / route | `kiosk.html?station=…&camera=1` |
| Loại | mode |
| Mức | Should |
| Người dùng | Người qua cổng (khi không có máy quét USB) |
| Thành phần code | `features/kiosk` – `CameraScanner` (`html5-qrcode`) |
| Yêu cầu, kịch bản demo | FR-11; demo D1 |

## 1. Mục đích

Cho phép quét QR trên điện thoại bằng webcam của máy kiosk khi demo không có máy quét USB.

## 2. Điều hướng

| Vào màn hình khi | Rời màn hình khi |
|---|---|
| Tham số `camera=1` | Giải mã được chuỗi → `POST /scan` → K-02 |

## 3. API sử dụng

- [`POST /scan`](../../api/gate-agent.md#1-post-scan)

## 4. Trạng thái màn hình

| Trạng thái | Nội dung chính |
|---|---|
| Đang mở camera | Xin quyền camera. |
| Đang quét | Khung ngắm trên nền K-01. |
| Không có quyền / không có camera | Thông báo và tiếp tục dùng ô nhập của K-01. |

## 5. Dữ liệu hiển thị

- Luồng video webcam (không lưu, không gửi đi)

## 6. Quy tắc hành vi

- Cùng một chuỗi giải mã liên tiếp chỉ gửi một lần (chặn quét đúp trong 3 s).
- Ô nhập ẩn của K-01 vẫn hoạt động song song.

## 7. Thiết kế chi tiết

> **TODO – chưa thiết kế.** Điền khi bắt đầu bước thiết kế giao diện.

### 7.1. Wireframe
_TODO: wireframe cho từng trạng thái ở mục 4 (`../../images/screens/K-04-camera-scan.png`)._

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
