import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, expect, it, vi } from 'vitest';
import { KioskPage } from './KioskPage';

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
  history.replaceState(null, '', '/');
});

const status = {
  gateId: 'gate-bd', online: false, outboxDepth: 2,
  pendingReconcile: 0, offlineThresholdExceeded: false,
  turnstiles: { 'BD-IN-01': 'UP' },
};

it('scans offline through the local gate and blocks duplicate submissions', async () => {
  history.replaceState(null, '', '?station=BD-IN-01');
  const fetchMock = vi.fn((path: string) => Promise.resolve({
    ok: true,
    json: async () => path === '/status' ? status : { result: 'ACCEPTED', employeeCode: 'E0012' },
  }));
  vi.stubGlobal('fetch', fetchMock);
  render(<KioskPage />);
  const input = await screen.findByLabelText('Mã QR từ máy quét USB');
  expect(screen.getByText('OFFLINE · 2 bản ghi chờ đồng bộ')).toBeTruthy();
  for (let scan = 0; scan < 2; scan++) {
    fireEvent.change(input, { target: { value: ' demo-qr ' } });
    fireEvent.submit(input.closest('form')!);
  }
  expect(await screen.findByText('MỜI QUA')).toBeTruthy();
  const submissions = fetchMock.mock.calls.filter(([path]) => path === '/scan');
  expect(submissions).toHaveLength(1);
});

it('blocks scanning for a station registered on another gate', async () => {
  history.replaceState(null, '', '?station=LA-IN-01&camera=1');
  vi.stubGlobal('fetch', vi.fn(async () => ({ ok: true, json: async () => status })));
  render(<KioskPage />);
  expect(await screen.findByText('Trạm LA-IN-01 không thuộc gate-bd')).toBeTruthy();
  expect(screen.queryByLabelText('Mã QR từ máy quét USB')).toBeNull();
  expect(screen.getByRole('link', { name: 'BD-IN-01' }).getAttribute('href')).toBe('?station=BD-IN-01&camera=1');
});
