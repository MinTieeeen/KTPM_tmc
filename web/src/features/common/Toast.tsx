// Thông báo ngắn tự ẩn sau 2 giây; bọc ứng dụng bằng ToastProvider, gọi qua useToast().show(chữ, loại?).
import { CircleAlert, CircleCheck } from 'lucide-react';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import type { ReactNode } from 'react';
import styles from './Toast.module.css';
import { ToastContext } from './toastContext';
import type { ToastKind } from './toastContext';

const DURATION_MS = 2000;

interface Item {
  id: number;
  message: string;
  kind: ToastKind;
}

export function ToastProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<Item[]>([]);
  const nextId = useRef(1);
  const timers = useRef(new Set<number>());

  const show = useCallback((message: string, kind: ToastKind = 'success') => {
    const id = nextId.current++;
    setItems((prev) => [...prev, { id, message, kind }]);
    const timer = window.setTimeout(() => {
      timers.current.delete(timer);
      setItems((prev) => prev.filter((i) => i.id !== id));
    }, DURATION_MS);
    timers.current.add(timer);
  }, []);

  useEffect(() => {
    const active = timers.current;
    return () => active.forEach((t) => window.clearTimeout(t));
  }, []);

  const api = useMemo(() => ({ show }), [show]);

  return (
    <ToastContext.Provider value={api}>
      {children}
      <div className={styles.region} aria-live="polite">
        {items.map((i) => {
          const Icon = i.kind === 'success' ? CircleCheck : CircleAlert;
          return (
            <div key={i.id} className={`${styles.toast} ${styles[i.kind]}`}>
              <Icon size={16} aria-hidden="true" />
              <span>{i.message}</span>
            </div>
          );
        })}
      </div>
    </ToastContext.Provider>
  );
}
