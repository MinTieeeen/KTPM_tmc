export interface GateStatus {
  gateId: string;
  online: boolean;
  outboxDepth: number;
  lastSyncAt?: string;
  offlineThresholdExceeded: boolean;
  pendingReconcile: number;
  turnstiles: Record<string, string>;
}

export interface ScanResult {
  result: 'ACCEPTED' | 'REJECTED' | 'GATE_FAILED';
  reason?: string;
  direction?: 'IN' | 'OUT';
  employeeCode?: string;
  elapsedMs?: number;
  eventId?: string;
}

export async function gateRequest<T>(path: string, body?: object): Promise<T> {
  const controller = new AbortController();
  const timer = window.setTimeout(() => controller.abort(), 2000);
  try {
    const response = await fetch(path, {
      signal: controller.signal,
      cache: 'no-store',
      ...(body && {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      }),
    });
    if (!response.ok) throw response.status;
    return response.status === 204 ? (undefined as T) : await response.json();
  } finally {
    window.clearTimeout(timer);
  }
}

const expired = 'Mã QR đã hết hạn, vui lòng làm mới';
const credential = 'Mã QR đã bị thu hồi hoặc không tồn tại';
const station = 'Trạm đang tạm ngừng';
const reasons = new Map(Object.entries({
  EXPIRED: expired,
  NOT_YET_VALID: expired,
  NONCE_REUSED: 'Mã QR đã được sử dụng',
  CREDENTIAL_REVOKED: credential,
  CREDENTIAL_NOT_FOUND: credential,
  EMPLOYEE_INACTIVE: 'Nhân viên không còn hiệu lực',
  UNKNOWN_STATION: station,
  STATION_DISABLED: station,
  ANTI_PASSBACK: 'Bạn đã quét vào trước đó',
  OFFLINE_THRESHOLD_EXCEEDED: 'Mất kết nối quá lâu - vui lòng gặp bảo vệ',
}));

export function rejectionText(reason?: string) {
  return reasons.get(reason ?? '') ?? 'Mã QR không hợp lệ';
}
