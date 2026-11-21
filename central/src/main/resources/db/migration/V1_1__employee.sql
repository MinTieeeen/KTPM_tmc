-- C2 Employee & QR Management (spec central-employee mục 6.6).

CREATE TABLE factories (
  id   VARCHAR(8)   PRIMARY KEY,
  name VARCHAR(100) NOT NULL
);

CREATE TABLE departments (
  id         VARCHAR(16)  PRIMARY KEY,
  name       VARCHAR(100) NOT NULL,
  factory_id VARCHAR(8)   NOT NULL REFERENCES factories(id)
);

CREATE TABLE employees (
  code          VARCHAR(16)  PRIMARY KEY,
  full_name     VARCHAR(150) NOT NULL,
  department_id VARCHAR(16)  NOT NULL REFERENCES departments(id),
  active        BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE qr_credentials (
  id            UUID         PRIMARY KEY,
  employee_code VARCHAR(16)  NOT NULL REFERENCES employees(code),
  status        VARCHAR(8)   NOT NULL CHECK (status IN ('ACTIVE','REVOKED')),
  issued_at     TIMESTAMPTZ  NOT NULL,
  revoked_at    TIMESTAMPTZ,
  revoke_reason VARCHAR(200),
  CHECK ((status = 'REVOKED') = (revoked_at IS NOT NULL))
);
-- INV-01: tối đa một credential ACTIVE mỗi nhân viên
CREATE UNIQUE INDEX ux_one_active ON qr_credentials(employee_code) WHERE status = 'ACTIVE';
CREATE INDEX ix_cred_employee_issued ON qr_credentials(employee_code, issued_at DESC);

CREATE TABLE signing_keys (
  kid        VARCHAR(32) PRIMARY KEY,
  public_key BYTEA       NOT NULL,                 -- X.509 SubjectPublicKeyInfo (Ed25519)
  active     BOOLEAN     NOT NULL,
  created_at TIMESTAMPTZ NOT NULL
);
