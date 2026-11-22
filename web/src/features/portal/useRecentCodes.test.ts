import { renderHook } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import type { QrToken } from '@/shared/api/centralApi';
import { MAX_RECENT_CODES } from './recentCodeStatus';
import { useRecentCodes } from './useRecentCodes';

const token = (expiresAt: string, clockOffsetMs = 0): QrToken => ({
  raw: 'TMC1.khong-duoc-luu.chu-ky',
  expiresAt,
  refreshAfterSec: 30,
  clockOffsetMs,
});

describe('useRecentCodes', () => {
  it('ghi mã mới với số thứ tự tăng dần, mới nhất trước', () => {
    const { result, rerender } = renderHook(({ t, at }) => useRecentCodes(t, at), {
      initialProps: { t: undefined as QrToken | undefined, at: 0 },
    });
    expect(result.current).toEqual([]);
    rerender({ t: token('2026-11-20T00:01:00Z'), at: 1_000 });
    rerender({ t: token('2026-11-20T00:01:30Z'), at: 31_000 });
    expect(result.current.map((c) => c.seq)).toEqual([2, 1]);
    expect(result.current[0]!.issuedAtMs).toBe(31_000);
  });

  it('không ghi trùng khi render lại với cùng một lần nhận mã', () => {
    const t = token('2026-11-20T00:01:00Z');
    const { result, rerender } = renderHook(({ at }) => useRecentCodes(t, at), { initialProps: { at: 5_000 } });
    rerender({ at: 5_000 });
    rerender({ at: 5_000 });
    expect(result.current).toHaveLength(1);
  });

  it('hạn được quy về đồng hồ thiết bị (trừ độ lệch)', () => {
    const { result } = renderHook(() => useRecentCodes(token('2026-11-20T00:01:00Z', 5_000), 1_000));
    expect(result.current[0]!.expiresAtMs).toBe(Date.parse('2026-11-20T00:01:00Z') - 5_000);
  });

  it('chỉ giữ tối đa 10 mã gần nhất', () => {
    const { result, rerender } = renderHook(({ at }) => useRecentCodes(token('2026-11-20T00:01:00Z'), at), {
      initialProps: { at: 1_000 },
    });
    for (let i = 2; i <= MAX_RECENT_CODES + 5; i++) rerender({ at: i * 1_000 });
    expect(result.current).toHaveLength(MAX_RECENT_CODES);
    expect(result.current[0]!.seq).toBe(MAX_RECENT_CODES + 5);
  });

  it('không giữ chuỗi mã ở bất kỳ trường nào', () => {
    const { result } = renderHook(() => useRecentCodes(token('2026-11-20T00:01:00Z'), 1_000));
    expect(JSON.stringify(result.current)).not.toContain('TMC1.');
  });
});
