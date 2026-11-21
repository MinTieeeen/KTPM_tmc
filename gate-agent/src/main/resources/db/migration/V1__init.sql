-- G3 Local Store & Outbox – lược đồ SQLite của Gate Agent (spec gate-agent-localstore mục 6.4).
-- Thời điểm lưu dạng chuỗi ISO-8601 UTC độ dài cố định (micro giây) để so sánh chuỗi được.

-- ---------- Dữ liệu tham chiếu (G4 ghi qua CredentialWriter, G1 chỉ đọc) ----------
CREATE TABLE credentials (
  credential_id   TEXT PRIMARY KEY,                 -- UUID
  employee_code   TEXT NOT NULL,
  status          TEXT NOT NULL CHECK (status IN ('ACTIVE','REVOKED')),
  employee_active INTEGER NOT NULL CHECK (employee_active IN (0,1)),
  updated_at      TEXT NOT NULL
);

CREATE TABLE public_keys (
  kid        TEXT PRIMARY KEY,
  public_key TEXT NOT NULL,                         -- X.509 SubjectPublicKeyInfo, base64
  active     INTEGER NOT NULL CHECK (active IN (0,1))
);

CREATE TABLE stations (
  station_id   TEXT PRIMARY KEY,
  factory_id   TEXT NOT NULL,
  direction    TEXT NOT NULL CHECK (direction IN ('IN','OUT')),
  enabled      INTEGER NOT NULL CHECK (enabled IN (0,1)),
  turnstile_id TEXT NOT NULL
);

-- keys: snapshot_version, last_success_at, offline_threshold_min
CREATE TABLE sync_state (
  key   TEXT PRIMARY KEY,
  value TEXT NOT NULL
);

-- ---------- Luồng quét (G1 ghi) ----------
CREATE TABLE scan_attempts (
  event_id      TEXT PRIMARY KEY,
  station_id    TEXT NOT NULL,
  credential_id TEXT,                               -- NULL khi không xác định được (INV-07)
  status        TEXT NOT NULL CHECK (status IN ('PENDING','ACCEPTED','REJECTED','GATE_FAILED')),
  reason        TEXT,
  occurred_at   TEXT NOT NULL,
  finalized_at  TEXT
);
CREATE INDEX ix_scan_pending ON scan_attempts(status) WHERE status = 'PENDING';

CREATE TABLE attendance_transactions (
  event_id      TEXT PRIMARY KEY,
  scan_event_id TEXT NOT NULL UNIQUE REFERENCES scan_attempts(event_id),
  employee_code TEXT NOT NULL,
  station_id    TEXT NOT NULL,
  direction     TEXT NOT NULL CHECK (direction IN ('IN','OUT')),
  occurred_at   TEXT NOT NULL
);

CREATE TABLE used_nonces (
  nonce      TEXT PRIMARY KEY,
  expires_at TEXT NOT NULL
);
CREATE INDEX ix_nonce_exp ON used_nonces(expires_at);

CREATE TABLE presence (
  employee_code  TEXT PRIMARY KEY,
  last_direction TEXT NOT NULL CHECK (last_direction IN ('IN','OUT')),
  last_at        TEXT NOT NULL
);

-- ---------- Transactional outbox (G1 thêm, G4 đánh dấu) ----------
CREATE TABLE outbox (
  seq           INTEGER PRIMARY KEY AUTOINCREMENT,
  event_id      TEXT NOT NULL UNIQUE,
  type          TEXT NOT NULL CHECK (type IN ('SCAN_ATTEMPT_RECORDED','ATTENDANCE_RECORDED')),
  payload       TEXT NOT NULL,                      -- JSON theo sync-contract
  status        TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','SYNCED','REJECTED')),
  reject_reason TEXT,
  created_at    TEXT NOT NULL,
  synced_at     TEXT
);
CREATE INDEX ix_outbox_pending ON outbox(status, seq);
