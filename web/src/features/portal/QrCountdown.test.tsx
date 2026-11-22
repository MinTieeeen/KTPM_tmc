import { cleanup, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it } from 'vitest';
import { QrCountdown } from './QrCountdown';

afterEach(cleanup);

// Mã nhận lúc BASE, đổi mã sau 30 s, hết hạn sau 60 s (00:00:00Z = 07:00:00 giờ VN năm 2026).
const BASE = Date.parse('2026-11-20T00:00:00Z');

function renderAt(secondsSinceReceived: number, refreshing = false) {
  return render(
    <QrCountdown
      expiresAtMs={BASE + 60_000}
      receivedAtMs={BASE}
      refreshAfterSec={30}
      nowMs={BASE + secondsSinceReceived * 1000}
      refreshing={refreshing}
    />,
  );
}

describe('QrCountdown – hai đồng hồ', () => {
  it('vừa nhận mã: mới sau 30 s, hết hạn sau 60 s', () => {
    renderAt(0);
    expect(screen.getByText('Mã mới sau 30 giây')).toBeTruthy();
    expect(screen.getByText('Hết hạn sau 60 giây (lúc 07:01:00)')).toBeTruthy();
  });

  it('giữa chu kỳ: hai đồng hồ giảm cùng tốc độ nhưng khác điểm đích', () => {
    renderAt(20);
    expect(screen.getByText('Mã mới sau 10 giây')).toBeTruthy();
    expect(screen.getByText('Hết hạn sau 40 giây (lúc 07:01:00)')).toBeTruthy();
  });

  it('quá hạn đổi mã mà mã cũ còn hiệu lực: "Đang lấy mã mới…" và vẫn đếm hết hạn', () => {
    renderAt(31, true);
    expect(screen.getByText('Đang lấy mã mới…')).toBeTruthy();
    expect(screen.getByText('Hết hạn sau 29 giây (lúc 07:01:00)')).toBeTruthy();
    expect(screen.queryByText(/Mã mới sau/)).toBeNull();
  });

  it('thanh tiến trình theo chu kỳ đổi mã (không theo hạn 60 s)', () => {
    renderAt(15);
    expect(screen.getByRole('progressbar', { name: 'Thời gian tới lần đổi mã' }).getAttribute('aria-valuenow')).toBe('50');
  });

  it('không đếm âm khi đã quá hạn', () => {
    renderAt(90);
    expect(screen.getByText('Hết hạn sau 0 giây (lúc 07:01:00)')).toBeTruthy();
  });
});
