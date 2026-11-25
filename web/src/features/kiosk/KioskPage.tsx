import { useEffect, useRef, useState } from 'react';
import {
  CheckCircle2,
  XCircle,
  AlertTriangle,
  WifiOff,
  ArrowRight,
  QrCode,
} from 'lucide-react';
import { ThemeToggle } from '@/features/common/ThemeToggle';
import { CameraScanner } from './CameraScanner';
import { DemoPanel } from './DemoPanel';
import { gateRequest, rejectionText, type GateStatus, type ScanResult } from '../../shared/api/gateApi';
import styles from './Kiosk.module.css';

const directionText: Record<string, string> = { IN: 'VÀO', OUT: 'RA' };

interface OverlayConfig {
  title: string;
  action: string;
  icon: 'check' | 'x' | 'alert';
  kind: 'ACCEPTED' | 'REJECTED' | 'GATE_FAILED' | 'UNKNOWN';
}

function getOverlayConfig(result: ScanResult | 'UNKNOWN' | undefined): OverlayConfig {
  if (!result || result === 'UNKNOWN') {
    return {
      title: 'KHÔNG XÁC ĐỊNH · GẶP BẢO VỆ',
      action: 'Không xác định - vui lòng gặp bảo vệ.',
      icon: 'alert',
      kind: 'UNKNOWN',
    };
  }
  if (result.result === 'ACCEPTED') {
    return {
      title: 'MỜI QUA',
      action: 'Bước qua cổng xoay',
      icon: 'check',
      kind: 'ACCEPTED',
    };
  }
  if (result.result === 'GATE_FAILED') {
    return {
      title: 'CỔNG CHƯA MỞ · GẶP BẢO VỆ',
      action: 'Cổng chưa mở - vui lòng gặp bảo vệ.',
      icon: 'alert',
      kind: 'GATE_FAILED',
    };
  }
  // REJECTED
  const reason = result.reason;
  if (reason === 'NONCE_REUSED') {
    return {
      title: 'MÃ QR ĐÃ ĐƯỢC SỬ DỤNG',
      action: 'Vui lòng mở mã QR mới trên Portal điện thoại',
      icon: 'x',
      kind: 'REJECTED',
    };
  }
  if (reason === 'EXPIRED' || reason === 'NOT_YET_VALID') {
    return {
      title: 'MÃ QR ĐÃ HẾT HẠN',
      action: 'Vui lòng làm mới mã QR trên Portal',
      icon: 'x',
      kind: 'REJECTED',
    };
  }
  if (reason === 'ANTI_PASSBACK') {
    return {
      title: 'LỖI QUÉT TRÙNG LÀN',
      action: 'Bạn đã quét vào trước đó · Vui lòng gặp bảo vệ',
      icon: 'x',
      kind: 'REJECTED',
    };
  }
  return {
    title: 'TỪ CHỐI',
    action: rejectionText(reason),
    icon: 'x',
    kind: 'REJECTED',
  };
}

