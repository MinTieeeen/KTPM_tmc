// Khối giữ chỗ khi đang tải dữ liệu lần đầu (thay vòng quay ở bảng và danh sách).
import type { CSSProperties } from 'react';
import styles from './Skeleton.module.css';

export function Skeleton({ width = '100%', height }: { width?: CSSProperties['width']; height?: CSSProperties['height'] }) {
  return <span className={styles.skeleton} style={{ width, height }} aria-hidden="true" />;
}
