// A-01 Khung trang quản trị – docs/web/screens/A-01-admin-shell.md (tab theo hash, người dùng, Đăng xuất).
import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '@/features/auth/authContext';
import { Button } from '@/features/common/Button';
import styles from './Admin.module.css';

const TABS = [
  { to: '/employees', label: 'Nhân viên' },
  { to: '/stations', label: 'Trạm' },
];

export function AdminShell() {
  const { state, logout } = useAuth();
  const user = state.status === 'authenticated' ? state.user : undefined;

  return (
    <div className={styles.page}>
      <header className={styles.appBar}>
        <h1 className={styles.appTitle}>TMC Admin – Quản trị chấm công</h1>
        <div className={styles.userBox}>
          {user && (
            <span>
              {user.username} ({user.role})
            </span>
          )}
          <Button variant="ghost" small className={styles.logout} onClick={logout}>
            Đăng xuất
          </Button>
        </div>
      </header>
      <nav className={styles.tabs} aria-label="Chức năng quản trị">
        {TABS.map((tab) => (
          <NavLink
            key={tab.to}
            to={tab.to}
            className={({ isActive }) => `${styles.tab} ${isActive ? styles.tabActive : ''}`}
          >
            {tab.label}
          </NavLink>
        ))}
      </nav>
      <main className={styles.content}>
        <Outlet />
      </main>
    </div>
  );
}
