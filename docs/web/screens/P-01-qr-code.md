# P-01 – Mã QR cá nhân

> Trạng thái: **Đã thiết kế (wireframe) · chờ duyệt** · Người viết: FE-2 · Ngày: 17/11/2026
>
> Vị trí trong sitemap: [sitemap.md § 4 – P-01](../sitemap.md#4-danh-mục-màn-hình-ánh-xạ-sitemap--thiết-kế) · Màn hình cha: portal.html

| Thuộc tính | Giá trị |
|---|---|
| Mã | P-01 |
| Trang / route | `portal.html` |
| Loại | screen |
| Mức | Must |
| Người dùng | Nhân viên (điện thoại) |
| Thành phần code | `features/portal` – `QrPage`, `QrCountdown` (`qrcode.react`) |
| Yêu cầu, kịch bản demo | FR-03, NFR-SEC-05; demo D1, D2 |

## 1. Mục đích

Hiển thị mã QR động của chính nhân viên đang đăng nhập; mã tự làm mới nên ảnh chụp lại nhanh chóng mất giá trị.

## 2. Điều hướng

| Vào màn hình khi | Rời màn hình khi |
|---|---|
| Đăng nhập thành công với vai trò EMPLOYEE | 409 → P-02 |
| P-02 nhận lại 200 | 401 / Đăng xuất → AU-01 |

## 3. API sử dụng

- [`GET /portal/qr-token`](../../api/central.md#3-get-portalqr-token)

## 4. Trạng thái màn hình

| Trạng thái | Nội dung chính |
|---|---|
| Đang tải | Lần lấy mã đầu tiên. |
| Hiển thị mã | QR mức sửa lỗi M, đồng hồ "Mã mới sau N giây", mã nhân viên; hạn của mã chỉ hiện khi sắp hết hoặc không làm mới được. |
| Đang làm mới | Giữ mã cũ cho tới khi có mã mới. |
| Mất mạng | Giữ mã hiện tại tới `expiresAt`, sau đó "Không lấy được mã QR, kiểm tra kết nối". |

## 5. Dữ liệu hiển thị

- `raw`, `expiresAt`, `refreshAfterSec`
- `employeeCode` (từ `GET /auth/me`)

## 6. Quy tắc hành vi

- Làm mới mỗi `refreshAfterSec` (30 s) và khi tab hiện lại (`visibilitychange`).
- Hạn của mã (`expiresAt`) theo đồng hồ máy chủ nên được quy về đồng hồ thiết bị bằng độ lệch lấy từ header `Date` của phản hồi (cận trên, chỉ thấp hơn thực tế tối đa 1 s); nhờ đó "Hết hạn sau" không vượt 60 s dù đồng hồ điện thoại lệch.
- Nút "Sao chép chuỗi" copy `raw`.
- Không log `raw`.

## 7. Thiết kế màn hình

### 7.1. Wireframe

Nguồn: [wireframes.drawio](../../drawio/wireframes.drawio), tab `P-01 Mã QR cá nhân`. Số đỏ trên hình ứng với cột **#** ở mục 7.2.

![P-01 wireframe](../../images/screens/P-01-qr-code.png)

### 7.2. Thành phần giao diện

| # | Thành phần | Loại | Dữ liệu (nguồn) | Cách hiển thị |
|---|---|---|---|---|
| 1 | Thanh tiêu đề | App bar | Tĩnh | Nền xanh, chữ trắng 16 px đậm: "Mã QR chấm công". |
| 2 | Nút Đăng xuất | Button secondary | – | Góc phải thanh tiêu đề. |
| 3 | Thông tin nhân viên | Text | `GET /auth/me`: `employeeCode`, `username` | "Nhân viên {employeeCode}" 18 px đậm; dòng dưới "tài khoản {username}" 12 px xám. API không trả họ tên. |
| 4 | Mã QR | QR image | `GET /portal/qr-token` → `raw` | 200×200 px, mức sửa lỗi M, viền trắng 8 px, căn giữa; luôn là mã mới nhất. |
| 5 | Đếm ngược | Progress bar + text | Lúc nhận mã + `refreshAfterSec` (đổi mã); `expiresAt` (hạn) so với giờ hiện tại | **Đồng hồ chính** "Mã mới sau {n} giây" (n ≤ `refreshAfterSec`) kèm thanh co dần theo chu kỳ đổi mã; khi n = 0 mà chưa có mã mới: "Đang lấy mã mới…". **Dòng phụ** "Hết hạn sau {m} giây (lúc HH:mm:ss)" (m khớp `expiresAt`, ≤ 60) **chỉ hiện** khi m ≤ 10 hoặc lần làm mới gần nhất lỗi (hiện [8]); m ≤ 10 thì chữ cam. Bình thường dòng này ẩn để người dùng không thấy hai con số khác nhau (30 s và 60 s). Cả hai cập nhật mỗi giây. |
| 6 | Ghi chú | Text | `refreshAfterSec` | 12 px xám: "Mã tự đổi mỗi {refreshAfterSec} giây và chỉ dùng được một lần." |
| 7 | Nút Sao chép chuỗi | Button secondary | `raw` | Icon sao chép (Copy) + "Sao chép chuỗi"; dùng khi kiosk không có camera. |
| 8 | Cảnh báo mất mạng | Alert cảnh báo | Lỗi khi làm mới | Hộp cam: "Không lấy được mã QR mới – kiểm tra kết nối". |
| 9 | Thông báo đã sao chép | Toast | Kết quả sao chép | Viên thuốc xám đậm "Đã sao chép", tự ẩn sau 2 s. |
| 10 | Nút đổi giao diện | Nhóm 3 nút (Sáng / Tối / Theo hệ thống) | `localStorage` khóa `tmc.theme` | Trên thanh tiêu đề, cạnh nút Đăng xuất; mặc định theo hệ điều hành, lựa chọn được lưu và áp ngay khi tải trang (không nhấp nháy). Mã QR luôn đen trên nền trắng ở cả hai chế độ. |

### 7.3. Hành động (input → output)

| Mã | Hành động | Input | Xử lý / API | Output |
|---|---|---|---|---|
| P01-A1 | Lấy mã | Mở trang (sau khi qua guard) | `GET /auth/me`, `GET /portal/qr-token` | Hiện [3] [4] [5] |
| P01-A2 | Làm mới định kỳ | Hết `refreshAfterSec` giây; tab hiện lại (`visibilitychange`) | `GET /portal/qr-token` | Thay [4], đặt lại [5]; mã cũ giữ tới khi có mã mới |
| P01-A3 | Mất mạng khi làm mới | Lỗi mạng / 5xx | Thử lại ở chu kỳ sau | Hiện [8]; giữ [4] tới `expiresAt`, sau đó ẩn [4] và chỉ còn [8] |
| P01-A4 | Sao chép chuỗi | Bấm [7] | `navigator.clipboard.writeText(raw)` | Hiện [9] trong 2 s |
| P01-A5 | Mã bị thu hồi / không có | 409 | – | Chuyển P-02 |
| P01-A6 | Đăng xuất / hết phiên | Bấm [2] hoặc nhận 401 | Xóa token | Chuyển AU-01 |

## 8. Tiêu chí chấp nhận

- [ ] QR đổi sau mỗi 30 s và mỗi lần mở lại tab; "Mã mới sau" không vượt 30 s; bình thường không có dòng "Hết hạn sau". Khi không làm mới được (ngắt mạng) hoặc mã còn ≤ 10 s thì hiện "Hết hạn sau {m} giây" với m khớp `expiresAt` và ≤ 60 (D1).
- [ ] Mã do P-01 hiển thị được kiosk chấp nhận; quét lại cùng mã → K-02 "Mã QR đã được sử dụng" (D2).
- [ ] Ngắt mạng điện thoại → [8] xuất hiện, QR biến mất khi hết hạn.
- [ ] Thu hồi mã ở Admin → trong lần làm mới kế tiếp chuyển P-02 (D3).
