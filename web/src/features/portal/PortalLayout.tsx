// Khung chung của P-01/P-02: thanh tiêu đề xanh "Mã QR chấm công", đổi giao diện và Đăng xuất.
import { LogOut, QrCode } from 'lucide-react';
import type { ReactNode } from 'react';
import { useAuth } from '@/features/auth/authContext';
import { Button } from '@/features/common/Button';
import { ThemeToggle } from '@/features/common/ThemeToggle';
import styles from './Portal.module.css';

export function PortalLayout({ children }: { children: ReactNode }) {
  const { logout } = useAuth();
  return (
    <div className={styles.page}>
      <header className={styles.appBar}>
        <div className={styles.brand}>
          <QrCode size={20} aria-hidden="true" />
          <h1 className={styles.appTitle}>Mã QR chấm công</h1>
        </div>
        <div className={styles.barActions}>
          <ThemeToggle />
          <Button variant="ghost" small icon={LogOut} aria-label="Đăng xuất" onClick={logout}>
            <span className={styles.logoutText}>Đăng xuất</span>
          </Button>
        </div>
      </header>
      <main className={styles.content}>{children}</main>
    </div>
  );
}
