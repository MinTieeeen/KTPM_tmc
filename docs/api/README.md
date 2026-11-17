# TMC – API specifications

> Status: Draft · Version 1.0.0 · Date: 2026-11-17
> Owners: BE-1 (Gate Agent), BE-2 (Central, Sync, Simulator) · Reviewers: FE-1, FE-2 (see § 8)
>
> These files are the **contract** between front-end and back-end and between the two back-end
> quanta. Code must follow them. A change goes through the process in § 8 (approval and changelog).

## 1. Files

Each API has a human-readable document (Markdown, read this first) and a machine-readable
**OpenAPI 3.0.3** file with the same content (used for type generation, mocks and contract tests).
If the two ever disagree, fix both in the same PR; the YAML is authoritative for tools.

| API | Document | OpenAPI | Server | Default base URL | Consumers |
|---|---|---|---|---|---|
| Gate Agent | [gate-agent.md](gate-agent.md) | [gate-agent.openapi.yaml](gate-agent.openapi.yaml) | Gate Agent (Q1) | `http://localhost:8081` (gate-bd), `:8082` (gate-la) | Kiosk (FE-1), guard, demo scripts |
| Central (user-facing) | [central.md](central.md) | [central.openapi.yaml](central.openapi.yaml) | Central (Q2) | `http://localhost:8080` | Portal and Admin (FE-2) |
| Sync | [sync.md](sync.md) | [sync.openapi.yaml](sync.openapi.yaml) | Central (Q2) – `/sync/**` | `http://localhost:8080` | Gate Agent G4 (BE-1) |
| Turnstile simulator | [turnstile-sim.md](turnstile-sim.md) | [turnstile-sim.openapi.yaml](turnstile-sim.openapi.yaml) | Turnstile simulator | `http://localhost:9001` (BD), `:9002` (LA) | Gate Agent G2 (BE-1), `sim.sh` |

The OpenAPI files can be opened in Swagger Editor / Redoc, used to generate TypeScript types, and
compared with what the running servers publish at `/v3/api-docs` (springdoc, Swagger UI at
`/swagger-ui.html`).

### 1.1. Endpoint index

| Method | Path | Server | Auth | Purpose |
|---|---|---|---|---|
| POST | `/scan` | Gate | none | Validate a QR token, open the turnstile |
| GET | `/status` | Gate | none | Connectivity / outbox / turnstile health (kiosk banner) |
| GET | `/guard/pending` | Gate | none | Scans left PENDING after a restart |
| POST | `/admin/simulate-offline` | Gate | none, `demo` profile only | Simulate WAN loss |
| POST | `/auth/login` | Central | public | Log in, get a JWT |
| GET | `/auth/me` | Central | JWT, any role | Session check |
| GET | `/portal/qr-token` | Central | JWT, `EMPLOYEE` | Signed dynamic QR token of the caller |
| GET | `/employees` | Central | JWT, `ADMIN` | Employees + credential status |
| POST | `/employees/{code}/qr/issue` | Central | JWT, `ADMIN` | Issue / re-issue a QR credential |
| POST | `/employees/{code}/qr/revoke` | Central | JWT, `ADMIN` | Revoke the active QR credential |
| GET | `/stations` | Central | JWT, `ADMIN` | Stations, gate online/offline, outbox depth |
| POST | `/sync/events` | Central | `X-Gate-Key` | Push outbox events (idempotent) |
| GET | `/sync/snapshot` | Central | `X-Gate-Key` | Pull reference-data delta |
| POST | `/sync/heartbeat` | Central | `X-Gate-Key` | Gate heartbeat |
| POST | `/turnstiles/{id}/open` | Simulator | none | Open once (idempotent on `requestId`) |
| GET | `/turnstiles/{id}/commands/{requestId}` | Simulator | none | Was the command executed? |
| GET | `/turnstiles/{id}/status`, `/turnstiles` | Simulator | none | Turnstile status |
| GET, PUT | `/admin/behavior` | Simulator | none | Read / change simulated behaviour |

Also public on both applications: static pages (`/kiosk.html` on the gate, `/portal.html`,
`/admin.html` on Central, `/assets/**`), `/actuator/health`, `/swagger-ui.html`, `/v3/api-docs/**`.

## 2. Common conventions

