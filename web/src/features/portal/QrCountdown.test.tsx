import { cleanup, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it } from 'vitest';
import { QrCountdown } from './QrCountdown';

afterEach(cleanup);

// Mã nhận lúc BASE, đổi mã sau 30 s, hết hạn sau 60 s (00:00:00Z = 07:00:00 giờ VN năm 2026).
const BASE = Date.parse('2026-11-20T00:00:00Z');

function renderAt(secondsSinceReceived: number, options: { refreshing?: boolean; refreshFailed?: boolean } = {}) {
  return render(
    <QrCountdown
      expiresAtMs={BASE + 60_000}
      receivedAtMs={BASE}
      refreshAfterSec={30}
      nowMs={BASE + secondsSinceReceived * 1000}
      refreshing={options.refreshing ?? false}
      refreshFailed={options.refreshFailed ?? false}
    />,
  );
}

const expiryText = () => screen.queryByText(/Hết hạn sau/);

describe('QrCountdown – đồng hồ chính "Mã mới sau"', () => {
  it('vừa nhận mã: mới sau 30 s và KHÔNG hiện hạn (tránh mâu thuẫn với chu kỳ đổi mã)', () => {
    renderAt(0);
    expect(screen.getByText('Mã mới sau 30 giây')).toBeTruthy();
    expect(expiryText()).toBeNull();
  });

  it('giữa chu kỳ: đếm lùi theo chu kỳ đổi mã, vẫn không hiện hạn', () => {
    renderAt(20);
    expect(screen.getByText('Mã mới sau 10 giây')).toBeTruthy();
    expect(expiryText()).toBeNull();
  });

  it('quá hạn đổi mã nhưng mã còn dài hạn: "Đang lấy mã mới…", vẫn không hiện hạn', () => {
    renderAt(31, { refreshing: true });
    expect(screen.getByText('Đang lấy mã mới…')).toBeTruthy();
    expect(screen.queryByText(/Mã mới sau/)).toBeNull();
    expect(expiryText()).toBeNull();
  });

  it('thanh tiến trình theo chu kỳ đổi mã (không theo hạn 60 s)', () => {
    renderAt(15);
    expect(screen.getByRole('progressbar', { name: 'Thời gian tới lần đổi mã' }).getAttribute('aria-valuenow')).toBe('50');
  });
});

describe('QrCountdown – dòng "Hết hạn sau" chỉ hiện khi có ý nghĩa', () => {
  it('làm mới lỗi: hiện hạn của mã hiện tại kèm giờ hết hạn', () => {
    renderAt(31, { refreshFailed: true });
    expect(screen.getByText('Hết hạn sau 29 giây (lúc 07:01:00)')).toBeTruthy();
  });

  it('sắp hết hạn (≤ 10 s) dù chưa có lỗi: hiện hạn', () => {
    renderAt(52);
    expect(screen.getByText('Hết hạn sau 8 giây (lúc 07:01:00)')).toBeTruthy();
  });

  it('đúng ngưỡng 10 s thì hiện, còn 11 s thì ẩn', () => {
    renderAt(50);
    expect(screen.getByText('Hết hạn sau 10 giây (lúc 07:01:00)')).toBeTruthy();
    cleanup();
    renderAt(49);
    expect(expiryText()).toBeNull();
  });

  it('không đếm âm khi đã quá hạn', () => {
    renderAt(90);
    expect(screen.getByText('Hết hạn sau 0 giây (lúc 07:01:00)')).toBeTruthy();
  });
});
