// P-02 Không có mã QR – docs/web/screens/P-02-no-qr.md (không bao giờ hiện QR, kể cả mã cũ).
import { qrBlockedText } from '@/features/common/errorTexts';
import { formatTime } from '@/features/common/format';
import styles from './Portal.module.css';
import { PortalLayout } from './PortalLayout';

interface Props {
  code: string;
  checkedAtMs: number;
}

export function NoQrNotice({ code, checkedAtMs }: Props) {
  const text = qrBlockedText(code);
  return (
    <PortalLayout>
      <div className={styles.noQrIcon} aria-hidden="true">
        🚫
      </div>
      <h2 className={styles.noQrHeading}>{text.heading}</h2>
      <p className={styles.noQrHint}>{text.hint}</p>
      <div className={styles.infoBox} role="status">
        Tự kiểm tra lại sau 30 giây · lần cuối {formatTime(checkedAtMs)}
      </div>
    </PortalLayout>
  );
}