| Topic | Rule |
|---|---|
| Format | `application/json; charset=utf-8` for requests and successful responses; errors use `application/problem+json`. |
| Field names | `camelCase`. Enum values `UPPER_SNAKE_CASE`. |
| Timestamps | ISO-8601 **UTC** with `Z`, millisecond precision optional: `2026-11-20T07:58:01.120Z`. Front-ends convert to Asia/Ho_Chi_Minh only for display. |
| Identifiers | Events, scans, credentials: UUID v4 strings. Employee code `E` + 4 digits (`E0012`). Station `BD-IN-01`. Gate `gate-bd`. Factory `BD` / `LA`. |
| Null / absent | Fields with no value are **omitted** (never sent as `null`). Clients treat absent and `null` the same. Optional fields are those not listed in `required`. |
| Unknown fields | Clients and servers **ignore** unknown response/request fields so that adding an optional field is non-breaking. |
| Lists | Returned as a JSON array (no envelope, no pagination in the MVP). Empty result → `[]`. |
| Numbers | Counters are 64-bit integers (`int64`); durations in API fields are in milliseconds (`elapsedMs`) or seconds (`refreshAfterSec`) as their name says. |
| Status codes | `200` OK with body, `204` OK without body, `400` invalid request, `401` not authenticated, `403` wrong role, `404` not found, `409` business conflict, `500` server error, `504` simulated timeout (simulator only). |
| Business outcome of a scan | Always `200` with `result` = `ACCEPTED` / `REJECTED` / `GATE_FAILED` (a rejected scan is not an HTTP error). |
| CORS | Not enabled. Pages are served by the same origin as their API; in development the Vite dev server proxies the API paths. |
| Caching | `GET /portal/qr-token` sends `Cache-Control: no-store`. Other responses are not cached by clients. |
| Logging | Never log QR `raw` strings, JWTs, passwords or gate keys (NFR-PRI-03). Log `eventId`, `cid`, `username` only. |

### 2.1. Errors (RFC 9457 Problem Details)

```json
{
  "type": "about:blank",
  "title": "Conflict",
  "status": 409,
  "detail": "QR credential has been revoked",
  "code": "CREDENTIAL_REVOKED"
}
```

- `code` is stable and machine-readable: **clients branch on `code` (and `status`), never on `title`
  or `detail`**. `detail` is English, for developers, and may change.
- `VALIDATION_ERROR` additionally carries `errors: [{ "field": "reason", "message": "…" }]`.

## 3. Authentication

### 3.1. Users (Central)

```
Browser                         Central
  │  POST /auth/login {username, password}
  │ ─────────────────────────────►  200 {accessToken, expiresAt, role, employeeCode?}
  │                                  401 INVALID_CREDENTIALS (same for wrong password and disabled account)
  │  store accessToken in sessionStorage
  │  GET /auth/me   Authorization: Bearer <accessToken>
  │ ─────────────────────────────►  200 {username, role, employeeCode?}  | 401 UNAUTHENTICATED
  │  any protected call with the same header
  │ ─────────────────────────────►  2xx | 401 → drop token, show login | 403 → "You do not have access"
```

- JWT RS256, `iss = tmc-central`, valid **8 h**, no refresh token: after expiry the next call returns
  401 and the user logs in again. Logout = remove the token on the client.
- Treat the token as opaque; do not decode it on the client.
- Roles: `EMPLOYEE` (Portal) and `ADMIN` (Admin). Authorisation matrix:

| Path | public | EMPLOYEE | ADMIN | Gate key |
|---|---|---|---|---|
| `/auth/login`, static pages, `/actuator/health`, API docs | ✔ | ✔ | ✔ | – |
| `/auth/me` | 401 | ✔ | ✔ | – |
| `/portal/qr-token` | 401 | ✔ | 403 | – |
| `/employees/**`, `/stations` | 401 | 403 | ✔ | – |
| `/sync/**` | 401 | 401 | 401 | ✔ |

**Demo accounts** (seeded by `V2__seed_demo.sql`): `e0001` … `e0020` (role `EMPLOYEE`, linked to
`E0001` … `E0020`; `e0019` is disabled), `admin` (role `ADMIN`). All share the demo password
**`Demo@2026`** (only its BCrypt hash is stored in the seed). Demo only – never reuse elsewhere.

### 3.2. Gate Agent → Central

Header `X-Gate-Key: <key>` on every `/sync/**` call. Keys: `GATE_API_KEY` in `deploy/env/gate-*.env`,
`GATE_API_KEYS=gate-bd=…,gate-la=…` in `deploy/env/central.env` (demo values only; never commit real keys).

### 3.3. Kiosk, guard, simulator

No authentication: these endpoints are only reachable from the factory LAN (ADR-08).

## 4. Error codes

