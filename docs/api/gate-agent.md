# Gate Agent API

> Version 1.0.0 · Owner: BE-1 · Reviewer: FE-1 · Machine-readable contract: [gate-agent.openapi.yaml](gate-agent.openapi.yaml)
> Common conventions, error format and reason texts: [README.md](README.md)

HTTP API of the **Gate Agent** (Q1 Gate Quantum). One instance runs in each factory.

| Instance | Base URL |
|---|---|
| gate-bd (Binh Duong) | `http://localhost:8081` |
| gate-la (Long An) | `http://localhost:8082` |
| Vite dev server (proxies `/scan`, `/status`, `/admin/simulate-offline` to `:8081`) | `http://localhost:5173` |

**Authentication:** none. The Gate Agent is only reachable from the factory LAN and the kiosk has no
user account (ADR-08).

| # | Method | Path | Used by |
|---|---|---|---|
| 1 | POST | [`/scan`](#1-post-scan) | Kiosk |
| 2 | GET | [`/status`](#2-get-status) | Kiosk (every 3 s) |
| 3 | GET | [`/guard/pending`](#3-get-guardpending) | Guard, demo operator |
| 4 | POST | [`/admin/simulate-offline`](#4-post-adminsimulate-offline) | Demo scripts (profile `demo` only) |

---

## 1. `POST /scan`

Validate a QR token and open the turnstile (flow F1). The server verifies the token signature and
validity window, checks the station, the offline threshold, the credential, the nonce and
anti-passback, persists the attempt, asks the turnstile to open and records the attendance
transaction.

**The business outcome is always returned with HTTP 200.** Look at `result`:

| `result` | Meaning | Kiosk screen |
|---|---|---|
| `ACCEPTED` | Turnstile confirmed open, attendance recorded | green |
| `REJECTED` | A rule failed; nothing was sent to the turnstile | red + reason |
| `GATE_FAILED` | Rules passed but the turnstile did not confirm opening | orange: "see the guard" |

### Request

| Header | Value |
|---|---|
| `Content-Type` | `application/json` |

| Field | Type | Required | Constraints | Description |
|---|---|---|---|---|
| `stationId` | string | yes | `^[A-Z]{2}-(IN\|OUT)-[0-9]{2}$` | Station of this kiosk, e.g. `BD-IN-01` (from the URL `kiosk.html?station=…`). |
| `raw` | string | yes | 1–1024 chars | Exact string read by the scanner, trailing newline trimmed. Expected `TMC1.<payload>.<signature>`, but any non-blank string is accepted – format problems come back as `REJECTED / INVALID_FORMAT`, not HTTP 400. Never log it. |

```json
{
  "stationId": "BD-IN-01",
  "raw": "TMC1.eyJ2IjoxLCJjaWQiOiI2ZjFjMmQ0YS0uLi4ifQ.MEUCIQ..."
}
```

### Response `200 OK`

| Field | Type | Present | Description |
|---|---|---|---|
| `result` | enum `ACCEPTED` \| `REJECTED` \| `GATE_FAILED` | always | Outcome. |
| `reason` | enum `RejectReason` | `REJECTED`, `GATE_FAILED` | Why the scan was not accepted (§ [Reject reasons](#reject-reasons)). |
| `employeeCode` | string `^E[0-9]{4}$` | `ACCEPTED`, `GATE_FAILED` | Employee code only, never the name. **Never** present for `REJECTED`. |
| `direction` | enum `IN` \| `OUT` | when the station exists locally | Direction of the station; omitted only for `UNKNOWN_STATION`. |
| `elapsedMs` | integer (int64) ≥ 0 | always | Server processing time in ms. |
| `eventId` | string (uuid) | always | Id of the scan attempt (used by the guard to reconcile). |

Examples:

```json
// ACCEPTED – gate opened
{ "result": "ACCEPTED", "employeeCode": "E0012", "direction": "IN", "elapsedMs": 612,
  "eventId": "0b9d6c1e-3f5a-4a52-9a0e-7a6c2b1d9e01" }

// REJECTED – the same token was scanned again (replayed screenshot)
{ "result": "REJECTED", "reason": "NONCE_REUSED", "direction": "IN", "elapsedMs": 41,
  "eventId": "7c1e2a90-5b7d-4c11-8f0e-1d2c3b4a5e60" }

// REJECTED – token expired
{ "result": "REJECTED", "reason": "EXPIRED", "direction": "IN", "elapsedMs": 18,
  "eventId": "2d4f6a80-1c3e-4b5d-8f70-9a1b2c3d4e5f" }

// GATE_FAILED – turnstile did not answer in 800 ms and did not execute the command
{ "result": "GATE_FAILED", "reason": "GATE_TIMEOUT", "employeeCode": "E0012", "direction": "IN",
  "elapsedMs": 1046, "eventId": "9a8b7c6d-5e4f-4a3b-8c2d-1e0f9a8b7c6d" }
```

### Errors

| Status | `code` | When | Kiosk |
|---|---|---|---|
| 400 | `VALIDATION_ERROR` | `stationId`/`raw` missing or blank, `raw` > 1024 chars, malformed JSON | Generic error (client bug) |
| 500 | `LOCAL_STORE_ERROR` | The attempt could not be persisted locally; the turnstile was **not** reported open | *"Không xác định – vui lòng gặp bảo vệ"* |

```json
{ "type": "about:blank", "title": "Bad Request", "status": 400, "detail": "Request validation failed",
  "code": "VALIDATION_ERROR", "errors": [ { "field": "raw", "message": "must not be blank" } ] }
```

### Rules for the client

| Rule | Detail |
|---|---|
| Timeout | Abort after **2000 ms** (`AbortController`) and show *"Không xác định – vui lòng gặp bảo vệ"*. The server answers in ≈ 1.1 s normally, ≈ 1.3 s worst case. |
| No retry | Never retry automatically: the nonce is consumed by the first call, a retry would return `NONCE_REUSED`. |
| One at a time | Ignore scanner input while a request is in flight (prevents double scans). |
| Green only for `ACCEPTED` | Any other result, error or timeout must never show green (NFR-AVL-03). |
| Display | Show `employeeCode`, direction (VÀO/RA) and `elapsedMs`; return to idle after **3 s**. |
| Not idempotent | Each call creates a new scan attempt with a new `eventId`. |

### Reject reasons

| `reason` | Result | Meaning |
|---|---|---|
| `INVALID_FORMAT` | REJECTED | Not a `TMC1.` token or cannot be decoded |
| `UNKNOWN_KEY` | REJECTED | Signed with a key id (`kid`) unknown to this gate |
| `BAD_SIGNATURE` | REJECTED | Signature does not match (tampered or forged) |
| `EXPIRED` | REJECTED | Now > `exp` + 30 s |
| `NOT_YET_VALID` | REJECTED | Now < `iat` − 30 s |
| `UNKNOWN_STATION` | REJECTED | `stationId` not in the gate's station list |
| `STATION_DISABLED` | REJECTED | Station disabled (INV-03) |
| `OFFLINE_THRESHOLD_EXCEEDED` | REJECTED | Entry station and the gate has been offline longer than the threshold |
| `CREDENTIAL_NOT_FOUND` | REJECTED | Credential not in the gate's local copy |
| `CREDENTIAL_REVOKED` | REJECTED | Credential revoked |
| `EMPLOYEE_INACTIVE` | REJECTED | Employee no longer active |
| `NONCE_REUSED` | REJECTED | Token already used once |
| `ANTI_PASSBACK` | REJECTED | Entry after an entry (INV-04) |
| `GATE_TIMEOUT` | GATE_FAILED | Turnstile did not answer within 800 ms and status check did not confirm |
| `GATE_ERROR` | GATE_FAILED | Turnstile returned an error |
| `CIRCUIT_OPEN` | GATE_FAILED | Too many consecutive turnstile failures – commands paused for 30 s |

The Vietnamese kiosk texts for each reason are in [README.md § 5](README.md#5-scan-results-and-reject-reasons-shared-by-gate-agent-and-kiosk).

---

## 2. `GET /status`

Gate connectivity, outbox depth and turnstile health. Polled by the kiosk every **3 s** to show the
OFFLINE banner (NFR-AVL-04).

### Request

No parameters, no body.

### Response `200 OK`

| Field | Type | Present | Description |
|---|---|---|---|
| `gateId` | string | always | e.g. `gate-bd` |
| `online` | boolean | always | `true` when the last successful exchange with Central is ≤ 30 s old |
| `lastSyncAt` | string (date-time) | when the gate synced at least once | Last successful exchange with Central |
| `outboxDepth` | integer (int64) ≥ 0 | always | Events waiting to be pushed to Central |
| `offlineThresholdExceeded` | boolean | always | `true` when offline longer than the threshold (8 h; 2 min in `demo`) or never synced – entry scans are rejected |
| `pendingReconcile` | integer ≥ 0 | always | Scans stuck in `PENDING` for more than 5 s |
| `turnstiles` | object `{ stationId: UP \| DOWN \| CIRCUIT_OPEN }` | always | Turnstile health per station |

`TurnstileHealth`: `UP` last command succeeded (or none sent yet) · `DOWN` last command failed ·
`CIRCUIT_OPEN` commands paused for 30 s after 5 consecutive failures.

```json
// online
{ "gateId": "gate-bd", "online": true, "lastSyncAt": "2026-11-20T07:58:02.004Z", "outboxDepth": 0,
  "offlineThresholdExceeded": false, "pendingReconcile": 0,
  "turnstiles": { "BD-IN-01": "UP", "BD-OUT-01": "UP" } }

// offline, threshold exceeded
{ "gateId": "gate-bd", "online": false, "lastSyncAt": "2026-11-20T07:41:10.512Z", "outboxDepth": 37,
  "offlineThresholdExceeded": true, "pendingReconcile": 1,
  "turnstiles": { "BD-IN-01": "UP", "BD-OUT-01": "CIRCUIT_OPEN" } }
```

### Rules for the client

| Condition | Kiosk shows |
|---|---|
| `online == false` | Banner *"OFFLINE – {outboxDepth} bản ghi chờ đồng bộ"* |
| `offlineThresholdExceeded == true` and the station is `IN` | Additionally *"Chiều vào tạm ngừng"* (exit stations keep working) |
| The call itself fails or takes > 2 s | Keep the last known state, show *"Kiosk không kết nối được dịch vụ cổng"* |

---

## 3. `GET /guard/pending`

Scans that were persisted as `PENDING` (before the turnstile call) but never finalised because the
Gate Agent stopped. Only attempts that occurred **before the current process started** are listed,
sorted by `occurredAt` ascending. Read-only.

### Response `200 OK`

Array (possibly empty) of:

| Field | Type | Present | Description |
|---|---|---|---|
| `eventId` | string (uuid) | always | Scan attempt id |
| `stationId` | string | always | Station |
| `credentialId` | string (uuid) | when known | Credential that was scanned |
| `occurredAt` | string (date-time) | always | Time of the scan |

```json
[ { "eventId": "3e5f7a9b-1c2d-4e6f-8a0b-2c4d6e8f0a1b", "stationId": "BD-IN-01",
    "credentialId": "6f1c2d4a-8b9e-4f01-a2b3-c4d5e6f70812", "occurredAt": "2026-11-20T07:58:01.120Z" } ]
```

### Errors

| Status | `code` | When |
|---|---|---|
| 500 | `LOCAL_STORE_ERROR` | Local SQLite store unavailable |

---

## 4. `POST /admin/simulate-offline`

Simulates loss of the WAN link to Central. **Demo profile only**
(`tmc.gate.fault-injection.enabled=true`); otherwise the endpoint does not exist (404).

While `offline` is `true` the Gate Agent behaves as if Central were unreachable: the outbox grows,
`GET /status` reports `online: false`, and after the offline threshold (2 min in `demo`) entry scans
are rejected with `OFFLINE_THRESHOLD_EXCEEDED`.

### Request

| Field | Type | Required | Description |
|---|---|---|---|
| `offline` | boolean | yes | `true` to cut, `false` to restore |

```json
{ "offline": true }
```

### Responses

| Status | Body | When |
|---|---|---|
| 204 | – | Applied |
| 400 | Problem `VALIDATION_ERROR` | `offline` missing or not a boolean |
| 404 | – | Fault injection disabled (not the `demo` profile) |
