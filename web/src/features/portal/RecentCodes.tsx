// P-03 Mã gần đây – docs/web/screens/P-03-recent-codes.md (chỉ thông tin, không hiển thị lại hình hay chuỗi mã).
import { Info, ShieldCheck } from 'lucide-react';
import { Badge } from '@/features/common/Badge';
import { EmptyState } from '@/features/common/EmptyState';
import { formatTime } from '@/features/common/format';
import styles from './Portal.module.css';
import { codeStatus, countValid } from './recentCodeStatus';
import type { RecentCode, RecentCodeStatus } from './recentCodeStatus';

function StatusBadge({ status }: { status: RecentCodeStatus }) {
  if (status === 'displayed') {
    return (
      <Badge color="green" pulse>
        Đang hiển thị
      </Badge>
    );
  }
  return status === 'valid' ? <Badge color="orange">Còn hiệu lực</Badge> : <Badge color="gray">Đã hết hạn</Badge>;
}

export function RecentCodes({ codes, nowMs }: { codes: RecentCode[]; nowMs: number }) {
  if (codes.length === 0) return <EmptyState icon={ShieldCheck}>Chưa có mã nào trong phiên này</EmptyState>;

  const valid = countValid(codes, nowMs);
  return (
    <div className={styles.card}>
      <p className={styles.recentSummary}>
        <ShieldCheck size={18} aria-hidden="true" />
        <span>
          Đang có {valid} mã còn hiệu lực
        </span>
      </p>
      <ul className={styles.recentList}>
        {codes.map((code, index) => {
          const status = codeStatus(code, index, nowMs);
          const remaining = Math.ceil((code.expiresAtMs - nowMs) / 1000);
          return (
            <li key={code.seq} className={styles.recentItem}>
              <div className={styles.recentMain}>
                <span className={styles.recentTitle}>Mã #{code.seq}</span>
                <span className={styles.recentTimes}>Cấp {formatTime(code.issuedAtMs)}</span>
                <span className={styles.recentTimes}>Hết hạn {formatTime(code.expiresAtMs)}</span>
                {status !== 'expired' && <span className={styles.recentRemaining}>còn {remaining} giây</span>}
              </div>
              <StatusBadge status={status} />
            </li>
          );
        })}
      </ul>
      <p className={styles.recentNote}>
        <Info size={14} aria-hidden="true" />
        <span>
          Chỉ ghi lại các mã nhận được từ lúc mở trang này. Nội dung mã không được lưu và mã cũ không thể hiển thị lại,
          để ảnh chụp màn hình nhanh mất giá trị.
        </span>
      </p>
    </div>
  );
}
