-- C7 IAM (spec central-iam mục 6.5). Tài khoản nhân viên liên kết bằng chuỗi employee_code,
-- không có khóa ngoại sang bảng employees của module employee (DP-04).

CREATE TABLE user_accounts (
  username      VARCHAR(32)  PRIMARY KEY,
  password_hash VARCHAR(100) NOT NULL,
  role          VARCHAR(16)  NOT NULL CHECK (role IN ('EMPLOYEE','ADMIN')),
  employee_code VARCHAR(16)  UNIQUE,               -- mỗi nhân viên ≤ 1 tài khoản
  enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
  CHECK (role <> 'EMPLOYEE' OR employee_code IS NOT NULL)
);
