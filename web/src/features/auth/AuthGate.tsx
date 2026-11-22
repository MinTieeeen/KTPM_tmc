// Guard của trang (sitemap § 5.1): đang tải / AU-01 / lỗi kết nối / AU-02 / nội dung đúng vai trò.
import { LogOut, RefreshCw } from 'lucide-react';
import type { ReactNode } from 'react';
import { Alert } from '@/features/common/Alert';
import { Button } from '@/features/common/Button';
import { Spinner } from '@/features/common/Spinner';
import { loginErrorText } from '@/features/common/errorTexts';
import { ThemeToggle } from '@/features/common/ThemeToggle';
import styles from './Auth.module.css';
import { useAuth } from './authContext';
import { LoginForm } from './LoginForm';
import { RequireRole } from './RequireRole';
import type { Page } from './pages';

export function AuthGate({ page, children }: { page: Page; children: ReactNode }) {
  const { state, retry, logout } = useAuth();

  switch (state.status) {
    case 'loading':
      return (
        <main className={styles.screen}>
          <div className={styles.loading} role="status">
            <Spinner />
            <span>Đang tải…</span>
          </div>
        </main>
      );
    case 'anonymous':
      return <LoginForm page={page} />;
    case 'error':
      return (
        <main className={styles.screen}>
          <ThemeToggle className={styles.themeCorner} />
          <div className={styles.card}>
            <Alert kind="error">{loginErrorText(state.error)}</Alert>
            <p className={styles.hint}>Phiên đăng nhập vẫn được giữ.</p>
            <div className={styles.actions}>
              <Button icon={RefreshCw} onClick={retry}>
                Thử lại
              </Button>
              <Button variant="secondary" icon={LogOut} onClick={logout}>
                Đăng xuất
              </Button>
            </div>
          </div>
        </main>
      );
    case 'authenticated':
      return (
        <RequireRole page={page} user={state.user} forbidden={state.forbidden}>
          {children}
        </RequireRole>
      );
  }
}
