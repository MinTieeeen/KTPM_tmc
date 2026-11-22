import { cleanup, fireEvent, screen, waitFor } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { createTestBackend } from '@/mocks/server';
import { loginAs, renderApp, resetSession } from '@/features/common/testUtils';
import { PortalApp } from './PortalApp';

const mock = createTestBackend();
const writeText = vi.fn<(text: string) => Promise<void>>();

beforeAll(() => mock.start());
afterAll(() => mock.stop());
beforeEach(() => {
  resetSession();
  mock.backend.calls = {};
  mock.backend.qrTtlSec = 60;
  mock.backend.qrRefreshAfterSec = 30;
  mock.backend.clockSkewMs = 0;
  writeText.mockReset().mockResolvedValue(undefined);
  Object.defineProperty(navigator, 'clipboard', { value: { writeText }, configurable: true });
});
afterEach(() => {
  cleanup();
  mock.server.resetHandlers();
});

const hasQr = (container: HTMLElement) => container.querySelector('[data-testid="qr-code"]') !== null;

describe('P-01 Mã QR cá nhân', () => {
  it('hiện mã nhân viên, tài khoản, QR, đếm ngược và ghi chú làm mới', async () => {
    loginAs('e0001');
    const { container } = renderApp(<PortalApp />);
    await screen.findByText('Nhân viên E0001');
    expect(screen.getByText('tài khoản e0001')).toBeTruthy();
    await waitFor(() => expect(hasQr(container)).toBe(true));
    expect(screen.getByText(/Mã mới sau \d+ giây/)).toBeTruthy();
    expect(screen.getByText('Mã tự đổi mỗi 30 giây và chỉ dùng được một lần.')).toBeTruthy();
    // Khi mọi thứ bình thường không hiện hạn 60 s: tránh mâu thuẫn với chu kỳ đổi mã 30 s.
    expect(screen.queryByText(/Hết hạn sau/)).toBeNull();
  });

  it('đồng hồ chính không vượt chu kỳ đổi mã (30 giây)', async () => {
    loginAs('e0001');
    renderApp(<PortalApp />);
    const text = (await screen.findByText(/Mã mới sau \d+ giây/)).textContent!;
    expect(Number(/(\d+)/.exec(text)![1])).toBeLessThanOrEqual(30);
  });

  it('làm mới lỗi khi mã còn dài hạn: mới hiện "Hết hạn sau" để biết mã hiện tại dùng được bao lâu', async () => {
    mock.backend.qrRefreshAfterSec = 1;
    loginAs('e0001');
    const { container } = renderApp(<PortalApp />);
    await waitFor(() => expect(hasQr(container)).toBe(true));
    expect(screen.queryByText(/Hết hạn sau/)).toBeNull();

    mock.server.use(http.get('/portal/qr-token', () => HttpResponse.error()));
    await screen.findByText('Không lấy được mã QR mới – kiểm tra kết nối', undefined, { timeout: 4000 });
    const text = (await screen.findByText(/Hết hạn sau \d+ giây/)).textContent!;
    const seconds = Number(/(\d+)/.exec(text)![1]);
    expect(seconds).toBeGreaterThan(50);
    expect(seconds).toBeLessThanOrEqual(60);
  });

  it.each([
    ['nhanh hơn thiết bị 5 giây', 5_000],
    ['chậm hơn thiết bị 20 giây', -20_000],
  ])(
    'đồng hồ máy chủ %s: hạn hiển thị vẫn đúng (6–8 giây với mã sống 8 giây) nhờ hiệu chỉnh theo header Date',
    async (_name, skew) => {
      mock.backend.qrTtlSec = 8; // ≤ 10 s nên dòng "Hết hạn sau" hiện ngay
      mock.backend.clockSkewMs = skew;
      loginAs('e0001');
      renderApp(<PortalApp />);
      const text = (await screen.findByText(/Hết hạn sau \d+ giây/)).textContent!;
      const seconds = Number(/(\d+)/.exec(text)![1]);
      expect(seconds).toBeGreaterThanOrEqual(6);
      expect(seconds).toBeLessThanOrEqual(8);
    },
  );

  it('mã đổi sau mỗi chu kỳ làm mới', async () => {
    mock.backend.qrTtlSec = 10;
    mock.backend.qrRefreshAfterSec = 1;
    loginAs('e0001');
    renderApp(<PortalApp />);
    const copyButton = await screen.findByRole('button', { name: /Sao chép chuỗi/ });
    fireEvent.click(copyButton);
    await waitFor(() => expect(writeText).toHaveBeenCalledTimes(1));
    const first = writeText.mock.calls[0]![0];

    await waitFor(() => expect(mock.backend.calls['GET /portal/qr-token']).toBeGreaterThanOrEqual(2), {
      timeout: 4000,
    });
    fireEvent.click(screen.getByRole('button', { name: /Sao chép chuỗi/ }));
    await waitFor(() => expect(writeText).toHaveBeenCalledTimes(2));
    expect(writeText.mock.calls[1]![0]).not.toBe(first);
    expect(first.startsWith('TMC1.')).toBe(true);
  });

  it('Sao chép chuỗi gọi clipboard với raw và hiện thông báo "Đã sao chép"', async () => {
    loginAs('e0001');
    renderApp(<PortalApp />);
    fireEvent.click(await screen.findByRole('button', { name: /Sao chép chuỗi/ }));
    await screen.findByText('Đã sao chép');
    expect(writeText.mock.calls[0]![0]).toMatch(/^TMC1\.mock-E0001-\d+\.signature$/);
  });

  it('mất mạng khi làm mới: cảnh báo, giữ QR đến khi hết hạn rồi ẩn QR', async () => {
    mock.backend.qrTtlSec = 2;
    mock.backend.qrRefreshAfterSec = 1;
    loginAs('e0001');
    const { container } = renderApp(<PortalApp />);
    await waitFor(() => expect(hasQr(container)).toBe(true));

    mock.server.use(http.get('/portal/qr-token', () => HttpResponse.error()));
    await screen.findByText('Không lấy được mã QR mới – kiểm tra kết nối', undefined, { timeout: 4000 });
    expect(hasQr(container)).toBe(true);

    await screen.findByText('Không lấy được mã QR, kiểm tra kết nối', undefined, { timeout: 4000 });
    expect(hasQr(container)).toBe(false);
  });
});

