# Turnstile Simulator API

> Version 1.0.0 · Owner: BE-2 · Reviewer: BE-1 · Machine-readable contract: [turnstile-sim.openapi.yaml](turnstile-sim.openapi.yaml)
> Common conventions: [README.md](README.md) · Specification: [../spec/turnstile-sim.md](../spec/turnstile-sim.md)

Stand-in for the turnstile vendor's controller API, one instance per factory. Called by the Gate
Agent adapter `HttpTurnstileAdapter` (G2) and by the demo script `deploy/scripts/sim.sh`.

| Instance | Base URL |
|---|---|
| turnstile-sim-bd | `http://localhost:9001` (`http://turnstile-sim-bd:9001` in network `lan-bd`) |
| turnstile-sim-la | `http://localhost:9002` (`http://turnstile-sim-la:9001` in network `lan-la`) |

**Authentication:** none (isolated factory LAN).
**Timeouts used by the Gate Agent:** `open` 800 ms, `commands/{requestId}` 200 ms.
**Turnstile ids:** any id is accepted and created on first use; stations use `TS-<stationId>`
(e.g. `TS-BD-IN-01`). State is in memory and lost on restart.

| # | Method | Path | Used by |
|---|---|---|---|
| 1 | POST | [`/turnstiles/{id}/open`](#1-post-turnstilesidopen) | Gate Agent |
| 2 | GET | [`/turnstiles/{id}/commands/{requestId}`](#2-get-turnstilesidcommandsrequestid) | Gate Agent (after a timeout) |
| 3 | GET | [`/turnstiles/{id}/status`](#3-get-turnstilesidstatus) | Tests, operator |
| 4 | GET | [`/turnstiles`](#4-get-turnstiles) | Tests, operator |
| 5 | GET, PUT | [`/admin/behavior`](#5-get--put-adminbehavior) | `sim.sh`, tests |

---

## 1. `POST /turnstiles/{id}/open`

Open the turnstile once. **Idempotent on `requestId`:** a request id that was already executed
returns `200` immediately with `duplicate: true` and the original `at`, whatever the current mode is,
and the turnstile does not open a second time.

### Request

| Path parameter | Type | Description |
|---|---|---|
| `id` | string | Turnstile id, e.g. `TS-BD-IN-01` |

| Body field | Type | Required | Description |
|---|---|---|---|
| `requestId` | string (uuid) | yes | The Gate Agent sends the scan `eventId` |

```json
{ "requestId": "0b9d6c1e-3f5a-4a52-9a0e-7a6c2b1d9e01" }
```

### Behaviour per mode

| Mode | Opens? | Response |
|---|---|---|
| `NORMAL` | yes, unless the random failure hits | `200` after a random latency in `latencyMs`; `500` with probability `failureRate` |
| `TIMEOUT` | no | `504` after 5 s |
| `ERROR` | no | `500` immediately |
| `EXECUTE_THEN_TIMEOUT` | yes | `200` after 5 s (the gate times out first, then finds the command executed) |

### Response `200 OK`

| Field | Type | Description |
|---|---|---|
| `requestId` | string (uuid) | Echo |
| `executed` | boolean | Always `true` for 200 |
| `duplicate` | boolean | `true` when this `requestId` had already been executed |
| `at` | string (date-time) | Time the turnstile opened (the first time) |

```json
{ "requestId": "0b9d6c1e-3f5a-4a52-9a0e-7a6c2b1d9e01", "executed": true, "duplicate": false,
  "at": "2026-11-20T07:58:01.700Z" }
```

### Errors

| Status | `code` | When | Gate Agent maps to |
|---|---|---|---|
| 400 | `VALIDATION_ERROR` | `requestId` missing or not a UUID | `GATE_ERROR` |
| 500 | `SIMULATED_ERROR` | Mode `ERROR` or random failure; did not open | `GATE_ERROR` |
| 504 | `SIMULATED_TIMEOUT` | Mode `TIMEOUT`; did not open | `GATE_TIMEOUT` (the gate's 800 ms timeout fires first) |

---

## 2. `GET /turnstiles/{id}/commands/{requestId}`

Was this open command executed? Answers **immediately**, independent of the mode and latency.
Unknown ids return `executed: false`.

### Response `200 OK`

| Field | Type | Present | Description |
|---|---|---|---|
| `executed` | boolean | always | |
| `at` | string (date-time) | when `executed` is true | Time the turnstile opened |

```json
{ "executed": true, "at": "2026-11-20T07:58:01.700Z" }
```
```json
{ "executed": false }
```

---

## 3. `GET /turnstiles/{id}/status`

### Response `200 OK` – `TurnstileStatus`

| Field | Type | Description |
|---|---|---|
| `turnstileId` | string | |
| `status` | enum `UP` \| `DOWN` | `DOWN` while the mode is `ERROR` |
| `openCount` | integer (int64) | Number of distinct commands executed |

```json
{ "turnstileId": "TS-BD-IN-01", "status": "UP", "openCount": 42 }
```

## 4. `GET /turnstiles`

Array of `TurnstileStatus` for every turnstile seen so far (possibly empty).

---

## 5. `GET` / `PUT /admin/behavior`

Read or change the simulated behaviour. `PUT` is a **partial update**: omitted fields keep their
value; the change applies to the next request.

### `Behavior` (response of both, request of `PUT`)

| Field | Type | Constraints | Default | Description |
|---|---|---|---|---|
| `latencyMs` | array of 2 integers `[min, max]` | `0 ≤ min ≤ max` | `[300, 500]` | Latency range in `NORMAL` mode |
| `failureRate` | number | 0 – 1 | `0.0` | Probability of `500` in `NORMAL` mode |
| `mode` | enum `NORMAL` \| `TIMEOUT` \| `ERROR` \| `EXECUTE_THEN_TIMEOUT` | | `NORMAL` | |

```json
// PUT /admin/behavior – make every open time out (demo D5, test T7)
{ "mode": "TIMEOUT" }

// PUT /admin/behavior – back to normal
{ "mode": "NORMAL", "latencyMs": [300, 500], "failureRate": 0.0 }

// response
{ "latencyMs": [300, 500], "failureRate": 0.0, "mode": "NORMAL" }
```

### Errors

| Status | `code` | When |
|---|---|---|
| 400 | `VALIDATION_ERROR` | `latencyMs` not 2 items or min > max, `failureRate` outside [0, 1], unknown `mode` |
