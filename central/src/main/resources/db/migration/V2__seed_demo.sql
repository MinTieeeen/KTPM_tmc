-- Dữ liệu demo (đặc tả M2 mục 8.4). Không có mật khẩu rõ: chỉ có hash BCrypt (cost 10) của mật khẩu demo
-- chung ghi trong docs/api/README.md § 3.1.

-- ---------- employee ----------
INSERT INTO factories (id, name) VALUES
  ('BD', 'Nha may Binh Duong'),
  ('LA', 'Nha may Long An');

INSERT INTO departments (id, name, factory_id) VALUES
  ('BD-ASM', 'Assembly',        'BD'),
  ('BD-QC',  'Quality Control', 'BD'),
  ('LA-WH',  'Warehouse',       'LA'),
  ('LA-PKG', 'Packaging',       'LA');

INSERT INTO employees (code, full_name, department_id, active) VALUES
  ('E0001', 'Nguyen Van An',     'BD-ASM', TRUE),
  ('E0002', 'Tran Thi Bich',     'BD-ASM', TRUE),
  ('E0003', 'Le Van Cuong',      'BD-ASM', TRUE),
  ('E0004', 'Pham Thi Dung',     'BD-ASM', TRUE),
  ('E0005', 'Hoang Van Em',      'BD-ASM', TRUE),
  ('E0006', 'Vu Thi Phuong',     'BD-QC',  TRUE),
  ('E0007', 'Dang Van Giang',    'BD-QC',  TRUE),
  ('E0008', 'Bui Thi Hoa',       'BD-QC',  TRUE),
  ('E0009', 'Do Van Hung',       'BD-QC',  TRUE),
  ('E0010', 'Ngo Thi Khanh',     'BD-QC',  TRUE),
  ('E0011', 'Duong Van Long',    'LA-WH',  TRUE),
  ('E0012', 'Ly Thi Mai',        'LA-WH',  TRUE),
  ('E0013', 'Truong Van Nam',    'LA-WH',  TRUE),
  ('E0014', 'Dinh Thi Oanh',     'LA-WH',  TRUE),
  ('E0015', 'Mai Van Phuc',      'LA-WH',  TRUE),
  ('E0016', 'Ha Thi Quynh',      'LA-PKG', TRUE),
  ('E0017', 'Phan Van Son',      'LA-PKG', TRUE),
  ('E0018', 'Cao Thi Thu',       'LA-PKG', TRUE),
  ('E0019', 'Lam Van Uy',        'LA-PKG', FALSE),   -- đã nghỉ việc
  ('E0020', 'Tran Thi Binh',     'LA-PKG', TRUE);

