// Nút chọn giao diện Sáng / Tối / Hệ thống; màu chữ kế thừa từ vùng chứa (thanh tiêu đề hoặc trang).
import { Monitor, Moon, Sun } from 'lucide-react';
import type { LucideIcon } from 'lucide-react';
import type { ThemePreference } from './theme';
import styles from './ThemeToggle.module.css';
import { useTheme } from './useTheme';

const OPTIONS: Array<{ value: ThemePreference; label: string; Icon: LucideIcon }> = [
  { value: 'light', label: 'Sáng', Icon: Sun },
  { value: 'dark', label: 'Tối', Icon: Moon },
  { value: 'system', label: 'Theo hệ thống', Icon: Monitor },
];

export function ThemeToggle({ className }: { className?: string }) {
  const { preference, setPreference } = useTheme();
  return (
    <div className={[styles.group, className].filter(Boolean).join(' ')} role="group" aria-label="Giao diện">
      {OPTIONS.map(({ value, label, Icon }) => (
        <button
          key={value}
          type="button"
          className={`${styles.option} ${preference === value ? styles.active : ''}`}
          aria-pressed={preference === value}
          aria-label={label}
          title={label}
          onClick={() => setPreference(value)}
        >
          <Icon size={16} aria-hidden="true" />
        </button>
      ))}
    </div>
  );
}
