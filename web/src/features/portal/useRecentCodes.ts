// Ghi lại mỗi mã mới nhận được (trong bộ nhớ, tối đa 10, mất khi tải lại trang); không bao giờ giữ chuỗi `raw`.
import { useEffect, useRef, useState } from 'react';
import type { QrToken } from '@/shared/api/centralApi';
import { MAX_RECENT_CODES } from './recentCodeStatus';
import type { RecentCode } from './recentCodeStatus';

export function useRecentCodes(token: QrToken | undefined, receivedAtMs: number): RecentCode[] {
  const [codes, setCodes] = useState<RecentCode[]>([]);
  const lastRecordedAt = useRef(0);

  useEffect(() => {
    if (!token || receivedAtMs === 0 || receivedAtMs === lastRecordedAt.current) return;
    lastRecordedAt.current = receivedAtMs;
    const expiresAtMs = Date.parse(token.expiresAt) - token.clockOffsetMs;
    setCodes((prev) =>
      [{ seq: (prev[0]?.seq ?? 0) + 1, issuedAtMs: receivedAtMs, expiresAtMs }, ...prev].slice(0, MAX_RECENT_CODES),
    );
  }, [token, receivedAtMs]);

  return codes;
}
