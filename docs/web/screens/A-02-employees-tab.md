# A-02 – Tab Nhân viên

> Trạng thái: **Đã thiết kế (wireframe) · chờ duyệt** · Người viết: FE-2 · Ngày: 17/11/2026
>
> Vị trí trong sitemap: [sitemap.md § 4 – A-02](../sitemap.md#4-danh-mục-màn-hình-ánh-xạ-sitemap--thiết-kế) · Màn hình cha: A-01 Khung trang quản trị

| Thuộc tính | Giá trị |
|---|---|
| Mã | A-02 |
| Trang / route | `admin.html#/employees` |
| Loại | tab |
| Mức | Should |
| Người dùng | Quản trị viên |
| Thành phần code | `features/admin` – `EmployeesTab` |
| Yêu cầu, kịch bản demo | FR-02, FR-04; demo D3 |

## 1. Mục đích

Xem nhân viên và trạng thái QR credential; khởi động thao tác thu hồi / cấp lại (demo D3).

## 2. Điều hướng

| Vào màn hình khi | Rời màn hình khi |
|---|---|
| Mặc định khi mở Admin | Thu hồi / Cấp lại → A-03 |
| Chọn tab Nhân viên |  |

## 3. API sử dụng

- [`GET /employees`](../../api/central.md#4-get-employees)

## 4. Trạng thái màn hình

| Trạng thái | Nội dung chính |
|---|---|
| Đang tải | Lần tải đầu. |
| Danh sách | Mã, họ tên, bộ phận, nhà máy, trạng thái nhân viên, trạng thái credential, ngày cấp, nút thao tác. |
| Không có kết quả | Bộ lọc không khớp nhân viên nào. |
| Lỗi tải | Thông báo + nút Thử lại. |

## 5. Dữ liệu hiển thị

- `EmployeeView[]`
- Bộ lọc bộ phận lấy từ danh sách chưa lọc

## 6. Quy tắc hành vi

- Tìm kiếm debounce 300 ms (`q`), lọc theo `department`.
- Nút **Thu hồi** khi `credentialStatus = ACTIVE`; **Cấp lại** khi nhân viên `active`.
- Sau A-03 thành công, thay dòng bằng `EmployeeView` trả về.

## 7. Thiết kế màn hình

### 7.1. Wireframe

Nguồn: [wireframes.drawio](../../drawio/wireframes.drawio), tab `A-02 Tab Nhân viên`. Số đỏ trên hình ứng với cột **#** ở mục 7.2.

![A-02 wireframe](../../images/screens/A-02-employees-tab.png)

### 7.2. Thành phần giao diện

| # | Thành phần | Loại | Dữ liệu (nguồn) | Cách hiển thị |
|---|---|---|---|---|
| 1 | Ô tìm kiếm | Input search | Người dùng nhập → tham số `q` | Placeholder "Tìm theo mã hoặc họ tên…", icon kính lúp (Search), rộng 360 px; tối đa 100 ký tự. |
| 2 | Lọc bộ phận | Select (dropdown) | Các cặp `departmentId`/`departmentName` khác nhau từ danh sách chưa lọc | Mục đầu "Tất cả bộ phận"; các mục sắp theo tên. |
| 3 | Số kết quả | Text | Độ dài danh sách trả về | 13 px xám, bên phải: "{n} nhân viên". |
| 4 | Bảng nhân viên | Table | `GET /employees` → `EmployeeView[]` | Cột: Mã · Họ tên · Bộ phận (`departmentName`) · Nhà máy · Nhân viên · Mã QR · Ngày cấp (`issuedAt`, dd/MM/yyyy giờ VN; trống nếu chưa cấp) · Thao tác. Sắp theo mã. Không phân trang. |
| 5 | Nhãn trạng thái | Badge | `active`; `credentialStatus` | Cột Nhân viên: "Đang làm" (xanh nhạt) / "Đã nghỉ" (xám). Cột Mã QR: "ACTIVE" (xanh) / "REVOKED" (đỏ) / "Chưa cấp" (xám, khi không có credential). |
| 6 | Nút thao tác theo dòng | Button danger / secondary | `credentialStatus`, `active` | "Thu hồi" (đỏ) khi `credentialStatus = ACTIVE`; "Cấp lại" (viền xanh) khi `active = true`. |
| 7 | Trạng thái rỗng | Empty state | Danh sách rỗng | Hộp xám dưới tiêu đề bảng: "Không có nhân viên nào khớp bộ lọc". |
| 8 | Lỗi tải | Alert lỗi + button | Lỗi `GET /employees` | Hộp đỏ "Không tải được danh sách nhân viên." và nút "Thử lại"; thay chỗ bảng. |

### 7.3. Hành động (input → output)

| Mã | Hành động | Input | Xử lý / API | Output |
|---|---|---|---|---|
| A02-A1 | Tải danh sách | Mở tab | `GET /employees` (không tham số) | Hiện [4]; dựng [2] từ kết quả |
| A02-A2 | Tìm kiếm | Gõ vào [1] | Debounce 300 ms → `GET /employees?q=&department=` | Cập nhật [3] [4] hoặc [7] |
| A02-A3 | Lọc bộ phận | Chọn ở [2] | `GET /employees?department=&q=` | Cập nhật [3] [4] hoặc [7] |
| A02-A4 | Thu hồi / Cấp lại | Bấm [6] | – | Mở A-03 với dòng đã chọn |
| A02-A5 | Thử lại | Bấm nút ở [8] | Gọi lại request cuối | Hiện [4] hoặc giữ [8] |
| A02-A6 | Cập nhật sau thao tác | A-03 trả `EmployeeView` | Thay dòng tương ứng | Dòng hiển thị trạng thái mới, không tải lại cả bảng |

## 8. Tiêu chí chấp nhận

- [ ] Mở tab → 20 dòng, E0019 có nhãn "Đã nghỉ" và không có nút Cấp lại; E0020 có "REVOKED" và chỉ có nút Cấp lại.
- [ ] Gõ "nguyen" → chỉ một request sau khi ngừng gõ 300 ms; kết quả rỗng → [7].
- [ ] Backend lỗi → [8]; Thử lại thành công → bảng hiện lại.
