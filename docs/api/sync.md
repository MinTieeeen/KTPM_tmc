# Sync API (Gate Agent ↔ Central)

> Version 1.0.0 · `contractVersion` 1 · Owner: BE-2 · Reviewer: BE-1 · Machine-readable contract: [sync.openapi.yaml](sync.openapi.yaml)
> Java records: library `sync-contract` ([spec](../spec/sync-contract.md)) · Common conventions: [README.md](README.md)

The only connector between the two architecture quanta (ADR-01, ADR-05). Served by Central module
`ingestion` (C1), called by Gate Agent module `sync` (G4).

| Environment | Base URL |
|---|---|
| From the host | `http://localhost:8080` |
| Inside Docker network `wan` | `http://central:8080` |

| # | Method | Path | Called by | Period |
|---|---|---|---|---|
| 1 | POST | [`/sync/events`](#1-post-syncevents) | `OutboxPusher` | every 2 s while the outbox is not empty |
| 2 | GET | [`/sync/snapshot`](#2-get-syncsnapshot) | `SnapshotPuller` | every 15 s (must be ≤ 60 s, NFR-SEC-06) |
| 3 | POST | [`/sync/heartbeat`](#3-post-syncheartbeat) | `HeartbeatSender` | every 10 s |

## General rules

| Topic | Rule |
|---|---|
| Authentication | Header `X-Gate-Key: <key>` on every call (one key per gate, replaces mTLS). JWTs are **not** accepted on `/sync/**`. Key missing/unknown → `401 INVALID_GATE_KEY`. |
| Gate identity | The key identifies the gate. Central checks that `gateId` in the body equals it and that every station belongs to that gate's factory (DP-11). |
| Delivery | At-least-once. The gate resends anything not acknowledged; Central is idempotent on `eventId` (INV-06), so resends never create duplicates. |
| Client timeouts | Connect 2 s, read 5 s. Network error, timeout, 5xx and 401 → keep the outbox, retry at the next period. |
| Compatibility | Adding an optional field is backward compatible (receivers ignore unknown fields). Removing/renaming a field or changing its meaning → `contractVersion` + 1, PR approved by BE-1 and BE-2. |

---

## 1. `POST /sync/events`

Push a batch of outbox events (scan attempts and attendance transactions).

### Request – `EventBatch`

| Field | Type | Required | Description |
|---|---|---|---|
| `contractVersion` | integer | yes | Must be `1` |
| `gateId` | string | yes | e.g. `gate-bd`; must match the key |
| `factoryId` | string | yes | e.g. `BD` |
| `events` | array of `EventEnvelope`, 1–200 items | yes | Ordered by outbox sequence, oldest first |

`EventEnvelope`:

| Field | Type | Required | Description |
|---|---|---|---|
| `eventId` | string (uuid) | yes | Generated at the gate; idempotency key; must equal `payload.eventId` |
| `type` | enum `SCAN_ATTEMPT_RECORDED` \| `ATTENDANCE_RECORDED` | yes | Selects the payload type |
| `occurredAt` | string (date-time) | yes | |
| `payload` | `ScanAttemptPayload` or `AttendancePayload` | yes | See below |

`ScanAttemptPayload` (`type = SCAN_ATTEMPT_RECORDED`):

| Field | Type | Required | Description |
|---|---|---|---|
| `eventId` | string (uuid) | yes | Scan attempt id |
| `stationId` | string | yes | |
| `credentialId` | string (uuid) | no | Omitted when the token could not be decoded (INV-07) |
| `result` | enum `ACCEPTED` \| `REJECTED` \| `GATE_FAILED` | yes | `PENDING` attempts are never sent |
| `reason` | string (`RejectReason` name) | no | Omitted when `ACCEPTED` |
| `occurredAt` | string (date-time) | yes | |

`AttendancePayload` (`type = ATTENDANCE_RECORDED`):

| Field | Type | Required | Description |
|---|---|---|---|
| `eventId` | string (uuid) | yes | Transaction id |
| `scanEventId` | string (uuid) | yes | Accepted scan attempt that produced it (unique) |
| `employeeCode` | string `^E[0-9]{4}$` | yes | |
| `stationId` | string | yes | |
| `direction` | enum `IN` \| `OUT` | yes | |
| `occurredAt` | string (date-time) | yes | Time the turnstile confirmed opening |

```json
{
  "contractVersion": 1, "gateId": "gate-bd", "factoryId": "BD",
  "events": [
    { "eventId": "0b9d6c1e-3f5a-4a52-9a0e-7a6c2b1d9e01", "type": "SCAN_ATTEMPT_RECORDED",
      "occurredAt": "2026-11-20T07:58:01.120Z",
      "payload": { "eventId": "0b9d6c1e-3f5a-4a52-9a0e-7a6c2b1d9e01", "stationId": "BD-IN-01",
                   "credentialId": "6f1c2d4a-8b9e-4f01-a2b3-c4d5e6f70812", "result": "ACCEPTED",
                   "occurredAt": "2026-11-20T07:58:01.120Z" } },
    { "eventId": "5e21a7c3-9d0b-4e8f-a1c2-3b4d5e6f7a80", "type": "ATTENDANCE_RECORDED",
      "occurredAt": "2026-11-20T07:58:01.731Z",
      "payload": { "eventId": "5e21a7c3-9d0b-4e8f-a1c2-3b4d5e6f7a80",
                   "scanEventId": "0b9d6c1e-3f5a-4a52-9a0e-7a6c2b1d9e01", "employeeCode": "E0012",
                   "stationId": "BD-IN-01", "direction": "IN", "occurredAt": "2026-11-20T07:58:01.731Z" } }
  ]
}
```

### Response `200 OK` – `BatchAck`

Every event appears in exactly one list:

| Field | Type | Meaning | Gate action |
|---|---|---|---|
| `accepted` | array of uuid | Stored now | mark `SYNCED` |
| `duplicates` | array of uuid | Already stored earlier (same `eventId`) | mark `SYNCED` |
| `rejected` | array of `{ eventId, reason }` | Invalid, will never be accepted | mark `REJECTED`, log WARN, do not resend |

An `eventId` missing from all three lists stays `PENDING` and is resent.

`rejected[].reason`:

| Value | Meaning |
|---|---|
| `INVALID_PAYLOAD` | Payload unreadable, field missing, enum value invalid, or `payload.eventId ≠ eventId` |
| `UNKNOWN_EVENT_TYPE` | `type` not supported |
| `UNKNOWN_STATION` | `stationId` unknown at Central |
| `STATION_NOT_IN_FACTORY` | Station belongs to another factory than the gate |

```json
{ "accepted": ["0b9d6c1e-3f5a-4a52-9a0e-7a6c2b1d9e01"],
  "duplicates": ["5e21a7c3-9d0b-4e8f-a1c2-3b4d5e6f7a80"],
  "rejected": [] }
```

### Errors (whole batch, nothing stored)

| Status | `code` | When | Gate action |
|---|---|---|---|
| 400 | `UNSUPPORTED_CONTRACT_VERSION` | `contractVersion` ≠ 1 | Keep outbox, log ERROR |
| 400 | `BATCH_TOO_LARGE` | More than 200 events | Bug – batches are ≤ 200 |
| 400 | `VALIDATION_ERROR` | Malformed batch envelope | Keep outbox, log ERROR |
| 401 | `INVALID_GATE_KEY` | Key missing or unknown | Keep outbox, log ERROR once a minute |
| 403 | `GATE_MISMATCH` | Body `gateId` differs from the key's gate | Configuration error |
| 403 | `UNKNOWN_GATE` | Gate not registered at Central | Configuration error |
| 500 | – | Storage failure; whole batch rolled back | Resend at the next period |

---

## 2. `GET /sync/snapshot`

Pull reference-data changes (credentials, public keys, stations, settings) since a version.

### Query parameters

| Name | Type | Required | Default | Description |
|---|---|---|---|---|
| `since` | integer (int64) | no | `0` | Last `toVersion` applied by the gate |

Rules:
- Returns the **latest state** of every item changed after `since` (several changes of one item are
  merged into one entry).
- `since = 0` → full reference data (first start or reset local store).
- `since` negative or greater than Central's current version → full data with `fromVersion = 0`.
- No change → empty lists and `toVersion == fromVersion`.
- The gate applies the whole delta in **one** local transaction, then stores `toVersion`. A delta
  whose `fromVersion` differs from the stored version (other than 0) is ignored.

### Response `200 OK` – `SnapshotDelta`

| Field | Type | Description |
|---|---|---|
| `fromVersion` | integer (int64) | Version the delta starts from |
| `toVersion` | integer (int64) | Version after applying it |
| `credentials` | array of `CredentialEntry` | Changed credentials |
| `publicKeys` | array of `PublicKey` | Changed signing public keys |
| `stations` | array of `StationConfig` | Changed stations |
| `settings` | `GateSettings` | Always present |

`CredentialEntry` (no personal name, DP-12):

| Field | Type | Description |
|---|---|---|
| `credentialId` | string (uuid) | |
| `employeeCode` | string `^E[0-9]{4}$` | |
| `status` | enum `ACTIVE` \| `REVOKED` | |
| `employeeActive` | boolean | |
| `updatedAt` | string (date-time) | |

`PublicKey`:

| Field | Type | Description |
|---|---|---|
| `kid` | string | Key id used in QR tokens, e.g. `k-demo` |
| `publicKeyBase64` | string | Ed25519 public key, X.509 SubjectPublicKeyInfo DER, standard base64 |
| `active` | boolean | Only active keys verify tokens |

`StationConfig`:

| Field | Type | Description |
|---|---|---|
| `stationId` | string | `BD-IN-01` |
| `factoryId` | string | `BD` |
| `direction` | enum `IN` \| `OUT` | |
| `enabled` | boolean | Disabled stations reject every scan (INV-03) |
| `turnstileId` | string | `TS-BD-IN-01` |

`GateSettings`:

| Field | Type | Description |
|---|---|---|
| `offlineThresholdMinutes` | integer ≥ 1 | 480 (8 h) by default, 2 in the `demo` profile |

```json
{ "fromVersion": 41, "toVersion": 44,
  "credentials": [ { "credentialId": "0a9b8c7d-6e5f-4a3b-9c2d-1e0f2a3b4c5d", "employeeCode": "E0020",
                     "status": "REVOKED", "employeeActive": true, "updatedAt": "2026-11-20T08:00:00Z" } ],
  "publicKeys": [ { "kid": "k-demo", "publicKeyBase64": "MCowBQYDK2VwAyEAGb9ECWmEzf6FQbrBZ9w7lshQhqowtrbLDFw4rXAxZuE=",
                    "active": true } ],
  "stations": [ { "stationId": "BD-IN-01", "factoryId": "BD", "direction": "IN", "enabled": true,
                  "turnstileId": "TS-BD-IN-01" } ],
  "settings": { "offlineThresholdMinutes": 480 } }
```

### Errors

| Status | `code` | When |
|---|---|---|
| 401 | `INVALID_GATE_KEY` | Key missing or unknown |

---

## 3. `POST /sync/heartbeat`

Report gate health. Stored as the gate's latest heartbeat (a heartbeat with an older `sentAt` than
the stored one is ignored). Central considers a gate offline after 30 s without heartbeat.

### Request – `Heartbeat`

| Field | Type | Required | Description |
|---|---|---|---|
| `gateId` | string | yes | Must match the key |
| `sentAt` | string (date-time) | yes | |
| `outboxDepth` | integer (int64) ≥ 0 | yes | Events waiting in the outbox |
| `lastSyncAt` | string (date-time) | no | Last successful exchange with Central |
| `turnstileStatus` | object `{ stationId: UP \| DOWN \| CIRCUIT_OPEN }` | yes | Turnstile health per station |

```json
{ "gateId": "gate-bd", "sentAt": "2026-11-20T08:00:10Z", "outboxDepth": 0,
  "lastSyncAt": "2026-11-20T08:00:02Z", "turnstileStatus": { "BD-IN-01": "UP", "BD-OUT-01": "CIRCUIT_OPEN" } }
```

### Responses

| Status | `code` | When |
|---|---|---|
| 204 | – | Recorded |
| 400 | `VALIDATION_ERROR` / `GATE_MISMATCH` | Malformed body / body `gateId` differs from the key's gate |
| 401 | `INVALID_GATE_KEY` | Key missing or unknown |
| 403 | `UNKNOWN_GATE` | Gate not registered at Central |
