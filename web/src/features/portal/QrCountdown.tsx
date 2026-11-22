// Hai đồng hồ riêng: "Mã mới sau N giây" (tới lần đổi mã) và "Hết hạn sau M giây" (tới expiresAt, cam khi ≤ 10 s).
import { RefreshCw, Timer } from 'lucide-react';
import { formatTime } from '@/features/common/format';
import styles from './Portal.module.css';

const LOW_SECONDS = 10;

interface Props {
  expiresAtMs: number;
  receivedAtMs: number;
  refreshAfterSec: number;
  nowMs: number;
  /** Đang gọi lấy mã mới (icon quay, chữ "Đang lấy mã mới…"). */
  refreshing: boolean;
}

export function QrCountdown({ expiresAtMs, receivedAtMs, refreshAfterSec, nowMs, refreshing }: Props) {
  const refreshMs = Math.max(1, refreshAfterSec * 1000);
  const untilRefreshMs = Math.max(0, receivedAtMs + refreshMs - nowMs);
  const refreshSeconds = Math.ceil(untilRefreshMs / 1000);
  const percent = Math.min(100, (untilRefreshMs / refreshMs) * 100);
  const expiresSeconds = Math.ceil(Math.max(0, expiresAtMs - nowMs) / 1000);
  const expiryLow = expiresSeconds <= LOW_SECONDS;

  return (
    <div className={styles.countdown}>
      <p className={styles.countdownText}>
        <RefreshCw size={16} className={refreshing ? styles.spin : undefined} aria-hidden="true" />
        <span>{untilRefreshMs > 0 ? `Mã mới sau ${refreshSeconds} giây` : 'Đang lấy mã mới…'}</span>
      </p>
      <div
        className={styles.bar}
        role="progressbar"
        aria-label="Thời gian tới lần đổi mã"
        aria-valuemin={0}
        aria-valuemax={100}
        aria-valuenow={Math.round(percent)}
      >
        <div className={styles.barFill} style={{ width: `${percent}%` }} />
      </div>
      <p className={`${styles.expiry} ${expiryLow ? styles.expiryLow : ''}`}>
        <Timer size={14} aria-hidden="true" />
        <span>
          Hết hạn sau {expiresSeconds} giây (lúc {formatTime(expiresAtMs)})
        </span>
      </p>
    </div>
  );
}
