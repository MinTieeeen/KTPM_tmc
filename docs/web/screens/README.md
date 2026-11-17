# Thiết kế màn hình

> Danh mục và sơ đồ: [../sitemap.md](../sitemap.md). Mỗi file ở đây ứng với **đúng một** dòng của bảng
> "Danh mục màn hình" (sitemap § 4); mã màn hình là khóa liên kết hai bên.

## Danh sách

| Mã | Màn hình | Trang / route | Loại | Mức | Trạng thái |
|---|---|---|---|---|---|
| K-01 | [Chờ quét](K-01-kiosk-idle.md) | `kiosk.html?station=<stationId>` | screen | Must | Đã thiết kế · [wireframe](../../images/screens/K-01-kiosk-idle.png) |
| K-02 | [Kết quả quét](K-02-scan-result.md) | overlay trên K-01 | overlay | Must | Đã thiết kế · [wireframe](../../images/screens/K-02-scan-result.png) |
| K-03 | [Lỗi cấu hình trạm](K-03-station-error.md) | `kiosk.html` (thiếu/sai `?station`) | screen | Must | Đã thiết kế · [wireframe](../../images/screens/K-03-station-error.png) |
| K-04 | [Quét bằng webcam](K-04-camera-scan.md) | `kiosk.html?station=…&camera=1` | mode | Should | Đã thiết kế · [wireframe](../../images/screens/K-04-camera-scan.png) |
| K-05 | [Bảng điều khiển demo](K-05-demo-panel.md) | `kiosk.html?station=…&demo=1` | panel | Could | Đã thiết kế · [wireframe](../../images/screens/K-05-demo-panel.png) |
| AU-01 | [Đăng nhập](AU-01-login.md) | `portal.html`, `admin.html` | screen | Must | Đã thiết kế · [wireframe](../../images/screens/AU-01-login.png) |
| AU-02 | [Không có quyền truy cập](AU-02-forbidden.md) | `portal.html`, `admin.html` | screen | Must | Đã thiết kế · [wireframe](../../images/screens/AU-02-forbidden.png) |
| P-01 | [Mã QR cá nhân](P-01-qr-code.md) | `portal.html` | screen | Must | Đã thiết kế · [wireframe](../../images/screens/P-01-qr-code.png) |
| P-02 | [Không có mã QR](P-02-no-qr.md) | `portal.html` (409) | state | Must | Đã thiết kế · [wireframe](../../images/screens/P-02-no-qr.png) |
| A-01 | [Khung trang quản trị](A-01-admin-shell.md) | `admin.html` | layout | Should | Đã thiết kế · [wireframe](../../images/screens/A-01-admin-shell.png) |
| A-02 | [Tab Nhân viên](A-02-employees-tab.md) | `admin.html#/employees` | tab | Should | Đã thiết kế · [wireframe](../../images/screens/A-02-employees-tab.png) |
| A-03 | [Hộp thoại thao tác QR](A-03-qr-action-dialog.md) | dialog trên `admin.html#/employees` | dialog | Should | Đã thiết kế · [wireframe](../../images/screens/A-03-qr-action-dialog.png) |
| A-04 | [Tab Trạm](A-04-stations-tab.md) | `admin.html#/stations` | tab | Should | Đã thiết kế · [wireframe](../../images/screens/A-04-stations-tab.png) |

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
| 7.1. Wireframe | Ảnh xuất từ tab tương ứng của `docs/drawio/wireframes.drawio`; số đỏ đánh dấu thành phần | Đã vẽ |
| 7.2. Thành phần giao diện | Mỗi số trên wireframe: tên, **loại** (button, input, table, badge…), dữ liệu và nguồn, cách hiển thị | Đã điền |
| 7.3. Hành động | Mỗi thao tác hoặc sự kiện: input → xử lý / API → output | Đã điền |
| 8. Tiêu chí chấp nhận | Checklist kiểm thử UI / kịch bản demo | Đã điền |

## Quy ước

- Tên file: `<mã>-<slug-tiếng-anh>.md`, ví dụ `P-01-qr-code.md`.
- Wireframe: mọi màn hình nằm chung trong `docs/drawio/wireframes.drawio` (mỗi màn hình một tab, tên tab = mã + tên màn hình); ảnh xuất ở `docs/images/screens/<tên file>.png`. Sinh lại: `python artifacts/drawio/02_spec/wireframes.py tmc/docs/drawio/wireframes.drawio` rồi xuất PNG từng tab bằng draw.io (`-p <số tab>`).
- Wireframe là low-fidelity: thể hiện bố cục, thành phần và trạng thái; màu chỉ dùng cho ý nghĩa (xanh = thành công, đỏ = lỗi/từ chối, cam = cảnh báo).
- Thêm màn hình mới: thêm dòng vào sitemap § 4 (cùng cây ở § 3 và sơ đồ trong `web.drawio`), rồi tạo file theo khung trên.
- Câu chữ hiển thị (tiếng Việt) phải khớp `docs/api/README.md` § 4, § 5.
