// Kiểu trạng thái phiên + context + hook useAuth (tách khỏi AuthProvider.tsx).
import { createContext, useContext } from 'react';
import type { ApiError } from '@/shared/api/apiError';
import type { CurrentUser } from '@/shared/api/centralApi';

export type AuthState =
  | { status: 'loading' }
  | { status: 'anonymous' }
  | { status: 'error'; error: ApiError }
  | { status: 'authenticated'; user: CurrentUser; forbidden: boolean };

export interface AuthApi {
  state: AuthState;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  /** Thử lại GET /auth/me sau lỗi mạng/5xx. */
  retry: () => void;
}

export const AuthContext = createContext<AuthApi | null>(null);

export function useAuth(): AuthApi {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth phải nằm trong AuthProvider');
  return ctx;
}