-- Mỗi nhân viên một credential ACTIVE, trừ E0020 (credential đã thu hồi, không có ACTIVE)
INSERT INTO qr_credentials (id, employee_code, status, issued_at, revoked_at, revoke_reason) VALUES
  ('6f1c2d4a-8b9e-4f01-a2b3-c4d5e6f70812', 'E0001', 'ACTIVE', '2026-11-16T01:00:00Z', NULL, NULL),
  ('c0000000-0000-4000-8000-000000000002', 'E0002', 'ACTIVE', '2026-11-16T01:00:00Z', NULL, NULL),
  ('c0000000-0000-4000-8000-000000000003', 'E0003', 'ACTIVE', '2026-11-16T01:00:00Z', NULL, NULL),
  ('c0000000-0000-4000-8000-000000000004', 'E0004', 'ACTIVE', '2026-11-16T01:00:00Z', NULL, NULL),
  ('c0000000-0000-4000-8000-000000000005', 'E0005', 'ACTIVE', '2026-11-16T01:00:00Z', NULL, NULL),
  ('c0000000-0000-4000-8000-000000000006', 'E0006', 'ACTIVE', '2026-11-16T01:00:00Z', NULL, NULL),
  ('c0000000-0000-4000-8000-000000000007', 'E0007', 'ACTIVE', '2026-11-16T01:00:00Z', NULL, NULL),
  ('c0000000-0000-4000-8000-000000000008', 'E0008', 'ACTIVE', '2026-11-16T01:00:00Z', NULL, NULL),
  ('c0000000-0000-4000-8000-000000000009', 'E0009', 'ACTIVE', '2026-11-16T01:00:00Z', NULL, NULL),
  ('c0000000-0000-4000-8000-000000000010', 'E0010', 'ACTIVE', '2026-11-16T01:00:00Z', NULL, NULL),
  ('c0000000-0000-4000-8000-000000000011', 'E0011', 'ACTIVE', '2026-11-16T01:00:00Z', NULL, NULL),
  ('c0000000-0000-4000-8000-000000000012', 'E0012', 'ACTIVE', '2026-11-16T01:00:00Z', NULL, NULL),
  ('c0000000-0000-4000-8000-000000000013', 'E0013', 'ACTIVE', '2026-11-16T01:00:00Z', NULL, NULL),
  ('c0000000-0000-4000-8000-000000000014', 'E0014', 'ACTIVE', '2026-11-16T01:00:00Z', NULL, NULL),
  ('c0000000-0000-4000-8000-000000000015', 'E0015', 'ACTIVE', '2026-11-16T01:00:00Z', NULL, NULL),
  ('c0000000-0000-4000-8000-000000000016', 'E0016', 'ACTIVE', '2026-11-16T01:00:00Z', NULL, NULL),
  ('c0000000-0000-4000-8000-000000000017', 'E0017', 'ACTIVE', '2026-11-16T01:00:00Z', NULL, NULL),
  ('c0000000-0000-4000-8000-000000000018', 'E0018', 'ACTIVE', '2026-11-16T01:00:00Z', NULL, NULL),
  ('c0000000-0000-4000-8000-000000000019', 'E0019', 'ACTIVE', '2026-11-16T01:00:00Z', NULL, NULL),
  ('0a9b8c7d-6e5f-4a3b-9c2d-1e0f2a3b4c5d', 'E0020', 'REVOKED', '2026-11-16T01:00:00Z',
   '2026-11-17T02:00:00Z', 'Mat dien thoai');

-- ---------- station ----------
INSERT INTO gate_agents (gate_id, factory_id) VALUES
  ('gate-bd', 'BD'),
  ('gate-la', 'LA');

INSERT INTO gate_stations (station_id, factory_id, direction, enabled, turnstile_id) VALUES
  ('BD-IN-01',  'BD', 'IN',  TRUE, 'TS-BD-IN-01'),
  ('BD-OUT-01', 'BD', 'OUT', TRUE, 'TS-BD-OUT-01'),
  ('LA-IN-01',  'LA', 'IN',  TRUE, 'TS-LA-IN-01'),
  ('LA-OUT-01', 'LA', 'OUT', TRUE, 'TS-LA-OUT-01');

-- ---------- iam ----------
-- Hash BCrypt chung của mật khẩu demo; e0019 bị vô hiệu hóa vì nhân viên đã nghỉ việc.
INSERT INTO user_accounts (username, password_hash, role, employee_code, enabled)
SELECT 'e' || substr(code, 2), '$2a$10$u9n.nVq7cbtb2HyaIoyrwe6QjsKxiXVmhihGR6QDaMU571l3mrZlS', 'EMPLOYEE', code,
       code <> 'E0019'
FROM (VALUES ('E0001'), ('E0002'), ('E0003'), ('E0004'), ('E0005'), ('E0006'), ('E0007'), ('E0008'),
             ('E0009'), ('E0010'), ('E0011'), ('E0012'), ('E0013'), ('E0014'), ('E0015'), ('E0016'),
             ('E0017'), ('E0018'), ('E0019'), ('E0020')) AS t(code);

-- admin: username: admin, password: Demo@2026
INSERT INTO user_accounts (username, password_hash, role, employee_code, enabled) VALUES
  ('admin', '$2a$10$u9n.nVq7cbtb2HyaIoyrwe6QjsKxiXVmhihGR6QDaMU571l3mrZlS', 'ADMIN', NULL, TRUE);
