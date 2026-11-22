import { cleanup, fireEvent, screen } from '@testing-library/react';
import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it } from 'vitest';
import { authSession } from '@/shared/api/authSession';
import { createTestBackend } from '@/mocks/server';
import { renderApp, resetSession } from '@/features/common/testUtils';
import { AuthGate } from './AuthGate';
import { useAuth } from './authContext';

const mock = createTestBackend();
beforeAll(() => mock.start());
afterAll(() => mock.stop());
beforeEach(() => {
  resetSession();
  mock.backend.calls = {};
});
afterEach(cleanup);

const SECRET = 'Nội dung được bảo vệ';

function LogoutButton() {
  const { logout } = useAuth();
  return <button onClick={logout}>thoát</button>;
}

function renderPortal() {
  return renderApp(
    <AuthGate page="portal">
      <p>{SECRET}</p>
      <LogoutButton />
    </AuthGate>,
  );
}

function fill(username = 'e0001', password = 'Demo@2026') {
  fireEvent.change(screen.getByLabelText('Tên đăng nhập'), { target: { value: username } });
  fireEvent.change(screen.getByLabelText('Mật khẩu'), { target: { value: password } });
}
const submit = () => fireEvent.click(screen.getByRole('button', { name: 'Đăng nhập' }));
const checkbox = () => screen.getByRole('checkbox', { name: 'Ghi nhớ đăng nhập' }) as HTMLInputElement;

describe('Ghi nhớ đăng nhập', () => {
  it('mặc định không tick; tick thì hiện cảnh báo máy dùng chung và đổi ghi chú phiên', () => {
    renderPortal();
    expect(checkbox().checked).toBe(false);
    expect(screen.getByText('Phiên đăng nhập kết thúc khi đóng tab')).toBeTruthy();
    expect(screen.queryByText(/Không nên bật trên máy dùng chung/)).toBeNull();

    fireEvent.click(checkbox());
    expect(checkbox().checked).toBe(true);
    expect(screen.getByText(/Giữ đăng nhập trên thiết bị này tối đa 8 giờ/)).toBeTruthy();
    expect(screen.getByText(/Không nên bật trên máy dùng chung/)).toBeTruthy();
    expect(screen.getByText('Phiên được giữ khi đóng tab')).toBeTruthy();
  });

  it('không tick: token chỉ ở sessionStorage; đóng tab rồi mở lại phải đăng nhập lại', async () => {
    renderPortal();
    fill();
    submit();
    await screen.findByText(SECRET);
    expect(sessionStorage.getItem('tmc.accessToken')).toBe('mock.e0001');
    expect(localStorage.getItem('tmc.rememberedSession')).toBeNull();

    cleanup();
    sessionStorage.clear(); // đóng tab
    renderPortal();
    await screen.findByRole('heading', { name: 'TMC Portal' });
    expect(screen.queryByText(SECRET)).toBeNull();
  });

  it('có tick: token ở localStorage kèm hạn; đóng tab rồi mở lại vào thẳng nội dung', async () => {
    renderPortal();
    fill();
    fireEvent.click(checkbox());
    submit();
    await screen.findByText(SECRET);
    expect(sessionStorage.getItem('tmc.accessToken')).toBeNull();
    const saved = JSON.parse(localStorage.getItem('tmc.rememberedSession')!);
    expect(saved.token).toBe('mock.e0001');
    expect(Date.parse(saved.expiresAt)).toBeGreaterThan(Date.now() + 7 * 3600_000);

    cleanup();
    sessionStorage.clear(); // đóng tab
    mock.backend.calls = {};
    renderPortal();
    await screen.findByText(SECRET);
    expect(mock.backend.calls['POST /auth/login']).toBeUndefined();
    expect(mock.backend.calls['GET /auth/me']).toBe(1);
  });

  it('đăng xuất xóa token đã ghi nhớ; mở lại phải đăng nhập', async () => {
    renderPortal();
    fill();
    fireEvent.click(checkbox());
    submit();
    await screen.findByText(SECRET);
    fireEvent.click(screen.getByRole('button', { name: 'thoát' }));
    await screen.findByRole('heading', { name: 'TMC Portal' });
    expect(localStorage.getItem('tmc.rememberedSession')).toBeNull();
    expect(authSession.getToken()).toBeNull();
  });

  it('token đã ghi nhớ nhưng hết hạn thì bị bỏ qua, hiện form đăng nhập', async () => {
    localStorage.setItem(
      'tmc.rememberedSession',
      JSON.stringify({ token: 'mock.e0001', expiresAt: new Date(Date.now() - 1000).toISOString() }),
    );
    renderPortal();
    await screen.findByRole('heading', { name: 'TMC Portal' });
    expect(localStorage.getItem('tmc.rememberedSession')).toBeNull();
  });

  it('token ghi nhớ bị máy chủ từ chối (401) thì bị xóa và về form đăng nhập', async () => {
    localStorage.setItem(
      'tmc.rememberedSession',
      JSON.stringify({ token: 'token-bi-thu-hoi', expiresAt: new Date(Date.now() + 3600_000).toISOString() }),
    );
    renderPortal();
    await screen.findByRole('heading', { name: 'TMC Portal' });
    expect(localStorage.getItem('tmc.rememberedSession')).toBeNull();
  });

  it('đăng nhập sai khi đã tick vẫn không lưu gì', async () => {
    renderPortal();
    fill('e0001', 'sai-mat-khau');
    fireEvent.click(checkbox());
    submit();
    await screen.findByText('Sai thông tin đăng nhập hoặc tài khoản bị khóa');
    expect(localStorage.getItem('tmc.rememberedSession')).toBeNull();
    expect(sessionStorage.getItem('tmc.accessToken')).toBeNull();
  });
});
