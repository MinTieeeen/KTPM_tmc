// Handler MSW giả lập Central theo docs/api/central.md; có trạng thái nên thu hồi/cấp lại đổi dữ liệu thật.
import { http, HttpResponse } from 'msw';
import type { EmployeeView, Role, StationView } from '@/shared/api/centralApi';
import {
  DEMO_PASSWORD,
  STATION_SEEDS,
  createEmployees,
  createGates,
  isDisabledUser,
  isKnownUser,
} from './central.fixtures';

export interface MockBackend {
  employees: EmployeeView[];
  gates: ReturnType<typeof createGates>;
  /** Số lần gọi từng đường dẫn (để test kiểm tra "chỉ một request"). */
  calls: Record<string, number>;
  /** Độ trễ giả lập (ms) trước khi trả lời. */
  delayMs: number;
  /** Thời hạn mã QR và chu kỳ làm mới (giây); test đặt nhỏ để không phải chờ. */
  qrTtlSec: number;
  qrRefreshAfterSec: number;
}

export function createMockBackend(): MockBackend {
  return {
    employees: createEmployees(),
    gates: createGates(),
    calls: {},
    delayMs: 0,
    qrTtlSec: 60,
    qrRefreshAfterSec: 30,
  };
}

function problem(status: number, code: string, title: string, detail = title) {
  return HttpResponse.json({ type: 'about:blank', title, status, detail, code }, { status });
}

const unauthenticated = () => problem(401, 'UNAUTHENTICATED', 'Unauthorized');
const forbidden = () => problem(403, 'FORBIDDEN', 'Forbidden');

function roleOf(username: string): Role {
  return username === 'admin' ? 'ADMIN' : 'EMPLOYEE';
}

function employeeCodeOf(username: string): string | undefined {
  return username === 'admin' ? undefined : username.toUpperCase();
}

/** Token giả có dạng `mock.<username>`; trả về username hoặc undefined nếu thiếu/sai. */
function userFrom(request: Request): string | undefined {
  const header = request.headers.get('Authorization');
  const match = header?.match(/^Bearer mock\.(.+)$/);
  return match?.[1];
}

let nonce = 0;

