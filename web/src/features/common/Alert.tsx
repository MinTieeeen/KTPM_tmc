// Hộp thông báo có icon: error (đỏ), warning (cam), notice (vàng), info (xanh); lỗi được đọc ngay bởi trình đọc màn hình.
import { CircleAlert, Info, TriangleAlert } from 'lucide-react';
import type { LucideIcon } from 'lucide-react';
import type { ReactNode } from 'react';
import styles from './Alert.module.css';

type Kind = 'error' | 'warning' | 'notice' | 'info';

const ICONS: Record<Kind, LucideIcon> = {
  error: CircleAlert,
  warning: TriangleAlert,
  notice: TriangleAlert,
  info: Info,
};

interface Props {
  kind: Kind;
  children: ReactNode;
  className?: string;
}

export function Alert({ kind, children, className }: Props) {
  const Icon = ICONS[kind];
  const role = kind === 'error' ? 'alert' : 'status';
  return (
    <div role={role} className={[styles.alert, styles[kind], className].filter(Boolean).join(' ')}>
      <Icon className={styles.icon} size={18} aria-hidden="true" />
      <div>{children}</div>
    </div>
  );
}