| `code` | HTTP | Returned by | Client action |
|---|---|---|---|
| `VALIDATION_ERROR` | 400 | all | Bug in the client; show a generic error. Field details in `errors`. |
| `INVALID_CREDENTIALS` | 401 | `POST /auth/login` | Show *"Wrong credentials or account locked"*. |
| `UNAUTHENTICATED` | 401 | Central (JWT endpoints) | Drop the token, show the login form. |
| `FORBIDDEN` | 403 | Central (JWT endpoints) | Show *"You do not have access"*. |
| `EMPLOYEE_NOT_FOUND` | 404 | `/employees/{code}/qr/*` | Reload the list. |
| `CREDENTIAL_REVOKED` | 409 | `GET /portal/qr-token` | Hide the QR, show *"Your QR code has been revoked, please contact HR"*. |
| `NO_ACTIVE_CREDENTIAL` | 409 | `GET /portal/qr-token`, `POST …/qr/revoke` | Portal: *"You have no QR code yet, please contact HR"*. Admin: reload. |
| `EMPLOYEE_INACTIVE` | 409 | `GET /portal/qr-token`, `POST …/qr/issue` | Portal: *"Your employee record is inactive"*. Admin: show the message. |
| `CONCURRENT_UPDATE` | 409 | `POST …/qr/issue` | Reload and retry. |
| `LOCAL_STORE_ERROR` | 500 | Gate `/scan`, `/guard/pending` | Kiosk: *"Unknown result – please see the guard"*. |
| `INVALID_GATE_KEY` | 401 | `/sync/**` | Gate: configuration error, keep the outbox, log once a minute. |
| `UNSUPPORTED_CONTRACT_VERSION` | 400 | `POST /sync/events` | Gate: keep the outbox, log ERROR (version mismatch). |
| `BATCH_TOO_LARGE` | 400 | `POST /sync/events` | Gate: bug, batches are ≤ 200. |
| `GATE_MISMATCH` | 403 / 400 | `/sync/events` / `/sync/heartbeat` | Gate: configuration error (`GATE_ID` vs key). |
| `UNKNOWN_GATE` | 403 | `/sync/**` | Gate: not registered at Central. |
| `SIMULATED_ERROR` | 500 | Simulator `open` | Gate maps to `GATE_ERROR`. |
| `SIMULATED_TIMEOUT` | 504 | Simulator `open` | Gate times out first (`GATE_TIMEOUT`). |

Event-level rejections inside a `BatchAck` (`INVALID_PAYLOAD`, `UNKNOWN_EVENT_TYPE`,
`UNKNOWN_STATION`, `STATION_NOT_IN_FACTORY`) are not HTTP errors – see `sync.openapi.yaml`.

## 5. Scan results and reject reasons (shared by Gate Agent and Kiosk)

The kiosk UI is in Vietnamese; the exact texts below are part of the contract (`web/shared` keeps
them in one table).

| `result` | `reason` | Meaning | Kiosk text (vi) | Screen |
|---|---|---|---|---|
| `ACCEPTED` | – | Turnstile opened, attendance recorded | *Mời qua – {employeeCode} ({VÀO/RA})* | green |
| `REJECTED` | `EXPIRED`, `NOT_YET_VALID` | Token outside its 60 s window (±30 s skew) | Mã QR đã hết hạn, vui lòng làm mới | red |
| `REJECTED` | `NONCE_REUSED` | Same token scanned twice (screenshot replay) | Mã QR đã được sử dụng | red |
| `REJECTED` | `BAD_SIGNATURE`, `INVALID_FORMAT`, `UNKNOWN_KEY` | Not a genuine TMC token | Mã QR không hợp lệ | red |
| `REJECTED` | `CREDENTIAL_REVOKED`, `CREDENTIAL_NOT_FOUND` | Credential revoked or unknown at this gate | Mã QR đã bị thu hồi hoặc không tồn tại | red |
| `REJECTED` | `EMPLOYEE_INACTIVE` | Employee no longer active | Nhân viên không còn hiệu lực | red |
| `REJECTED` | `UNKNOWN_STATION`, `STATION_DISABLED` | Station unknown or disabled | Trạm đang tạm ngừng | red |
| `REJECTED` | `ANTI_PASSBACK` | Entry after an entry | Bạn đã quét vào trước đó | red |
| `REJECTED` | `OFFLINE_THRESHOLD_EXCEEDED` | Gate offline too long – entry closed (exit stays open) | Mất kết nối quá lâu – vui lòng gặp bảo vệ | red |
| `GATE_FAILED` | `GATE_TIMEOUT`, `GATE_ERROR`, `CIRCUIT_OPEN` | Rules passed but the turnstile did not confirm | Cổng chưa mở – vui lòng gặp bảo vệ | orange |
| (no response in 2 s, network error, HTTP 5xx) | – | Outcome unknown | Không xác định – vui lòng gặp bảo vệ | orange |