describe('P-02 Không có mã QR', () => {
  it('e0020 (đã thu hồi) → câu thu hồi, không có QR nào', async () => {
    loginAs('e0020');
    const { container } = renderApp(<PortalApp />);
    await screen.findByText('Mã QR đã bị thu hồi');
    expect(screen.getByText('Liên hệ phòng Nhân sự (HR) để được cấp lại mã.')).toBeTruthy();
    expect(screen.getByText(/Tự kiểm tra lại sau 30 giây · lần cuối \d{2}:\d{2}:\d{2}/)).toBeTruthy();
    expect(hasQr(container)).toBe(false);
  });

  it('nhân viên chưa có credential → "Bạn chưa được cấp mã QR"', async () => {
    const e = mock.backend.employees.find((x) => x.code === 'E0003')!;
    mock.backend.employees = mock.backend.employees.map((x) =>
      x === e ? { ...x, credentialStatus: undefined, credentialId: undefined } : x,
    );
    loginAs('e0003');
    renderApp(<PortalApp />);
    await screen.findByText('Bạn chưa được cấp mã QR');
  });

  it('đang xem QR mà bị thu hồi → chuyển P-02 và không còn QR; cấp lại → tự về P-01', async () => {
    mock.backend.employees = mock.backend.employees.map((x) =>
      x.code === 'E0020' ? { ...x, credentialStatus: 'ACTIVE' } : x,
    );
    loginAs('e0020');
    const { container, queryClient } = renderApp(<PortalApp />);
    await waitFor(() => expect(hasQr(container)).toBe(true));

    mock.backend.employees = mock.backend.employees.map((x) =>
      x.code === 'E0020' ? { ...x, credentialStatus: 'REVOKED' } : x,
    );
    await queryClient.refetchQueries({ queryKey: ['qr-token'] });
    await screen.findByText('Mã QR đã bị thu hồi');
    expect(hasQr(container)).toBe(false);

    mock.backend.employees = mock.backend.employees.map((x) =>
      x.code === 'E0020' ? { ...x, credentialStatus: 'ACTIVE' } : x,
    );
    await queryClient.refetchQueries({ queryKey: ['qr-token'] });
    await waitFor(() => expect(hasQr(container)).toBe(true));
    expect(screen.queryByText('Mã QR đã bị thu hồi')).toBeNull();
  });
});
