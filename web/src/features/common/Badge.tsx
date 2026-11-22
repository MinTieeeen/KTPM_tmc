// Nhãn trạng thái màu: xanh = tốt, đỏ = lỗi/từ chối, cam = cảnh báo, xám = trung tính.
import type { ReactNode } from 'react';
import styles from './Badge.module.css';

interface Props {
  color: 'green' | 'red' | 'orange' | 'gray';
  children: ReactNode;
}

export function Badge({ color, children }: Props) {
  return <span className={`${styles.badge} ${styles[color]}`}>{children}</span>;
}
