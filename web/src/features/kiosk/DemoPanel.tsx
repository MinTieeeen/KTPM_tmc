import { useState } from 'react';
import { Settings, ChevronDown, ChevronUp, RefreshCw } from 'lucide-react';
import { Button } from '@/features/common/Button';
import { Badge } from '@/features/common/Badge';
import { gateRequest, type GateStatus } from '../../shared/api/gateApi';
import styles from './Kiosk.module.css';

export function DemoPanel({ status, reachable }: { status: GateStatus; reachable: boolean }) {
  const [collapsed, setCollapsed] = useState(() => localStorage.getItem('tmc-kiosk-demo-collapsed') === 'true');
  const [simulated, setSimulated] = useState<boolean>();
  const [busy, setBusy] = useState(false);
  const [hidden, setHidden] = useState(false);
  const [error, setError] = useState(false);
  const offline = simulated ?? !status.online;

  async function toggle() {
    setBusy(true);
    setError(false);
    try {
      await gateRequest('/admin/simulate-offline', { offline: !offline });
      setSimulated(!offline);
    } catch (failure) {
      if (failure === 404) setHidden(true);
      else setError(true);
    } finally {
      setBusy(false);
    }
  }

  if (hidden) return null;

  // Khi đã kết nối lại và outbox còn bản ghi chưa đồng bộ
  const isSyncingBacklog = status.online && status.outboxDepth > 0;

  return (
    <aside className={styles.demoAside}>
      <button
        type="button"
        className={styles.demoHeader}
        aria-expanded={!collapsed}
        onClick={() => {
          localStorage.setItem('tmc-kiosk-demo-collapsed', String(!collapsed));
          setCollapsed(!collapsed);
        }}
      >
        <span style={{ display: 'inline-flex', alignItems: 'center', gap: 8 }}>
          <Settings size={15} />
          <span>Chế độ demo</span>
        </span>
        {collapsed ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
      </button>

      {!collapsed && (
        <div className={styles.demoBody}>
          <div className={styles.demoRow}>
            <span style={{ color: 'var(--text-muted)' }}>Kết nối Central:</span>
            <Badge color={status.online ? 'green' : 'red'}>
              {status.online ? 'Online' : 'Đã ngắt (giả lập)'}
            </Badge>
          </div>

          <div className={styles.demoRow}>
            <span style={{ color: 'var(--text-muted)' }}>Hàng đợi Outbox:</span>
            {isSyncingBacklog ? (
              <span style={{ color: '#d97706', fontWeight: 700, display: 'inline-flex', alignItems: 'center', gap: 4 }}>
                <RefreshCw size={12} className="spin" />
                Đang đồng bộ · còn {status.outboxDepth} bản ghi
              </span>
            ) : (
              <strong>{status.outboxDepth} bản ghi</strong>
            )}
          </div>

          <div className={styles.demoRow}>
            <span style={{ color: 'var(--text-muted)' }}>Quá ngưỡng offline:</span>
            <span>{status.offlineThresholdExceeded ? 'Có' : 'Không'}</span>
          </div>

          <Button
            variant={offline ? 'primary' : 'danger'}
            small
            disabled={busy || !reachable}
            onClick={() => void toggle()}
            loading={busy}
          >
            {offline ? 'Nối lại' : 'Giả lập mất kết nối'}
          </Button>

          {error && (
            <p role="alert" style={{ color: 'var(--danger)', margin: 0, fontSize: 12 }}>
              Không thể cập nhật chế độ giả lập.
            </p>
          )}
        </div>
      )}
    </aside>
  );
}
