-- C1 Gate Sync & Ingestion (spec central-ingestion mục 6.5). Chỉ module ingestion truy cập các bảng này (DP-04).
-- scan_attempts, attendance_transactions chỉ INSERT (DP-02); idempotent theo event_id (INV-06).

CREATE TABLE scan_attempts (
  event_id      UUID        PRIMARY KEY,
  gate_id       VARCHAR(32) NOT NULL,
  station_id    VARCHAR(32) NOT NULL,
  credential_id UUID,                               -- NULL khi gate không đọc được token (INV-07)
  result        VARCHAR(16) NOT NULL CHECK (result IN ('ACCEPTED','REJECTED','GATE_FAILED')),
  reason        VARCHAR(40),
  occurred_at   TIMESTAMPTZ NOT NULL,
  received_at   TIMESTAMPTZ NOT NULL
);

CREATE TABLE attendance_transactions (
  event_id      UUID        PRIMARY KEY,
  scan_event_id UUID        NOT NULL UNIQUE,
  gate_id       VARCHAR(32) NOT NULL,
  employee_code VARCHAR(16) NOT NULL,
  station_id    VARCHAR(32) NOT NULL,
  direction     VARCHAR(3)  NOT NULL CHECK (direction IN ('IN','OUT')),
  occurred_at   TIMESTAMPTZ NOT NULL,
  received_at   TIMESTAMPTZ NOT NULL
);
CREATE INDEX ix_att_employee_time ON attendance_transactions(employee_code, occurred_at);

-- Nhật ký thay đổi dữ liệu tham chiếu, ghi khi nghe sự kiện của C2/C3; /sync/snapshot đọc từ đây.
CREATE TABLE ref_changes (
  version    BIGSERIAL   PRIMARY KEY,
  kind       VARCHAR(16) NOT NULL CHECK (kind IN ('CREDENTIAL','PUBLIC_KEY','STATION')),
  key        VARCHAR(64) NOT NULL,
  payload    JSONB       NOT NULL,                  -- DTO của sync-contract theo kind
  changed_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX ix_ref_kind_key ON ref_changes(kind, key, version DESC);
