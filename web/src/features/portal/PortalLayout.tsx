// Khung chung của P-01/P-02: thanh tiêu đề "Mã QR chấm công" và nút Đăng xuất.
import type { ReactNode } from 'react';
import { useAuth } from '@/features/auth/authContext';
import { Button } from '@/features/common/Button';
import styles from './Portal.module.css';

export function PortalLayout({ children }: { children: ReactNode }) {
  const { logout } = useAuth();
  return (
    <div className={styles.page}>
      <header className={styles.appBar}>
        <h1 className={styles.appTitle}>Mã QR chấm công</h1>
        <Button variant="ghost" small className={styles.logout} onClick={logout}>
          Đăng xuất
        </Button>
      </header>
      <main className={styles.content}>{children}</main>
    </div>
  );
}
