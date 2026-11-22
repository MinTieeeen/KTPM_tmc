// A-01 Khung trang quản trị – docs/web/screens/A-01-admin-shell.md (tab theo hash, người dùng, Đăng xuất).
import { LogOut, QrCode, Radio, Users } from 'lucide-react';
import type { LucideIcon } from 'lucide-react';
import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '@/features/auth/authContext';
import { Button } from '@/features/common/Button';
import { ThemeToggle } from '@/features/common/ThemeToggle';
import styles from './Admin.module.css';

const TABS: Array<{ to: string; label: string; Icon: LucideIcon }> = [
  { to: '/employees', label: 'Nhân viên', Icon: Users },
  { to: '/stations', label: 'Trạm', Icon: Radio },
];

export function AdminShell() {
  const { state, logout } = useAuth();
  const user = state.status === 'authenticated' ? state.user : undefined;

  return (
    <div className={styles.page}>
      <header className={styles.appBar}>
        <div className={styles.brand}>
          <span className={styles.logo}>
            <QrCode size={19} aria-hidden="true" />
          </span>
          <h1 className={styles.appTitle}>TMC Admin – Quản trị chấm công</h1>
        </div>
        <div className={styles.userBox}>
          {user && (
            <div className={styles.userChip}>
              <span className={styles.avatar} aria-hidden="true">
                {user.username.charAt(0)}
              </span>
              <span>
                {user.username} ({user.role})
              </span>
            </div>
          )}
          <ThemeToggle />
          <Button variant="ghost" small icon={LogOut} aria-label="Đăng xuất" onClick={logout}>
            <span className={styles.logoutText}>Đăng xuất</span>
          </Button>
        </div>
      </header>
      <nav className={styles.tabs} aria-label="Chức năng quản trị">
        {TABS.map(({ to, label, Icon }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) => `${styles.tab} ${isActive ? styles.tabActive : ''}`}
          >
            <Icon size={17} aria-hidden="true" />
            {label}
          </NavLink>
        ))}
      </nav>
      <main className={styles.content}>
        <Outlet />
      </main>
    </div>
  );
}
