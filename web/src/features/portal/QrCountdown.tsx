// Đếm ngược tới expiresAt: thanh co dần (cam khi ≤ 10 s) và chữ "Hết hạn sau {n} giây".
import styles from './Portal.module.css';

const LOW_SECONDS = 10;

interface Props {
  expiresAtMs: number;
  receivedAtMs: number;
  nowMs: number;
}

export function QrCountdown({ expiresAtMs, receivedAtMs, nowMs }: Props) {
  const remainingMs = Math.max(0, expiresAtMs - nowMs);
  const seconds = Math.ceil(remainingMs / 1000);
  const totalMs = Math.max(1, expiresAtMs - receivedAtMs);
  const percent = Math.min(100, (remainingMs / totalMs) * 100);
  const low = seconds <= LOW_SECONDS;

  return (
    <div className={styles.countdown}>
      <div
        className={styles.bar}
        role="progressbar"
        aria-valuemin={0}
        aria-valuemax={100}
        aria-valuenow={Math.round(percent)}
      >
        <div className={`${styles.barFill} ${low ? styles.barFillLow : ''}`} style={{ width: `${percent}%` }} />
      </div>
      <p className={styles.countdownText}>Hết hạn sau {seconds} giây</p>
    </div>
  );
}
