# Central API (user-facing)

> Version 1.0.0 · Owner: BE-2 · Reviewer: FE-2 · Machine-readable contract: [central.openapi.yaml](central.openapi.yaml)
> Common conventions, error format and error codes: [README.md](README.md) · Gate-to-Central API: [sync.md](sync.md)

User-facing HTTP API of **Central** (Q2 Central Quantum), consumed by the pages served from the
same origin (`portal.html`, `admin.html`).

| Environment | Base URL |
|---|---|
| Central (Docker Compose / local) | `http://localhost:8080` |
| Vite dev server (proxies `/auth/`, `/portal/`, `/employees`, `/stations` to `:8080`) | `http://localhost:5173` |

| # | Method | Path | Auth / role | Used by |
|---|---|---|---|---|
| 1 | POST | [`/auth/login`](#1-post-authlogin) | public | Login form (Portal, Admin) |
| 2 | GET | [`/auth/me`](#2-get-authme) | JWT, any role | Portal, Admin (session check) |
| 3 | GET | [`/portal/qr-token`](#3-get-portalqr-token) | JWT, `EMPLOYEE` | Portal |
| 4 | GET | [`/employees`](#4-get-employees) | JWT, `ADMIN` | Admin – Employees tab |
| 5 | POST | [`/employees/{code}/qr/issue`](#5-post-employeescodeqrissue) | JWT, `ADMIN` | Admin – Employees tab |
| 6 | POST | [`/employees/{code}/qr/revoke`](#6-post-employeescodeqrrevoke) | JWT, `ADMIN` | Admin – Employees tab |
| 7 | GET | [`/stations`](#7-get-stations) | JWT, `ADMIN` | Admin – Stations tab |

## Authentication

Every endpoint except `POST /auth/login` requires:

| Header | Value |
|---|---|
| `Authorization` | `Bearer <accessToken>` (from `POST /auth/login`) |

- Token: JWT RS256, `iss = tmc-central`, valid **8 h**, no refresh token. Treat it as opaque.
- Store it in `sessionStorage` (lost when the tab closes). Logout = delete it on the client.
- Errors common to every protected endpoint:

| Status | `code` | When | Client action |
|---|---|---|---|
| 401 | `UNAUTHENTICATED` | Token missing, malformed, expired, or account disabled after login | Delete the token, show the login form |
| 403 | `FORBIDDEN` | Valid token, wrong role | Show *"Không có quyền truy cập"* |

The server always enforces the role; hiding buttons in the UI is only a convenience (DP-10).

---

## 1. `POST /auth/login`

Log in and obtain an access token. **Public.**

### Request

| Field | Type | Required | Constraints | Description |
|---|---|---|---|---|
| `username` | string | yes | 1–32 chars | Case-insensitive, trimmed by the server (`e0001`, `admin`) |
| `password` | string | yes | 1–128 chars | |

```json
{ "username": "e0001", "password": "<demo password>" }
```

### Response `200 OK`

| Field | Type | Present | Description |
|---|---|---|---|
| `accessToken` | string | always | JWT to send as `Authorization: Bearer <accessToken>` |
| `expiresAt` | string (date-time) | always | Token expiry (login time + 8 h) |
| `role` | enum `EMPLOYEE` \| `ADMIN` | always | Role of the account |
| `employeeCode` | string `^E[0-9]{4}$` | `EMPLOYEE` only | Linked employee |

```json
{ "accessToken": "eyJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJ0bWMtY2VudHJhbCIsInN1YiI6ImUwMDAxIn0.c2ln",
  "expiresAt": "2026-11-20T15:58:00Z", "role": "EMPLOYEE", "employeeCode": "E0001" }
```

### Errors

| Status | `code` | When | Client shows |
|---|---|---|---|
| 400 | `VALIDATION_ERROR` | Field missing or too long | Generic error |
| 401 | `INVALID_CREDENTIALS` | Wrong username, wrong password **or** disabled account (same response for all three) | *"Sai thông tin đăng nhập hoặc tài khoản bị khóa"* |

Disable the submit button while the request is in flight.

---

## 2. `GET /auth/me`

Return the current user. Called when a page loads with a stored token.

### Response `200 OK`

| Field | Type | Present | Description |
|---|---|---|---|
| `username` | string | always | |
| `role` | enum `EMPLOYEE` \| `ADMIN` | always | |
| `employeeCode` | string | `EMPLOYEE` only | |

```json
{ "username": "e0001", "role": "EMPLOYEE", "employeeCode": "E0001" }
```

### Client rules

- `200` and the role matches the page (Portal → `EMPLOYEE`, Admin → `ADMIN`) → continue.
- `200` with another role → *"Không có quyền truy cập"* + logout button.
- `401` → login form.

---

## 3. `GET /portal/qr-token`

Return a freshly signed dynamic QR token for the logged-in employee. **Role `EMPLOYEE`.**

The employee is taken from the JWT – there is **no parameter**, so an employee can only obtain their
own code (FR-03). Each call returns a **new** token (new nonce) valid for 60 s. The response carries
`Cache-Control: no-store`.

### Request

No parameters, no body.

### Response `200 OK`

| Field | Type | Present | Description |
|---|---|---|---|
| `raw` | string (≤ 1024 chars, ≈ 250 typical) | always | String to encode in the QR code: `TMC1.<base64url payload>.<base64url signature>`. Opaque; never log it. |
| `expiresAt` | string (date-time) | always | End of validity (issue time + 60 s). Gates accept ±30 s clock skew. |
| `refreshAfterSec` | integer ≥ 1 | always | Seconds after which to fetch a new token (30) |

```json
{ "raw": "TMC1.eyJ2IjoxLCJjaWQiOiI2ZjFjMmQ0YS04YjllLTRmMDEtYTJiMy1jNGQ1ZTZmNzA4MTIiLC4uLn0.kQ1x…",
  "expiresAt": "2026-11-20T07:59:00Z", "refreshAfterSec": 30 }
```

### Errors

| Status | `code` | When | Portal shows |
|---|---|---|---|
| 401 | `UNAUTHENTICATED` | see [Authentication](#authentication) | Login form |
| 403 | `FORBIDDEN` | Caller is `ADMIN` | *"Không có quyền truy cập"* |
| 409 | `CREDENTIAL_REVOKED` | Latest credential was revoked | *"Mã QR đã bị thu hồi, liên hệ HR"* |
| 409 | `NO_ACTIVE_CREDENTIAL` | No credential was ever issued | *"Bạn chưa được cấp mã QR, liên hệ HR"* |
| 409 | `EMPLOYEE_INACTIVE` | Employee no longer active | *"Hồ sơ nhân viên không còn hiệu lực"* |

```json
{ "type": "about:blank", "title": "Conflict", "status": 409,
  "detail": "QR credential has been revoked", "code": "CREDENTIAL_REVOKED" }
```

### Client rules

| Rule | Detail |
|---|---|
| Render | QR code of `raw` with error-correction level **M**; countdown to `expiresAt`. |
| Refresh | Every `refreshAfterSec` seconds **and** when the tab becomes visible again (`visibilitychange`). The previous token stays valid until it expires. |
| On 409 | Stop showing any QR code, show the message above, keep polling every 30 s (a re-issued credential then appears automatically). |
| Network error | Keep the current QR until `expiresAt`, then show *"Không lấy được mã QR, kiểm tra kết nối"*. |
| Copy | A "Sao chép chuỗi" button copies `raw` (to paste into the kiosk without a camera). |

---

## 4. `GET /employees`

List employees with the status of their latest QR credential. **Role `ADMIN`.** No pagination (20
employees in the demo); sorted by `code` ascending.

### Query parameters

| Name | Type | Required | Description |
|---|---|---|---|
| `department` | string | no | Exact `departmentId`; omit for all departments |
| `q` | string (≤ 100 chars) | no | Case-insensitive substring of the employee code or full name |

Example: `GET /employees?department=BD-ASM&q=nguyen`

### Response `200 OK`

Array (possibly empty) of `EmployeeView`:

| Field | Type | Present | Description |
|---|---|---|---|
| `code` | string `^E[0-9]{4}$` | always | Employee code |
| `fullName` | string | always | |
| `departmentId` | string | always | |
| `departmentName` | string | always | |
| `factoryId` | enum `BD` \| `LA` | always | |
| `active` | boolean | always | `false` when the employee left the company |
| `credentialId` | string (uuid) | if a credential was ever issued | Latest credential |
| `credentialStatus` | enum `ACTIVE` \| `REVOKED` | if a credential was ever issued | Status of the latest credential |
| `issuedAt` | string (date-time) | if a credential was ever issued | Issue time of the latest credential |

```json
[
  { "code": "E0001", "fullName": "Nguyen Van An", "departmentId": "BD-ASM", "departmentName": "Assembly",
    "factoryId": "BD", "active": true, "credentialId": "6f1c2d4a-8b9e-4f01-a2b3-c4d5e6f70812",
    "credentialStatus": "ACTIVE", "issuedAt": "2026-11-16T01:00:00Z" },
  { "code": "E0020", "fullName": "Tran Thi Binh", "departmentId": "LA-PKG", "departmentName": "Packaging",
    "factoryId": "LA", "active": true, "credentialId": "0a9b8c7d-6e5f-4a3b-9c2d-1e0f2a3b4c5d",
    "credentialStatus": "REVOKED", "issuedAt": "2026-11-16T01:00:00Z" }
]
```

### Errors

`400 VALIDATION_ERROR` (`q` too long), `401`, `403` – see [Authentication](#authentication).

### Client rules

- The department filter options are the distinct `departmentId` / `departmentName` pairs of the
  **unfiltered** list (there is no departments endpoint).
- Debounce the search box by 300 ms.
- Buttons per row: **Thu hồi** (revoke) when `credentialStatus == ACTIVE`; **Cấp lại** (issue) always
  when `active == true`.

---

## 5. `POST /employees/{code}/qr/issue`

Issue a new QR credential, or re-issue one. **Role `ADMIN`.** If a credential is already active it is
revoked first (reason `REISSUED`) in the same transaction, so the employee never has two active
credentials (INV-01). The change reaches every connected gate within 60 s.

### Request

| Path parameter | Type | Description |
|---|---|---|
| `code` | string `^E[0-9]{4}$` | Employee code, e.g. `E0012` |

No body.

### Response `200 OK`

The updated `EmployeeView` (same fields as [§ 4](#4-get-employees)), with `credentialStatus = ACTIVE`
and a new `credentialId`. The client replaces the row with it.

### Errors

| Status | `code` | When | Admin action |
|---|---|---|---|
| 401 / 403 | `UNAUTHENTICATED` / `FORBIDDEN` | see [Authentication](#authentication) | |
| 404 | `EMPLOYEE_NOT_FOUND` | No employee with this code | Reload the list |
| 409 | `EMPLOYEE_INACTIVE` | Cannot issue to an inactive employee | Show the message |
| 409 | `CONCURRENT_UPDATE` | Another admin changed the credential at the same time | Reload and retry |

---

## 6. `POST /employees/{code}/qr/revoke`

Revoke the employee's active QR credential. **Role `ADMIN`.** After success the employee's Portal
gets `409 CREDENTIAL_REVOKED` on its next refresh and every connected gate rejects the employee
within 60 s (NFR-SEC-06).

### Request

| Path parameter | Type | Description |
|---|---|---|
| `code` | string `^E[0-9]{4}$` | Employee code |

| Body field | Type | Required | Constraints | Description |
|---|---|---|---|---|
| `reason` | string | yes | 3–200 chars after trimming | Free text, e.g. "Lost phone" |

```json
{ "reason": "Lost phone" }
```

### Response `200 OK`

The updated `EmployeeView` with `credentialStatus = REVOKED`.

### Errors

| Status | `code` | When |
|---|---|---|
| 400 | `VALIDATION_ERROR` | `reason` missing, shorter than 3 or longer than 200 chars |
| 401 / 403 | `UNAUTHENTICATED` / `FORBIDDEN` | see [Authentication](#authentication) |
| 404 | `EMPLOYEE_NOT_FOUND` | No employee with this code |
| 409 | `NO_ACTIVE_CREDENTIAL` | Nothing to revoke |

### Client rules

Ask for the reason and a confirmation dialog before calling.

---

## 7. `GET /stations`

Gate stations with connectivity and outbox status. **Role `ADMIN`.** Polled every **5 s** by the
Stations tab. One row per station, sorted by `stationId`. Connectivity fields come from the latest
heartbeat of the Gate Agent of the station's factory, so stations of the same factory share
`online`, `lastHeartbeatAt`, `outboxDepth` and `lastSyncAt`. A gate is online when its last heartbeat
was received at most 30 s ago.

### Response `200 OK`

Array of `StationView`:

| Field | Type | Present | Description |
|---|---|---|---|
| `stationId` | string | always | e.g. `BD-IN-01` |
| `factoryId` | enum `BD` \| `LA` | always | |
| `direction` | enum `IN` \| `OUT` | always | |
| `enabled` | boolean | always | |
| `gateId` | string | always | Gate Agent of the factory, e.g. `gate-bd` |
| `online` | boolean | always | Heartbeat received in the last 30 s |
| `lastHeartbeatAt` | string (date-time) | if a heartbeat was ever received | |
| `outboxDepth` | integer (int64) ≥ 0 | if a heartbeat was ever received | Events waiting at the gate |
| `lastSyncAt` | string (date-time) | if reported by the gate | Last successful sync of the gate |
| `turnstileStatus` | enum `UP` \| `DOWN` \| `CIRCUIT_OPEN` | if reported by the gate | Health of this station's turnstile |

```json
[
  { "stationId": "BD-IN-01", "factoryId": "BD", "direction": "IN", "enabled": true, "gateId": "gate-bd",
    "online": false, "lastHeartbeatAt": "2026-11-20T07:41:10Z", "outboxDepth": 37,
    "lastSyncAt": "2026-11-20T07:41:10Z", "turnstileStatus": "UP" },
  { "stationId": "LA-IN-01", "factoryId": "LA", "direction": "IN", "enabled": true, "gateId": "gate-la",
    "online": true, "lastHeartbeatAt": "2026-11-20T07:58:05Z", "outboxDepth": 0,
    "lastSyncAt": "2026-11-20T07:58:05Z", "turnstileStatus": "UP" }
]
```

### Errors

`401`, `403` – see [Authentication](#authentication).

### Client rules

Show online/offline, last heartbeat as relative time ("12 s ago"), outbox depth and turnstile status.
