// P-01 Mã QR cá nhân – docs/web/screens/P-01-qr-code.md (QR level M, đếm ngược, sao chép chuỗi) + tab P-03.
import { Copy } from 'lucide-react';
import { QRCodeSVG } from 'qrcode.react';
import type { CurrentUser, QrToken } from '@/shared/api/centralApi';
import { Alert } from '@/features/common/Alert';
import { Button } from '@/features/common/Button';
import { Spinner } from '@/features/common/Spinner';
import { useToast } from '@/features/common/toastContext';
import { TEXT_QR_REFRESH_FAILED, TEXT_QR_REFRESH_WARNING } from '@/features/common/errorTexts';
import { useCopy } from '@/features/common/useCopy';
import { useNow } from '@/features/common/useNow';
import styles from './Portal.module.css';
import { PortalLayout } from './PortalLayout';
import { PortalTabs } from './PortalTabs';
import { QrCountdown } from './QrCountdown';
import { RecentCodes } from './RecentCodes';
import type { RecentCode } from './recentCodeStatus';
import { usePortalTab } from './usePortalTab';

interface Props {
  user: CurrentUser;
  token: QrToken | undefined;
  receivedAtMs: number;
  /** Lần làm mới gần nhất lỗi (mạng/5xx). */
  refreshFailed: boolean;
  /** Đang gọi lấy mã mới (icon làm mới quay). */
  refreshing?: boolean;
  /** Các mã đã nhận trong phiên (mới nhất trước), cho tab "Mã gần đây". */
  recent?: RecentCode[];
}

export function QrPage({ user, token, receivedAtMs, refreshFailed, refreshing = false, recent = [] }: Props) {
  // Đồng hồ nhảy mỗi giây nên có thể chậm hơn lúc nhận mã; không để đếm ngược vượt thời hạn thật.
  const nowMs = Math.max(useNow(1000), receivedAtMs);
  const copy = useCopy();
  const toast = useToast();
  const [tab, setTab] = usePortalTab();

  // expiresAt theo đồng hồ máy chủ; quy về đồng hồ thiết bị để đếm ngược đúng dù hai đồng hồ lệch nhau.
  const expiresAtMs = token ? Date.parse(token.expiresAt) - token.clockOffsetMs : 0;
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

      <PortalTabs tab={tab} onChange={setTab} />

      <div className={styles.panel} role="tabpanel" id="portal-panel" aria-labelledby={`portal-tab-${tab}`}>
        {tab === 'recent' ? (
          <RecentCodes codes={recent} nowMs={nowMs} />
        ) : visibleToken ? (
          <div className={styles.card}>
            <div className={styles.qrBox} data-testid="qr-code">
              <QRCodeSVG value={visibleToken.raw} size={200} level="M" marginSize={0} title="Mã QR chấm công" />
            </div>
            <QrCountdown
              expiresAtMs={expiresAtMs}
              receivedAtMs={receivedAtMs}
              refreshAfterSec={visibleToken.refreshAfterSec}
              nowMs={nowMs}
              refreshing={refreshing}
            />
            <p className={styles.caption}>
              Mã tự đổi mỗi {visibleToken.refreshAfterSec} giây. Mã cũ vẫn quét được đến khi hết hạn, nhưng mỗi mã chỉ
              dùng được một lần.
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
      </div>
    </PortalLayout>
  );
}
