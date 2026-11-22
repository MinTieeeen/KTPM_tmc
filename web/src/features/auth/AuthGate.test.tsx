import { cleanup, fireEvent, screen, waitFor } from '@testing-library/react';
import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it } from 'vitest';
import { authSession } from '@/shared/api/authSession';
import { createTestBackend } from '@/mocks/server';
import { loginAs, renderApp, resetSession } from '@/features/common/testUtils';
import { AuthGate } from './AuthGate';
import type { Page } from './pages';

const mock = createTestBackend();
beforeAll(() => mock.start());
afterAll(() => mock.stop());
beforeEach(() => {
  resetSession();
  mock.backend.calls = {};
  mock.backend.delayMs = 0;
});
afterEach(cleanup);

const SECRET = 'Nội dung được bảo vệ';

function renderGate(page: Page) {
  return renderApp(
    <AuthGate page={page}>
      <p>{SECRET}</p>
    </AuthGate>,
  );
}

function fillAndSubmit(username: string, password: string) {
  fireEvent.change(screen.getByLabelText('Tên đăng nhập'), { target: { value: username } });
  fireEvent.change(screen.getByLabelText('Mật khẩu'), { target: { value: password } });
  fireEvent.click(screen.getByRole('button', { name: 'Đăng nhập' }));
}

const WRONG = 'Sai thông tin đăng nhập hoặc tài khoản bị khóa';

describe('AU-01 Đăng nhập', () => {
  it('không có token → hiện form, nút khóa khi còn ô trống', () => {
    renderGate('portal');
    expect(screen.getByRole('heading', { name: 'TMC Portal' })).toBeTruthy();
    const submit = screen.getByRole('button', { name: 'Đăng nhập' }) as HTMLButtonElement;
    expect(submit.disabled).toBe(true);
    fireEvent.change(screen.getByLabelText('Tên đăng nhập'), { target: { value: 'e0001' } });
    expect(submit.disabled).toBe(true);
  });

  it('tiêu đề Admin ở trang admin', () => {
    renderGate('admin');
    expect(screen.getByRole('heading', { name: 'TMC Admin' })).toBeTruthy();
  });

  it('e0001 + mật khẩu demo trên Portal → vào nội dung', async () => {
    renderGate('portal');
    fillAndSubmit('e0001', 'Demo@2026');
    await screen.findByText(SECRET);
    expect(authSession.getToken()).toBe('mock.e0001');
  });

  it('admin trên Admin → vào nội dung', async () => {
    renderGate('admin');
    fillAndSubmit('admin', 'Demo@2026');
    await screen.findByText(SECRET);
  });

  it('sai mật khẩu và tài khoản e0019 → cùng một câu, xóa mật khẩu', async () => {
    renderGate('portal');
    fillAndSubmit('e0001', 'sai-mat-khau');
    expect((await screen.findByRole('alert')).textContent).toBe(WRONG);
    expect((screen.getByLabelText('Mật khẩu') as HTMLInputElement).value).toBe('');

    fillAndSubmit('e0019', 'Demo@2026');
    await waitFor(() => expect(mock.backend.calls['POST /auth/login']).toBe(2));
    expect((await screen.findByRole('alert')).textContent).toBe(WRONG);
    expect(screen.queryByText(SECRET)).toBeNull();
  });

  it('bấm đăng nhập liên tục khi đang gửi → chỉ một request', async () => {
    mock.backend.delayMs = 50;
    renderGate('portal');
    fillAndSubmit('e0001', 'Demo@2026');
    fireEvent.click(screen.getByRole('button', { name: /Đang đăng nhập/ }));
    fireEvent.submit(screen.getByLabelText('Tên đăng nhập').closest('form')!);
    await screen.findByText(SECRET);
    expect(mock.backend.calls['POST /auth/login']).toBe(1);
  });

  it('nút 👁 đổi kiểu ô mật khẩu', () => {
    renderGate('portal');
    const password = screen.getByLabelText('Mật khẩu') as HTMLInputElement;
    expect(password.type).toBe('password');
    fireEvent.click(screen.getByRole('button', { name: 'Hiện mật khẩu' }));
    expect(password.type).toBe('text');
  });
});

describe('AU-02 Không có quyền truy cập', () => {
  it('e0001 mở trang admin → AU-02 đúng tài khoản, nội dung không được mount', async () => {
    loginAs('e0001');
    renderGate('admin');
    await screen.findByRole('heading', { name: 'Không có quyền truy cập' });
    expect(screen.getByText(/Tài khoản e0001 \(EMPLOYEE\) không được dùng trang Quản trị/)).toBeTruthy();
    expect(screen.getByText('Đăng nhập bằng tài khoản có vai trò ADMIN')).toBeTruthy();
    expect(screen.queryByText(SECRET)).toBeNull();
    expect(mock.backend.calls['GET /employees']).toBeUndefined();
    expect(mock.backend.calls['GET /stations']).toBeUndefined();
    expect(mock.backend.calls['GET /portal/qr-token']).toBeUndefined();
  });

  it('admin mở Portal → AU-02; Đăng xuất → về form đăng nhập và xóa token', async () => {
    loginAs('admin');
    renderGate('portal');
    await screen.findByRole('heading', { name: 'Không có quyền truy cập' });
    expect(screen.getByText('Đăng nhập bằng tài khoản có vai trò EMPLOYEE')).toBeTruthy();
    fireEvent.click(screen.getByRole('button', { name: 'Đăng xuất' }));
    await screen.findByRole('heading', { name: 'TMC Portal' });
    expect(authSession.getToken()).toBeNull();
  });
});

describe('Guard phiên', () => {
  it('token hợp lệ → vào thẳng nội dung sau GET /auth/me', async () => {
    loginAs('e0001');
    renderGate('portal');
    await screen.findByText(SECRET);
    expect(mock.backend.calls['GET /auth/me']).toBe(1);
  });

  it('GET /auth/me trả 401 → về AU-01 và xóa token', async () => {
    authSession.setToken('token-het-han');
    renderGate('portal');
    await screen.findByRole('heading', { name: 'TMC Portal' });
    expect(authSession.getToken()).toBeNull();
  });
});
