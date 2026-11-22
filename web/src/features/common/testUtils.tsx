// Tiện ích test: render với QueryClient + AuthProvider + Toast, đăng nhập giả bằng token mock.
import { render } from '@testing-library/react';
import type { ReactElement } from 'react';
import { QueryClientProvider } from '@tanstack/react-query';
import { authSession } from '@/shared/api/authSession';
import { AuthProvider } from '@/features/auth/AuthProvider';
import { createQueryClient } from './queryClient';
import { ToastProvider } from './Toast';

export function renderApp(ui: ReactElement) {
  const queryClient = createQueryClient();
  const result = render(
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <ToastProvider>{ui}</ToastProvider>
      </AuthProvider>
    </QueryClientProvider>,
  );
  return { ...result, queryClient };
}

/** Đặt token mock như đã đăng nhập (trước khi render). */
export function loginAs(username: string): void {
  authSession.setToken(`mock.${username}`);
}

export function resetSession(): void {
  sessionStorage.clear();
  localStorage.clear();
}
