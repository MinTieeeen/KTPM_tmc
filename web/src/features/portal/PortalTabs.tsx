// Thanh tab của Portal: "Mã QR" (P-01) và "Mã gần đây" (P-03); panel tương ứng do QrPage hiển thị.
import { History, QrCode } from 'lucide-react';
import type { LucideIcon } from 'lucide-react';
import styles from './Portal.module.css';
import type { PortalTab } from './usePortalTab';

const TABS: Array<{ id: PortalTab; label: string; Icon: LucideIcon }> = [
  { id: 'qr', label: 'Mã QR', Icon: QrCode },
  { id: 'recent', label: 'Mã gần đây', Icon: History },
];

export function PortalTabs({ tab, onChange }: { tab: PortalTab; onChange: (tab: PortalTab) => void }) {
  return (
    <div className={styles.tabs} role="tablist" aria-label="Nội dung Portal">
      {TABS.map(({ id, label, Icon }) => (
        <button
          key={id}
          type="button"
          role="tab"
          id={`portal-tab-${id}`}
          aria-selected={tab === id}
          aria-controls="portal-panel"
          className={`${styles.tabButton} ${tab === id ? styles.tabActive : ''}`}
          onClick={() => onChange(id)}
        >
          <Icon size={16} aria-hidden="true" />
          {label}
        </button>
      ))}
    </div>
  );
}
