import { describe, expect, it } from 'vitest';
import { factoryName, formatDate, formatRelative, formatShortTime, formatTime } from './format';

describe('format', () => {
  it('đổi ngày sang giờ VN (UTC+7) dạng dd/MM/yyyy', () => {
    expect(formatDate('2026-11-16T01:00:00Z')).toBe('16/11/2026');
    expect(formatDate('2026-11-16T18:00:00Z')).toBe('17/11/2026');
  });

  it('trả chuỗi rỗng khi chưa có ngày', () => {
    expect(formatDate(undefined)).toBe('');
  });

  it('định dạng giờ theo giờ VN, 24 giờ', () => {
    expect(formatTime(Date.parse('2026-11-16T01:02:03Z'))).toBe('08:02:03');
    expect(formatTime(Date.parse('2026-11-16T17:00:00Z'))).toBe('00:00:00');
    expect(formatShortTime('2026-11-16T01:02:03Z')).toBe('08:02');
  });

  it('thời gian tương đối: giây, phút, giờ, không âm', () => {
    const now = Date.parse('2026-11-20T08:00:00Z');
    expect(formatRelative('2026-11-20T07:59:57Z', now)).toBe('3 giây trước');
    expect(formatRelative('2026-11-20T07:57:00Z', now)).toBe('3 phút trước');
    expect(formatRelative('2026-11-20T05:00:00Z', now)).toBe('3 giờ trước');
    expect(formatRelative('2026-11-20T08:00:05Z', now)).toBe('0 giây trước');
  });

  it('tên nhà máy', () => {
    expect(factoryName('BD')).toBe('Bình Dương');
    expect(factoryName('LA')).toBe('Long An');
    expect(factoryName('XX')).toBe('XX');
  });
});
