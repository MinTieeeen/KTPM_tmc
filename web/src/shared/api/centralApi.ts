// Nơi duy nhất Portal/Admin gọi HTTP tới Central (docs/api/central.md); gắn Bearer, 401 → authSession.
import { authSession } from './authSession';
import { networkError, toApiError } from './apiError';
import type { components } from './central.schema';

type Schemas = components['schemas'];
export type Role = Schemas['Role'];
export type LoginResponse = Schemas['LoginResponse'];
export type CurrentUser = Schemas['CurrentUser'];
export type QrTokenResponse = Schemas['QrTokenResponse'];
export type EmployeeView = Schemas['EmployeeView'];
export type StationView = Schemas['StationView'];

interface RequestOptions {
  method?: 'GET' | 'POST';
  body?: unknown;
  /** false cho /auth/login: 401 ở đây là sai thông tin, không phải hết phiên. */
  auth?: boolean;
}

/** Mã QR kèm độ lệch đồng hồ: (giờ máy chủ − giờ thiết bị) lúc nhận phản hồi, tính từ header Date (độ phân giải 1 s). */
export interface QrToken extends QrTokenResponse {
  clockOffsetMs: number;
}

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  return (await requestWithClock<T>(path, options)).data;
}

async function requestWithClock<T>(
  path: string,
  options: RequestOptions = {},
): Promise<{ data: T; clockOffsetMs: number }> {
  const { method = 'GET', body, auth = true } = options;
  const headers: Record<string, string> = { Accept: 'application/json' };
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  const token = authSession.getToken();
  if (auth && token) headers.Authorization = `Bearer ${token}`;

  let res: Response;
  try {
    res = await fetch(path, {
      method,
      headers,
      cache: 'no-store',
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch {
    throw networkError();
  }
  const receivedAtMs = Date.now();

  if (!res.ok) {
    if (res.status === 401 && auth) authSession.onUnauthorized();
    if (res.status === 403 && auth) authSession.onForbidden();
    throw await toApiError(res);
  }
  const serverMs = Date.parse(res.headers.get('Date') ?? '');
  // Date bị cắt xuống giây: lấy cận trên (+999 ms) để đếm ngược chỉ thấp hơn thực tế tối đa 1 s, không bao giờ cao hơn.
  const clockOffsetMs = Number.isFinite(serverMs) ? serverMs + 999 - receivedAtMs : 0;
  return { data: (await res.json()) as T, clockOffsetMs };
}

export const centralApi = {
  login: (username: string, password: string) =>
    request<LoginResponse>('/auth/login', { method: 'POST', body: { username, password }, auth: false }),

  me: () => request<CurrentUser>('/auth/me'),

  getQrToken: async (): Promise<QrToken> => {
    const { data, clockOffsetMs } = await requestWithClock<QrTokenResponse>('/portal/qr-token');
    return { ...data, clockOffsetMs };
  },

  listEmployees: (department?: string, q?: string) => {
    const params = new URLSearchParams();
    if (department) params.set('department', department);
    if (q) params.set('q', q);
    const query = params.toString();
    return request<EmployeeView[]>(`/employees${query ? `?${query}` : ''}`);
  },

  issueQr: (code: string) =>
    request<EmployeeView>(`/employees/${encodeURIComponent(code)}/qr/issue`, { method: 'POST' }),

  revokeQr: (code: string, reason: string) =>
    request<EmployeeView>(`/employees/${encodeURIComponent(code)}/qr/revoke`, {
      method: 'POST',
      body: { reason },
    }),

  listStations: () => request<StationView[]>('/stations'),
};
