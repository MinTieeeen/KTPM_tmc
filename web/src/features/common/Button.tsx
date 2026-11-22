// Nút dùng chung của Portal/Admin; `icon` hiện bên trái, `loading` khóa nút và hiện chữ `loadingText` (nếu có).
import type { ButtonHTMLAttributes } from 'react';
import type { LucideIcon } from 'lucide-react';
import styles from './Button.module.css';
import { Spinner } from './Spinner';

type Variant = 'primary' | 'secondary' | 'danger' | 'dangerSoft' | 'ghost';

interface Props extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
  small?: boolean;
  block?: boolean;
  loading?: boolean;
  loadingText?: string;
  icon?: LucideIcon;
}

export function Button({
  variant = 'primary',
  small,
  block,
  loading,
  loadingText,
  icon: Icon,
  disabled,
  className,
  children,
  type = 'button',
  ...rest
}: Props) {
  const classes = [styles.button, styles[variant], small && styles.small, block && styles.block, className]
    .filter(Boolean)
    .join(' ');
  return (
    <button {...rest} type={type} className={classes} disabled={disabled || loading} aria-busy={loading || undefined}>
      {loading ? <Spinner /> : Icon && <Icon className={styles.icon} size={small ? 15 : 17} aria-hidden="true" />}
      {loading && loadingText ? loadingText : children}
    </button>
  );
}
