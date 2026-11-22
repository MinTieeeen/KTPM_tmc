// Trạng thái rỗng: icon + câu thông báo (vd. "Không có nhân viên nào khớp bộ lọc").
import type { LucideIcon } from 'lucide-react';
import styles from './EmptyState.module.css';

export function EmptyState({ icon: Icon, children }: { icon: LucideIcon; children: string }) {
  return (
    <div className={styles.empty}>
      <span className={styles.iconWrap}>
        <Icon size={24} aria-hidden="true" />
      </span>
      <p className={styles.title}>{children}</p>
    </div>
  );
}