Unknown `reason` values (added later) must fall back to *"Mã QR không hợp lệ"* (red) for `REJECTED`
and *"Cổng chưa mở – vui lòng gặp bảo vệ"* (orange) for `GATE_FAILED`.

## 6. Front-end integration guide

### 6.1. Kiosk (`kiosk.html?station=BD-IN-01[&camera=1]`) – FE-1

| Step | Rule |
|---|---|
| Input | A hidden input keeps focus at all times; the USB scanner types the token and Enter. Trim, ignore empty input. |
| Send | `POST /scan {stationId, raw}` with a **2000 ms** client timeout (`AbortController`). One scan at a time: ignore new input while a request is in flight (prevents double scans). |
| Show | Map `result`/`reason` with § 5. Show `employeeCode`, the direction and `elapsedMs`. Return to the idle screen after **3 s**. |
| Never | Retry `/scan` automatically; show green for anything but `result == ACCEPTED`. |
| Banner | Poll `GET /status` every **3 s**. `online == false` → *"OFFLINE – {outboxDepth} bản ghi chờ đồng bộ"*. `offlineThresholdExceeded` on an `IN` station → *"Chiều vào tạm ngừng"*. |

### 6.2. Portal (`portal.html`) – FE-2

| Step | Rule |
|---|---|
| Session | On load: token in `sessionStorage`? → `GET /auth/me`; require `role == EMPLOYEE`. Else show login. |
| QR | `GET /portal/qr-token`; render `raw` (level M); countdown to `expiresAt`. Refresh every `refreshAfterSec` (30 s) and on `visibilitychange` → visible. |
| Errors | `409` → hide QR, show § 4 text, keep polling every 30 s. `401` → login. Network error → keep the current QR until `expiresAt`, then show *"Không lấy được mã QR, kiểm tra kết nối"*. |
| Extra | "Copy string" button copies `raw`. |

### 6.3. Admin (`admin.html`) – FE-2

| Tab | Rule |
|---|---|
| Session | Require `role == ADMIN` (`GET /auth/me`). |
| Employees | `GET /employees?department=&q=` (debounce search 300 ms). Department options = distinct `departmentId`/`departmentName` of the unfiltered list. **Revoke**: ask for a reason (3–200 chars) + confirm → `POST …/qr/revoke`; **Issue / Re-issue** → `POST …/qr/issue`. Replace the row with the returned `EmployeeView`. |
| Stations | `GET /stations` every **5 s**; show online/offline, last heartbeat (relative time), outbox depth, turnstile status. |

### 6.4. Types and mocks

- Generate TypeScript types from these files (no back-end needed):
  `npm run gen:api:docs` (in `web/`) → `src/shared/api/gate.schema.ts`, `central.schema.ts`.
  Once the back-ends run, `npm run gen:api` regenerates from `/v3/api-docs`; the result must be
  equivalent – a difference means the back-end drifted from the contract.
- MSW handlers in `web/src/mocks` return the `examples` of these files.

## 7. Back-end implementation checklist

- springdoc must publish the same paths, schemas, required fields and enums as these files
  (`operationId` equal to the one here). Use `@Schema(requiredMode = REQUIRED)` on required fields.
- Jackson: `default-property-inclusion: non_null`, `write-dates-as-timestamps: false`,
  `FAIL_ON_UNKNOWN_PROPERTIES = false` (already in `application.yml` except the last one).
- Return `ProblemDetail` with the `code` property for every error, including Spring Security's
  401/403 (custom `AuthenticationEntryPoint` / `AccessDeniedHandler`) and Bean Validation (400).
- Contract tests: for each file, a test loads the YAML and checks the running application's
  `/v3/api-docs` for missing paths / schemas.

## 8. Change process

1. A change is proposed by a PR that edits the Markdown document **and** the YAML, and adds a line to § 9.
2. Approval: Gate Agent API → FE-1; Central user API → FE-2; Sync API and Simulator → BE-1 and BE-2.
3. Non-breaking (new optional field, new endpoint, new enum value with documented fallback): minor
   version + 1 (`1.1.0`). Breaking (remove/rename a field, change a type or meaning, new required
   field): major version + 1; for the Sync API also `contractVersion` + 1.
4. Code changes that implement the API change go in the same PR or a follow-up linked to it.

## 9. Changelog

| Version | Date | File | Change |
|---|---|---|---|
| 1.0.0 | 2026-11-17 | all | Initial version from `docs/spec` and the M2 implementation spec. |