export function KioskPage() {
  const params = new URLSearchParams(location.search);
  const stationId = params.get('station') ?? '';
  const validFormat = /^[A-Z]{2}-(IN|OUT)-[0-9]{2}$/.test(stationId);
  const [status, setStatus] = useState<GateStatus>();
  const [reachable, setReachable] = useState<boolean>();
  const [busy, setBusy] = useState(false);
  const [result, setResult] = useState<ScanResult | 'UNKNOWN'>();
  const input = useRef<HTMLInputElement>(null);
  const lock = useRef(false);
  const stationKnown = !!status && Object.prototype.hasOwnProperty.call(status.turnstiles, stationId);
  const ready = validFormat && stationKnown;
  const entry = stationId.includes('-IN-');

  useEffect(() => {
    let active = true;
    async function poll() {
      try {
        const data = await gateRequest<GateStatus>('/status');
        if (active) {
          setStatus(data);
          setReachable(true);
        }
      } catch {
        if (active) setReachable(false);
      }
    }
    void poll();
    const timer = window.setInterval(() => void poll(), 3000);
    return () => {
      active = false;
      window.clearInterval(timer);
    };
  }, []);

  useEffect(() => {
    if (!result) return;
    const timer = window.setTimeout(() => {
      setResult(undefined);
      lock.current = false;
    }, 3000);
    return () => window.clearTimeout(timer);
  }, [result]);

  useEffect(() => {
    if (!ready) return;
    const focus = () => input.current?.focus({ preventScroll: true });
    let timer = 0;
    const element = input.current;
    const restore = () => {
      window.clearTimeout(timer);
      timer = window.setTimeout(() => {
        if (!document.activeElement?.closest('button, a')) focus();
      }, 100);
    };
    focus();
    element?.addEventListener('blur', restore);
    window.addEventListener('focus', restore);
    return () => {
      window.clearTimeout(timer);
      element?.removeEventListener('blur', restore);
      window.removeEventListener('focus', restore);
    };
  }, [ready, result]);

  async function submit(value: string) {
    const raw = value.trim();
    if (!ready || !raw || lock.current) return;
    lock.current = true;
    setBusy(true);
    try {
      const data = await gateRequest<ScanResult>('/scan', { stationId, raw });
      setResult(data && ['ACCEPTED', 'REJECTED', 'GATE_FAILED'].includes(data.result) ? data : 'UNKNOWN');
    } catch {
      setResult('UNKNOWN');
    } finally {
      setBusy(false);
    }
  }

  // K-03 – Màn hình cấu hình trạm (Tách rõ 3 tình huống lỗi)
  if (!ready || !status) {
    let errorTitle = 'Đang kiểm tra trạm…';
    let errorDesc = 'Vui lòng chọn trạm hoặc sửa địa chỉ theo mẫu:';
    let isGateError = false;

    if (!validFormat) {
      errorTitle = 'Thiếu hoặc sai tham số trạm';
      errorDesc = 'Địa chỉ chưa có mã trạm hoặc mã trạm không đúng cú pháp. Vui lòng chọn làn cổng đúng bên dưới hoặc sửa URL:';
    } else if (status && !stationKnown) {
      errorTitle = `Trạm ${stationId} không thuộc ${status.gateId}`;
      errorDesc = `Trạm này không nằm trong danh sách cổng của Gate Agent (${status.gateId}). Hãy chọn làn khả dụng của cổng này:`;
    } else if (reachable === false) {
      errorTitle = 'Chưa kết nối được với Gate Agent';
      errorDesc = 'Kiosk không liên lạc được với dịch vụ cổng. Hệ thống đang tự động kiểm tra lại mỗi 3 giây…';
      isGateError = true;
    }

    return (
      <main className={styles.page}>
        <header className={`${styles.appBar} ${styles.appBarConfig}`}>
          <div className={styles.brand}>
            <QrCode size={20} aria-hidden="true" />
            <h1 className={styles.appTitle}>TMC Kiosk · Cấu hình trạm</h1>
          </div>
          <div className={styles.barActions}>
            <ThemeToggle />
          </div>
        </header>

        <section className={styles.center}>
          <div className={styles.actionCard}>
            <div style={{ color: isGateError ? 'var(--warning-fg)' : 'var(--danger)', marginBottom: 8 }}>
              {isGateError ? <WifiOff size={56} /> : <AlertTriangle size={56} />}
            </div>

            <h2 className={styles.actionTitle}>{errorTitle}</h2>
            <p className={styles.actionSubtitle}>{errorDesc}</p>

            <span className={styles.codeExample}>kiosk.html?station=BD-IN-01</span>

            <nav className={styles.stationNav} aria-label="Trạm khả dụng">
              {Object.keys(status?.turnstiles ?? {}).map((id) => {
                const next = new URLSearchParams(location.search);
                next.set('station', id);
                const isEntry = id.includes('-IN-');
                return (
                  <a
                    key={id}
                    href={`?${next}`}
                    className={styles.stationButton}
                    aria-label={id}
                    role="link"
                  >
                    <span className={isEntry ? styles.stationTagIn : styles.stationTagOut}>
                      {isEntry ? 'VÀO' : 'RA'}
                    </span>
                    <span>{id}</span>
                    <ArrowRight size={16} />
                  </a>
                );
              })}
            </nav>

            {reachable === false && (
              <p className={styles.disruptionAlert} role="alert" style={{ width: '100%', borderRadius: 'var(--radius-md)', margin: 0 }}>
                Không kết nối được Gate Agent. Tự kiểm tra lại mỗi 3 giây.
              </p>
            )}
          </div>
        </section>
      </main>
    );
  }

  const outcome = result === 'UNKNOWN' ? undefined : result;
  const overlay = getOverlayConfig(result);

  // K-01 – Màn hình chỉ dẫn tại làn cổng
  return (
    <main className={styles.page}>
      {/* 1. Header: Đồng bộ kích thước và style với AppBar hệ thống */}
      <header className={`${styles.appBar} ${entry ? styles.appBarIn : styles.appBarOut}`}>
        <div className={styles.brand}>
          <QrCode size={20} aria-hidden="true" />
          <h1 className={styles.appTitle}>LÀN {entry ? 'VÀO' : 'RA'}</h1>
        </div>

        <div className={styles.barActions}>
          <ThemeToggle />
        </div>
      </header>

      {/* 2. Dải trạng thái gián đoạn (Dưới tiêu đề làn) */}
      {!status.online && (
        <div className={`${styles.disruptionBar} ${styles.disruptionOfflineOk}`}>
          <span style={{ marginRight: 8 }}>✔ <strong>Vẫn có thể quét mã</strong></span>
          <span>OFFLINE · {status.outboxDepth} bản ghi chờ đồng bộ</span>
        </div>
      )}

      {/* 3. Khu vực trung tâm: Chỉ dẫn rõ ràng người dùng cần làm gì */}
      <section className={styles.center}>
        {/* Tình huống 3: Mất liên lạc Gate Agent */}
        {reachable === false ? (
          <div className={`${styles.actionCard} ${styles.cardDanger}`}>
            <WifiOff size={68} style={{ color: 'var(--danger)' }} />
            <h2 className={styles.actionTitle} style={{ color: 'var(--danger)' }}>
              MẤT KẾT NỐI VỚI CỔNG
            </h2>
            <p className={styles.actionSubtitle} role="alert">
              Kiosk không kết nối được dịch vụ cổng. Đang tự động kiểm tra lại…
            </p>
          </div>
        ) : entry && status.offlineThresholdExceeded ? (
          /* Tình huống 2: Quá ngưỡng offline ở làn VÀO */
          <div className={`${styles.actionCard} ${styles.cardDanger}`}>
            <AlertTriangle size={68} style={{ color: 'var(--danger)' }} />
            <h2 className={styles.actionTitle} style={{ color: 'var(--danger)' }}>
              LÀN VÀO TẠM NGỪNG
            </h2>
            <p className={styles.actionSubtitle} role="alert">
              Chiều vào tạm ngừng – vui lòng gặp bảo vệ.
            </p>
          </div>
        ) : (
          /* Tình huống 1 & Bình thường: Chờ quét mã */
          <div className={styles.actionCard}>
            {params.get('camera') === '1' ? (
              <CameraScanner onScan={submit} />
            ) : (
              <>
                <div className={styles.viewfinder}>
                  <QrCode className={styles.viewfinderIcon} />
                  <div className={styles.laser} />
                </div>

                <h2 className={styles.actionTitle}>
                  {busy ? 'Đang kiểm tra…' : 'Đưa mã QR vào máy quét'}
                </h2>
                <p className={styles.actionSubtitle}>
                  Mã QR trên Portal tự đổi mỗi 30 giây.
                </p>

                <div className={styles.readyBadge}>
                  <span className={styles.pulseDot} style={{ color: 'var(--primary)', width: 8, height: 8 }} />
                  <span>Đầu đọc QR USB đã sẵn sàng</span>
                </div>
              </>
            )}

            <form
              onSubmit={(event) => {
                event.preventDefault();
                const element = input.current;
                if (!element) return;
                const raw = element.value;
                element.value = '';
                void submit(raw);
              }}
            >
              <input
                ref={input}
                className={styles.scanInput}
                aria-label="Mã QR từ máy quét USB"
                autoComplete="off"
                spellCheck={false}
                tabIndex={-1}
              />
            </form>
          </div>
        )}
      </section>

      {params.get('demo') === '1' && <DemoPanel status={status} reachable={!!reachable} />}

      {/* K-02 · Lớp phủ toàn màn hình kết quả quét: Biểu tượng + Tiêu đề + Hành động tiếp theo */}
      {result && (
        <section className={styles.overlay} data-result={overlay.kind} role="status">
          <div className={styles.overlayIcon}>
            {overlay.icon === 'check' ? (
              <CheckCircle2 size={108} strokeWidth={2.4} />
            ) : overlay.icon === 'x' ? (
              <XCircle size={108} strokeWidth={2.4} />
            ) : (
              <AlertTriangle size={108} strokeWidth={2.4} />
            )}
          </div>

          <h1 className={styles.overlayTitle}>{overlay.title}</h1>

          {outcome?.employeeCode && (
            <p className={styles.overlayWho}>
              {outcome.employeeCode} · {directionText[outcome.direction ?? (entry ? 'IN' : 'OUT')]}
            </p>
          )}

          <div className={styles.overlayActionBox}>
            {overlay.action}
          </div>

          {outcome?.elapsedMs !== undefined && (
            <p className={styles.overlayElapsed}>
              Thời gian xử lý {(outcome.elapsedMs / 1000).toLocaleString('vi-VN', {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2,
              })} s
            </p>
          )}

          <div className={styles.progressContainer}>
            <div className={styles.progress} aria-label="Tự đóng sau 3 giây" />
            <small style={{ opacity: 0.9 }}>Tự đóng sau 3 giây</small>
          </div>

          {outcome?.eventId && (
            <div className={styles.overlayEventId}>
              Mã lượt: {outcome.eventId.slice(0, 8)}
            </div>
          )}
        </section>
      )}
    </main>
  );
}
