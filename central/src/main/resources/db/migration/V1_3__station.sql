-- C3 Gate Station Management (spec central-station mục 6.5).
-- factory_id không có khóa ngoại sang bảng factories của module employee (DP-04).

CREATE TABLE gate_agents (
  gate_id    VARCHAR(32) PRIMARY KEY,
  factory_id VARCHAR(8)  NOT NULL UNIQUE
);

CREATE TABLE gate_stations (
  station_id   VARCHAR(32) PRIMARY KEY,
  factory_id   VARCHAR(8)  NOT NULL,
  direction    VARCHAR(3)  NOT NULL CHECK (direction IN ('IN','OUT')),
  enabled      BOOLEAN     NOT NULL DEFAULT TRUE,
  turnstile_id VARCHAR(32) NOT NULL UNIQUE
);

CREATE TABLE gate_heartbeats (
  gate_id          VARCHAR(32) PRIMARY KEY REFERENCES gate_agents(gate_id),
  sent_at          TIMESTAMPTZ NOT NULL,
  received_at      TIMESTAMPTZ NOT NULL,
  outbox_depth     BIGINT      NOT NULL,
  last_sync_at     TIMESTAMPTZ,
  turnstile_status TEXT        NOT NULL              -- JSON {stationId: UP|DOWN|CIRCUIT_OPEN}
);
