// Hộp thoại modal: Esc, nút X và click ngoài để đóng (bị chặn khi `locked`), focus vào hộp khi mở, trả focus khi đóng.
import { X } from 'lucide-react';
import type { LucideIcon } from 'lucide-react';
import { useEffect, useId, useRef } from 'react';
import type { ReactNode } from 'react';
import styles from './Dialog.module.css';

interface Props {
  title: string;
  onClose: () => void;
  locked?: boolean;
  icon?: LucideIcon;
  tone?: 'danger' | 'primary';
  /** Hộp rộng hơn (danh sách nhiều dòng, vd. thao tác hàng loạt). */
  wide?: boolean;
  children: ReactNode;
  footer?: ReactNode;
}

export function Dialog({ title, onClose, locked = false, icon: Icon, tone = 'primary', wide = false, children, footer }: Props) {
  const titleId = useId();
  const boxRef = useRef<HTMLDivElement>(null);
  const lockedRef = useRef(locked);
  const onCloseRef = useRef(onClose);
  lockedRef.current = locked;
  onCloseRef.current = onClose;

  useEffect(() => {
    const previous = document.activeElement as HTMLElement | null;
    boxRef.current?.focus();
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && !lockedRef.current) onCloseRef.current();
    };
    document.addEventListener('keydown', onKey);
    return () => {
      document.removeEventListener('keydown', onKey);
      previous?.focus();
    };
  }, []);

  return (
    <div
      className={styles.overlay}
      onMouseDown={(e) => {
        if (e.target === e.currentTarget && !locked) onClose();
      }}
    >
      <div
        ref={boxRef}
        className={`${styles.dialog} ${wide ? styles.wide : ''}`}
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        tabIndex={-1}
      >
        <button type="button" className={styles.close} aria-label="Đóng" disabled={locked} onClick={onClose}>
          <X size={18} aria-hidden="true" />
        </button>
        <div className={styles.header}>
          {Icon && (
            <span className={`${styles.badge} ${tone === 'danger' ? styles.toneDanger : styles.tonePrimary}`}>
              <Icon size={20} aria-hidden="true" />
            </span>
          )}
          <h2 id={titleId} className={styles.title}>
            {title}
          </h2>
        </div>
        <div className={styles.body}>{children}</div>
        {footer && <div className={styles.footer}>{footer}</div>}
      </div>
    </div>
  );
}
