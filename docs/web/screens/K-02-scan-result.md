# K-02 – Kết quả quét

> Trạng thái: **Mô tả xong · Chưa thiết kế** · Người viết: FE-1 · Ngày: 17/11/2026
>
> Vị trí trong sitemap: [sitemap.md § 4 – K-02](../sitemap.md#4-danh-mục-màn-hình-ánh-xạ-sitemap--thiết-kế) · Màn hình cha: K-01 Chờ quét

| Thuộc tính | Giá trị |
|---|---|
| Mã | K-02 |
| Trang / route | overlay trên K-01 |
| Loại | overlay |
| Mức | Must |
| Người dùng | Người qua cổng |
| Thành phần code | `features/kiosk` – `ResultOverlay`; `shared/ui` – `reasonTexts` |
| Yêu cầu, kịch bản demo | FR-14, NFR-PER-01, NFR-AVL-02, NFR-AVL-03, INV-05; demo D1, D2, D5 |

## 1. Mục đích

Hiển thị kết quả một lượt quét trong 3 s bằng màu và câu chữ dễ hiểu; không bao giờ báo xanh khi cổng chưa xác nhận mở.

## 2. Điều hướng

| Vào màn hình khi | Rời màn hình khi |
|---|---|
| K-01 nhận phản hồi `POST /scan`, lỗi mạng hoặc quá 2000 ms | Sau 3 s → K-01 |

## 3. API sử dụng

- [`POST /scan`](../../api/gate-agent.md#1-post-scan)

## 4. Trạng thái màn hình

| Trạng thái | Nội dung chính |
|---|---|
| ACCEPTED (xanh) | "Mời qua", `employeeCode`, chiều VÀO/RA, `elapsedMs`. |
| REJECTED (đỏ) | Câu theo `reason` (bảng ở [api/README § 5](../../api/README.md#5-scan-results-and-reject-reasons-shared-by-gate-agent-and-kiosk)). |
| GATE_FAILED (cam) | "Cổng chưa mở – vui lòng gặp bảo vệ". |
| Không xác định (cam) | Lỗi mạng, HTTP 5xx hoặc quá 2000 ms: "Không xác định – vui lòng gặp bảo vệ". |

## 5. Dữ liệu hiển thị

- `result`, `reason`, `employeeCode`, `direction`, `elapsedMs` (không có họ tên – DP-12)

## 6. Quy tắc hành vi

- Chỉ `result == ACCEPTED` mới hiện màu xanh.
- `reason` lạ: REJECTED → "Mã QR không hợp lệ", GATE_FAILED → câu của GATE_FAILED.
- Tự đóng sau 3 s; trong lúc hiện vẫn chặn lượt quét mới.

## 7. Thiết kế chi tiết

> **TODO – chưa thiết kế.** Điền khi bắt đầu bước thiết kế giao diện.

### 7.1. Wireframe
_TODO: wireframe cho từng trạng thái ở mục 4 (`../../images/screens/K-02-scan-result.png`)._

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
