// Hộp thông báo: error (đỏ), warning (cam), notice (vàng), info (xanh); lỗi được đọc ngay bởi trình đọc màn hình.
import type { ReactNode } from 'react';
import styles from './Alert.module.css';

interface Props {
  kind: 'error' | 'warning' | 'notice' | 'info';
  children: ReactNode;
  className?: string;
}

export function Alert({ kind, children, className }: Props) {
  const role = kind === 'error' ? 'alert' : 'status';
  return (
    <div role={role} className={[styles.alert, styles[kind], className].filter(Boolean).join(' ')}>
      {children}
    </div>
  );
}
