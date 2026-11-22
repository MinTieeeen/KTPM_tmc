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

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
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

  if (!res.ok) {
    if (res.status === 401 && auth) authSession.onUnauthorized();
    if (res.status === 403 && auth) authSession.onForbidden();
    throw await toApiError(res);
  }
  return (await res.json()) as T;
}

export const centralApi = {
  login: (username: string, password: string) =>
    request<LoginResponse>('/auth/login', { method: 'POST', body: { username, password }, auth: false }),

  me: () => request<CurrentUser>('/auth/me'),

  getQrToken: () => request<QrTokenResponse>('/portal/qr-token'),

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
