// Nhãn trạng thái có chấm màu: xanh = tốt, đỏ = lỗi/từ chối, cam = cảnh báo, xám = trung tính; `pulse` nhấp nháy chấm.
import type { ReactNode } from 'react';
import styles from './Badge.module.css';

interface Props {
  color: 'green' | 'red' | 'orange' | 'gray';
  pulse?: boolean;
  children: ReactNode;
}

export function Badge({ color, pulse, children }: Props) {
  return (
    <span className={`${styles.badge} ${styles[color]} ${pulse ? styles.pulse : ''}`}>
      <span className={styles.dot} aria-hidden="true" />
      {children}
    </span>
  );
}