export function createCentralHandlers(backend: MockBackend) {
  const track = async (path: string) => {
    backend.calls[path] = (backend.calls[path] ?? 0) + 1;
    if (backend.delayMs > 0) await new Promise((r) => setTimeout(r, backend.delayMs));
  };

  const findEmployee = (code: string) => backend.employees.find((e) => e.code === code);
  const replace = (updated: EmployeeView) => {
    backend.employees = backend.employees.map((e) => (e.code === updated.code ? updated : e));
    return updated;
  };

  return [
    http.post('/auth/login', async ({ request }) => {
      await track('POST /auth/login');
      const body = (await request.json()) as { username?: string; password?: string };
      const username = (body.username ?? '').trim().toLowerCase();
      if (!username || !body.password) return problem(400, 'VALIDATION_ERROR', 'Bad Request');
      if (!isKnownUser(username) || isDisabledUser(username) || body.password !== DEMO_PASSWORD) {
        return problem(401, 'INVALID_CREDENTIALS', 'Unauthorized');
      }
      return HttpResponse.json({
        accessToken: `mock.${username}`,
        expiresAt: new Date(Date.now() + 8 * 3600_000).toISOString(),
        role: roleOf(username),
        employeeCode: employeeCodeOf(username),
      });
    }),

    http.get('/auth/me', async ({ request }) => {
      await track('GET /auth/me');
      const username = userFrom(request);
      if (!username) return unauthenticated();
      return HttpResponse.json({
        username,
        role: roleOf(username),
        employeeCode: employeeCodeOf(username),
      });
    }),

    http.get('/portal/qr-token', async ({ request }) => {
      await track('GET /portal/qr-token');
      const username = userFrom(request);
      if (!username) return unauthenticated();
      if (roleOf(username) !== 'EMPLOYEE') return forbidden();
      const employee = findEmployee(employeeCodeOf(username)!);
      if (!employee) return problem(409, 'NO_ACTIVE_CREDENTIAL', 'Conflict');
      if (!employee.active) return problem(409, 'EMPLOYEE_INACTIVE', 'Conflict');
      if (!employee.credentialStatus) return problem(409, 'NO_ACTIVE_CREDENTIAL', 'Conflict');
      if (employee.credentialStatus === 'REVOKED') return problem(409, 'CREDENTIAL_REVOKED', 'Conflict');
      nonce += 1;
      return HttpResponse.json(
        {
          raw: `TMC1.mock-${employee.code}-${nonce}.signature`,
          expiresAt: new Date(Date.now() + backend.qrTtlSec * 1000).toISOString(),
          refreshAfterSec: backend.qrRefreshAfterSec,
        },
        { headers: { 'Cache-Control': 'no-store' } },
      );
    }),

    http.get('/employees', async ({ request }) => {
      await track('GET /employees');
      const username = userFrom(request);
      if (!username) return unauthenticated();
      if (roleOf(username) !== 'ADMIN') return forbidden();
      const url = new URL(request.url);
      const department = url.searchParams.get('department');
      const q = (url.searchParams.get('q') ?? '').toLowerCase();
      if (q.length > 100) return problem(400, 'VALIDATION_ERROR', 'Bad Request');
      const result = backend.employees.filter(
        (e) =>
          (!department || e.departmentId === department) &&
          (!q || e.code.toLowerCase().includes(q) || e.fullName.toLowerCase().includes(q)),
      );
      return HttpResponse.json(result);
    }),

    http.post('/employees/:code/qr/issue', async ({ request, params }) => {
      await track('POST /employees/:code/qr/issue');
      const username = userFrom(request);
      if (!username) return unauthenticated();
      if (roleOf(username) !== 'ADMIN') return forbidden();
      const employee = findEmployee(String(params.code));
      if (!employee) return problem(404, 'EMPLOYEE_NOT_FOUND', 'Not Found');
      if (!employee.active) return problem(409, 'EMPLOYEE_INACTIVE', 'Conflict');
      nonce += 1;
      return HttpResponse.json(
        replace({
          ...employee,
          credentialStatus: 'ACTIVE',
          credentialId: `00000000-0000-4000-8000-${String(nonce).padStart(12, '0')}`,
          issuedAt: new Date().toISOString(),
        }),
      );
    }),

    http.post('/employees/:code/qr/revoke', async ({ request, params }) => {
      await track('POST /employees/:code/qr/revoke');
      const username = userFrom(request);
      if (!username) return unauthenticated();
      if (roleOf(username) !== 'ADMIN') return forbidden();
      const body = (await request.json()) as { reason?: string };
      const reason = (body.reason ?? '').trim();
      if (reason.length < 3 || reason.length > 200) return problem(400, 'VALIDATION_ERROR', 'Bad Request');
      const employee = findEmployee(String(params.code));
      if (!employee) return problem(404, 'EMPLOYEE_NOT_FOUND', 'Not Found');
      if (employee.credentialStatus !== 'ACTIVE') return problem(409, 'NO_ACTIVE_CREDENTIAL', 'Conflict');
      return HttpResponse.json(replace({ ...employee, credentialStatus: 'REVOKED' }));
    }),

    http.get('/stations', async ({ request }) => {
      await track('GET /stations');
      const username = userFrom(request);
      if (!username) return unauthenticated();
      if (roleOf(username) !== 'ADMIN') return forbidden();
      const now = Date.now();
      const result: StationView[] = STATION_SEEDS.map((seed) => {
        const gate = backend.gates[seed.factoryId];
        // Gate offline: heartbeat dừng ở hơn 2 phút trước; online: vừa nhận.
        const heartbeat = new Date(now - (gate.online ? 3_000 : 150_000)).toISOString();
        return {
          ...seed,
          enabled: true,
          gateId: gate.gateId,
          online: gate.online,
          lastHeartbeatAt: heartbeat,
          outboxDepth: gate.outboxDepth,
          lastSyncAt: heartbeat,
          turnstileStatus: 'UP',
        };
      });
      return HttpResponse.json(result);
    }),
  ];
}
