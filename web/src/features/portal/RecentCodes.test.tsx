import { cleanup, fireEvent, screen, waitFor } from '@testing-library/react';
import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it } from 'vitest';
import { createTestBackend } from '@/mocks/server';
import { loginAs, renderApp, resetSession } from '@/features/common/testUtils';
import { PortalApp } from './PortalApp';

const mock = createTestBackend();

beforeAll(() => mock.start());
afterAll(() => mock.stop());
beforeEach(() => {
  resetSession();
  localStorage.clear();
  window.history.replaceState(null, '', '/');
  mock.backend.calls = {};
  mock.backend.qrTtlSec = 60;
  mock.backend.qrRefreshAfterSec = 30;
  mock.backend.clockSkewMs = 0;
});
afterEach(cleanup);

const hasQr = (container: HTMLElement) => container.querySelector('[data-testid="qr-code"]') !== null;
const openRecent = () => fireEvent.click(screen.getByRole('tab', { name: 'Mã gần đây' }));

async function openPortal() {
  loginAs('e0001');
  const utils = renderApp(<PortalApp />);
  await screen.findByText('Nhân viên E0001');
  await waitFor(() => expect(hasQr(utils.container)).toBe(true));
  return utils;
}

describe('P-03 Mã gần đây', () => {
  it('có hai tab; mặc định là Mã QR', async () => {
    await openPortal();
    expect(screen.getByRole('tab', { name: 'Mã QR' }).getAttribute('aria-selected')).toBe('true');
    expect(screen.getByRole('tab', { name: 'Mã gần đây' }).getAttribute('aria-selected')).toBe('false');
  });

  it('một mã: "Mã #1 Đang hiển thị", "Đang có 1 mã còn hiệu lực", hình QR được ẩn', async () => {
    const { container } = await openPortal();
    openRecent();
    await screen.findByText('Mã #1');
    expect(screen.getByText('Đang hiển thị')).toBeTruthy();
    expect(screen.getByText('Đang có 1 mã còn hiệu lực')).toBeTruthy();
    expect(screen.getByText(/^Cấp \d{2}:\d{2}:\d{2}$/)).toBeTruthy();
    expect(screen.getByText(/^Hết hạn \d{2}:\d{2}:\d{2}$/)).toBeTruthy();
    expect(hasQr(container)).toBe(false);
    expect(window.location.hash).toBe('#/recent');
  });

  it('sau khi đổi mã: mã mới "Đang hiển thị", mã cũ "Còn hiệu lực" và tổng số mã còn hạn tăng', async () => {
    mock.backend.qrTtlSec = 10;
    mock.backend.qrRefreshAfterSec = 1;
    await openPortal();
    openRecent();
    await screen.findByText('Mã #2', undefined, { timeout: 5000 });
    expect(screen.getAllByText('Đang hiển thị')).toHaveLength(1);
    expect(screen.getAllByText('Còn hiệu lực').length).toBeGreaterThanOrEqual(1);
    const summary = screen.getByText(/Đang có \d+ mã còn hiệu lực/).textContent!;
    expect(Number(/(\d+)/.exec(summary)![1])).toBeGreaterThanOrEqual(2);
  });

  it('mã quá hạn chuyển sang "Đã hết hạn" và không còn tính vào số mã còn hiệu lực', async () => {
    mock.backend.qrTtlSec = 2;
    mock.backend.qrRefreshAfterSec = 1;
    await openPortal();
    openRecent();
    await screen.findByText('Đã hết hạn', undefined, { timeout: 6000 });
    const summary = screen.getByText(/Đang có \d+ mã còn hiệu lực/).textContent!;
    expect(Number(/(\d+)/.exec(summary)![1])).toBeLessThanOrEqual(3);
  });

  it('không lộ nội dung mã: DOM và bộ nhớ trình duyệt không chứa "TMC1."', async () => {
    await openPortal();
    openRecent();
    await screen.findByText('Mã #1');
    expect(document.body.textContent).not.toContain('TMC1.');
    expect(JSON.stringify({ ...sessionStorage })).not.toContain('TMC1.');
    expect(JSON.stringify({ ...localStorage })).not.toContain('TMC1.');
  });

  it('quay lại tab Mã QR thì hình QR hiện lại', async () => {
    const { container } = await openPortal();
    openRecent();
    await screen.findByText('Mã #1');
    fireEvent.click(screen.getByRole('tab', { name: 'Mã QR' }));
    await waitFor(() => expect(hasQr(container)).toBe(true));
    expect(window.location.hash).toBe('');
  });

  it('mở trang với #/recent thì vào thẳng tab Mã gần đây', async () => {
    window.history.replaceState(null, '', '/#/recent');
    loginAs('e0001');
    renderApp(<PortalApp />);
    await screen.findByText('Mã #1');
    expect(screen.getByRole('tab', { name: 'Mã gần đây' }).getAttribute('aria-selected')).toBe('true');
  });

  it('nhân viên bị thu hồi mã vẫn thấy P-02 (không có tab, không hiện mã nào)', async () => {
    loginAs('e0020');
    renderApp(<PortalApp />);
    await screen.findByText('Mã QR đã bị thu hồi');
    expect(screen.queryByRole('tab')).toBeNull();
  });
});
