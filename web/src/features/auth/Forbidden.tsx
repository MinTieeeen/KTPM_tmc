// AU-02 Không có quyền truy cập – docs/web/screens/AU-02-forbidden.md (không gọi API nghiệp vụ).
import { LogOut, ShieldAlert } from 'lucide-react';
import type { CurrentUser } from '@/shared/api/centralApi';
import { Button } from '@/features/common/Button';
import { ThemeToggle } from '@/features/common/ThemeToggle';
import styles from './Auth.module.css';
import { useAuth } from './authContext';
import { PAGES } from './pages';
import type { Page } from './pages';

export function Forbidden({ page, user }: { page: Page; user: CurrentUser }) {
  const { logout } = useAuth();
  const config = PAGES[page];
  return (
    <main className={styles.screen}>
      <ThemeToggle className={styles.themeCorner} />
      <div className={styles.cardCenter}>
        <span className={styles.iconCircle}>
          <ShieldAlert size={30} aria-hidden="true" />
        </span>
        <h1 className={styles.heading}>Không có quyền truy cập</h1>
        <p className={styles.text}>
          Tài khoản {user.username} ({user.role}) không được dùng trang {config.forbiddenName}.
        </p>
        <p className={styles.hint}>Đăng nhập bằng tài khoản có vai trò {config.role}</p>
        <Button icon={LogOut} onClick={logout}>
          Đăng xuất
        </Button>
      </div>
    </main>
  );
}
