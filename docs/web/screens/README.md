# Thiết kế màn hình

> Danh mục và sơ đồ: [../sitemap.md](../sitemap.md). Mỗi file ở đây ứng với **đúng một** dòng của bảng
> "Danh mục màn hình" (sitemap § 4); mã màn hình là khóa liên kết hai bên.

## Danh sách

| Mã | Màn hình | Trang / route | Loại | Mức | Trạng thái |
|---|---|---|---|---|---|
| K-01 | [Chờ quét](K-01-kiosk-idle.md) | `kiosk.html?station=<stationId>` | screen | Must | Mô tả xong · chưa thiết kế |
| K-02 | [Kết quả quét](K-02-scan-result.md) | overlay trên K-01 | overlay | Must | Mô tả xong · chưa thiết kế |
| K-03 | [Lỗi cấu hình trạm](K-03-station-error.md) | `kiosk.html` (thiếu/sai `?station`) | screen | Must | Mô tả xong · chưa thiết kế |
| K-04 | [Quét bằng webcam](K-04-camera-scan.md) | `kiosk.html?station=…&camera=1` | mode | Should | Mô tả xong · chưa thiết kế |
| K-05 | [Bảng điều khiển demo](K-05-demo-panel.md) | `kiosk.html?station=…&demo=1` | panel | Could | Mô tả xong · chưa thiết kế |
| AU-01 | [Đăng nhập](AU-01-login.md) | `portal.html`, `admin.html` | screen | Must | Mô tả xong · chưa thiết kế |
| AU-02 | [Không có quyền truy cập](AU-02-forbidden.md) | `portal.html`, `admin.html` | screen | Must | Mô tả xong · chưa thiết kế |
| P-01 | [Mã QR cá nhân](P-01-qr-code.md) | `portal.html` | screen | Must | Mô tả xong · chưa thiết kế |
| P-02 | [Không có mã QR](P-02-no-qr.md) | `portal.html` (409) | state | Must | Mô tả xong · chưa thiết kế |
| A-01 | [Khung trang quản trị](A-01-admin-shell.md) | `admin.html` | layout | Should | Mô tả xong · chưa thiết kế |
| A-02 | [Tab Nhân viên](A-02-employees-tab.md) | `admin.html#/employees` | tab | Should | Mô tả xong · chưa thiết kế |
| A-03 | [Hộp thoại thao tác QR](A-03-qr-action-dialog.md) | dialog trên `admin.html#/employees` | dialog | Should | Mô tả xong · chưa thiết kế |
| A-04 | [Tab Trạm](A-04-stations-tab.md) | `admin.html#/stations` | tab | Should | Mô tả xong · chưa thiết kế |

## Khung của một file màn hình

| Mục | Nội dung | Trạng thái hiện tại |
|---|---|---|
| Đầu file | Mã, trang/route, loại, mức, người dùng, thành phần code, yêu cầu; liên kết về sitemap | Đã điền |
| 1. Mục đích | Màn hình dùng để làm gì | Đã điền |
| 2. Điều hướng | Vào khi nào, rời đi đâu (khớp sitemap § 5.2) | Đã điền |
| 3. API sử dụng | Liên kết tới mục tương ứng trong `docs/api/*.md` | Đã điền |
| 4. Trạng thái màn hình | Mọi trạng thái cần thiết kế | Đã điền |
| 5. Dữ liệu hiển thị | Trường dữ liệu lấy từ API | Đã điền |
| 6. Quy tắc hành vi | Chu kỳ, timeout, chặn thao tác, câu chữ bắt buộc | Đã điền |
| 7. Thiết kế chi tiết | Wireframe, bố cục, tương tác, responsive, khả năng tiếp cận | **TODO** |
| 8. Tiêu chí chấp nhận | Test UI / kịch bản demo cho từng trạng thái | **TODO** |

## Quy ước

- Tên file: `<mã>-<slug-tiếng-anh>.md`, ví dụ `P-01-qr-code.md`.
- Wireframe / mockup lưu ở `docs/images/screens/<tên file>.png`; nếu vẽ bằng draw.io thì thêm thành tab trong `docs/drawio/web.drawio`.
- Thêm màn hình mới: thêm dòng vào sitemap § 4 (cùng cây ở § 3 và sơ đồ trong `web.drawio`), rồi tạo file theo khung trên.
- Câu chữ hiển thị (tiếng Việt) phải khớp `docs/api/README.md` § 4, § 5.
