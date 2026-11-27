import { useEffect, useId, useRef, useState } from 'react';
import { Camera, AlertCircle } from 'lucide-react';
import styles from './Kiosk.module.css';

interface Props {
  onScan: (raw: string) => void;
}

export function CameraScanner({ onScan }: Props) {
  const id = useId();
  const callback = useRef(onScan);
  const [active, setActive] = useState(false);
  const [error, setError] = useState(false);

  useEffect(() => {
    callback.current = onScan;
  }, [onScan]);

  useEffect(() => {
    let cancelled = false;
    let stopScanner: (() => Promise<void>) | undefined;
    let previous = '';
    let lastSeen = 0;

    async function start() {
      try {
        const { Html5Qrcode } = await import('html5-qrcode');
        if (cancelled) return;
        const scanner = new Html5Qrcode(id);
        const onDecoded = (raw: string) => {
          if (cancelled) return;
          const now = Date.now();
          const repeated = raw === previous && now - lastSeen < 3000;
          previous = raw;
          lastSeen = now;
          if (!repeated) {
            callback.current(raw);
          }
        };
        try {
          await scanner.start({ facingMode: 'environment' }, { fps: 10, qrbox: 220 }, onDecoded, () => {});
        } catch {
          await scanner.start({ facingMode: 'user' }, { fps: 10, qrbox: 220 }, onDecoded, () => {});
        }

        stopScanner = async () => {
          await scanner.stop();
          if (document.getElementById(id)) scanner.clear();
        };

        if (cancelled) {
          await stopScanner();
        } else {
          setActive(true);
          setError(false);
        }
      } catch {
        if (!cancelled) {
          setActive(false);
          setError(true);
        }
      }
    }

    void start();

    return () => {
      cancelled = true;
      if (stopScanner) void stopScanner().catch(() => {});
    };
  }, [id]);

  return (
    <div className={styles.cameraBox}>
      {active && (
        <h2 className={styles.actionTitle} style={{ fontSize: 24 }}>
          Đưa mã QR vào khung camera
        </h2>
      )}

      <div
        className={styles.cameraViewport}
        style={active ? undefined : { position: 'absolute', width: 1, height: 1, overflow: 'hidden', opacity: 0, pointerEvents: 'none' }}
      >
        <div id={id} style={{ width: '100%', height: '100%' }} />
      </div>

      {active && (
        <div className={styles.readyBadge}>
          <Camera size={16} style={{ color: '#16a34a' }} />
          <span>Camera đang quét · Giữ mã QR trước ống kính</span>
        </div>
      )}

      {error && (
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 12 }}>
          <div style={{ color: 'var(--danger)', display: 'flex', alignItems: 'center', gap: 8, fontWeight: 700 }}>
            <AlertCircle size={20} />
            <span>Camera không khả dụng. Hãy dùng máy quét USB.</span>
          </div>
          <p className={styles.actionSubtitle}>
            Vui lòng cắm hoặc hướng máy quét mã vạch USB vào mã QR trên điện thoại.
          </p>
        </div>
      )}

      {!active && !error && (
        <div className={styles.readyBadge}>
          <span className={styles.pulseDot} style={{ color: 'var(--primary)' }} />
          <span>Đang khởi động camera…</span>
        </div>
      )}
    </div>
  );
}
