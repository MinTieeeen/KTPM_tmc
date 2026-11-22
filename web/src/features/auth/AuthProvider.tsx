// Phiên đăng nhập của Portal/Admin: tải /auth/me khi có token, nghe 401 (→ AU-01) và 403 (→ AU-02).
import { useCallback, useEffect, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { asApiError, networkError } from '@/shared/api/apiError';
import { authSession } from '@/shared/api/authSession';
import { centralApi } from '@/shared/api/centralApi';
import { AuthContext } from './authContext';
import type { AuthApi, AuthState } from './authContext';

function initialState(): AuthState {
  return authSession.getToken() ? { status: 'loading' } : { status: 'anonymous' };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient();
  const [state, setState] = useState<AuthState>(initialState);
  const [attempt, setAttempt] = useState(0);

  // Có token khi mở trang (hoặc bấm Thử lại): xác minh bằng GET /auth/me.
  useEffect(() => {
    if (!authSession.getToken()) return;
    let cancelled = false;
    centralApi
      .me()
      .then((user) => {
        if (!cancelled) setState({ status: 'authenticated', user, forbidden: false });
      })
      .catch((err: unknown) => {
        if (cancelled) return;
        const apiError = asApiError(err);
        if (apiError?.status === 401) return; // authSession đã xóa token và báo anonymous
        setState({ status: 'error', error: apiError ?? networkError() });
      });
    return () => {
      cancelled = true;
    };
  }, [attempt]);

  useEffect(() => {
    const offSession = authSession.subscribe(() => {
      if (!authSession.getToken()) {
        queryClient.clear();
        setState({ status: 'anonymous' });
      }
    });
    const offForbidden = authSession.subscribeForbidden(() => {
      setState((prev) => (prev.status === 'authenticated' ? { ...prev, forbidden: true } : prev));
    });
    return () => {
      offSession();
      offForbidden();
    };
  }, [queryClient]);

  const login = useCallback(async (username: string, password: string) => {
    const result = await centralApi.login(username, password);
    authSession.setToken(result.accessToken);
    try {
      const user = await centralApi.me();
      setState({ status: 'authenticated', user, forbidden: false });
    } catch (err) {
      authSession.clear();
      throw err;
    }
  }, []);

  const logout = useCallback(() => authSession.clear(), []);

  const retry = useCallback(() => {
    setState({ status: 'loading' });
    setAttempt((n) => n + 1);
  }, []);

  const api = useMemo<AuthApi>(() => ({ state, login, logout, retry }), [state, login, logout, retry]);
  return <AuthContext.Provider value={api}>{children}</AuthContext.Provider>;
}
