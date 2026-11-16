# Load test T9 (k6)

- Mục tiêu: 800 lượt quét trong 3–5 phút cho mỗi nhà máy, độ trễ cổng giả lập 300–500 ms; **p95 < 2 s**, 0 lỗi 5xx (NFR-PER-01/02).
- Script đặt ở `scripts/`, kết quả (`k6 run --summary-export`) lưu ở `results/` để đưa vào phần 9 của báo cáo.
- Token cần được lấy trước: đăng nhập `POST /auth/login` bằng nhiều tài khoản nhân viên mẫu rồi gọi `GET /portal/qr-token` (mỗi token chỉ dùng được một lần).
