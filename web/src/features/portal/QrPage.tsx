// P-01 Mã QR cá nhân – docs/web/screens/P-01-qr-code.md (QR level M, đếm ngược, sao chép chuỗi).
import { Copy, RefreshCw } from 'lucide-react';
import { QRCodeSVG } from 'qrcode.react';
import type { CurrentUser, QrTokenResponse } from '@/shared/api/centralApi';
import { Alert } from '@/features/common/Alert';
import { Button } from '@/features/common/Button';
import { Spinner } from '@/features/common/Spinner';
import { useToast } from '@/features/common/toastContext';
import { TEXT_QR_REFRESH_FAILED, TEXT_QR_REFRESH_WARNING } from '@/features/common/errorTexts';
import { useCopy } from '@/features/common/useCopy';
import { useNow } from '@/features/common/useNow';
import styles from './Portal.module.css';
import { PortalLayout } from './PortalLayout';
import { QrCountdown } from './QrCountdown';

interface Props {
  user: CurrentUser;
  token: QrTokenResponse | undefined;
  receivedAtMs: number;
  /** Lần làm mới gần nhất lỗi (mạng/5xx). */
  refreshFailed: boolean;
  /** Đang gọi lấy mã mới (icon làm mới quay). */
  refreshing?: boolean;
}

export function QrPage({ user, token, receivedAtMs, refreshFailed, refreshing = false }: Props) {
  // Đồng hồ nhảy mỗi giây nên có thể chậm hơn lúc nhận mã; không để đếm ngược vượt thời hạn thật.
  const nowMs = Math.max(useNow(1000), receivedAtMs);
  const copy = useCopy();
  const toast = useToast();

  const expiresAtMs = token ? Date.parse(token.expiresAt) : 0;
  const visibleToken = token && nowMs < expiresAtMs ? token : undefined;

  const onCopy = async () => {
    if (!visibleToken) return;
    if (await copy(visibleToken.raw)) toast.show('Đã sao chép');
    else toast.show('Không sao chép được', 'error');
  };

  return (
    <PortalLayout>
      <div className={styles.who}>
        <p className={styles.employee}>Nhân viên {user.employeeCode ?? ''}</p>
        <p className={styles.account}>tài khoản {user.username}</p>
      </div>

      {visibleToken ? (
        <div className={styles.card}>
          <div className={styles.qrBox} data-testid="qr-code">
            <QRCodeSVG value={visibleToken.raw} size={200} level="M" marginSize={0} title="Mã QR chấm công" />
          </div>
          <QrCountdown expiresAtMs={expiresAtMs} receivedAtMs={receivedAtMs} nowMs={nowMs} />
          <p className={styles.hint}>
            <RefreshCw size={13} className={refreshing ? styles.spin : undefined} aria-hidden="true" />
            Tự làm mới mỗi {visibleToken.refreshAfterSec} giây
          </p>
          <Button variant="secondary" icon={Copy} onClick={onCopy}>
            Sao chép chuỗi
          </Button>
          {refreshFailed && <Alert kind="warning">{TEXT_QR_REFRESH_WARNING}</Alert>}
        </div>
      ) : token || refreshFailed ? (
        <Alert kind="warning" className={styles.fullWidth}>
          {TEXT_QR_REFRESH_FAILED}
        </Alert>
      ) : (
        <Spinner />
      )}
    </PortalLayout>
  );
}
