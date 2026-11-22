import { describe, expect, it } from 'vitest';
import { codeStatus, countValid } from './recentCodeStatus';
import type { RecentCode } from './recentCodeStatus';

const code = (seq: number, expiresAtMs: number): RecentCode => ({ seq, issuedAtMs: 0, expiresAtMs });

describe('codeStatus', () => {
  it('mã mới nhất còn hạn là "đang hiển thị"', () => {
    expect(codeStatus(code(2, 60_000), 0, 10_000)).toBe('displayed');
  });

  it('mã cũ còn hạn là "còn hiệu lực"', () => {
    expect(codeStatus(code(1, 60_000), 1, 10_000)).toBe('valid');
  });

  it('quá hạn là "đã hết hạn", kể cả khi là mã mới nhất; đúng thời điểm hết hạn cũng tính là hết', () => {
    expect(codeStatus(code(1, 60_000), 1, 60_000)).toBe('expired');
    expect(codeStatus(code(2, 60_000), 0, 70_000)).toBe('expired');
  });
});

describe('countValid', () => {
  it('chỉ đếm mã chưa hết hạn', () => {
    const codes = [code(3, 90_000), code(2, 60_000), code(1, 30_000)];
    expect(countValid(codes, 10_000)).toBe(3);
    expect(countValid(codes, 40_000)).toBe(2);
    expect(countValid(codes, 100_000)).toBe(0);
    expect(countValid([], 0)).toBe(0);
  });
});
