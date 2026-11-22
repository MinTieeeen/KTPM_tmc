// Nút dùng chung của Portal/Admin; `loading` khóa nút và hiện chữ `loadingText` (nếu có).
import type { ButtonHTMLAttributes } from 'react';
import styles from './Button.module.css';
import { Spinner } from './Spinner';

type Variant = 'primary' | 'secondary' | 'danger' | 'ghost';

interface Props extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
  small?: boolean;
  block?: boolean;
  loading?: boolean;
  loadingText?: string;
}

export function Button({
  variant = 'primary',
  small,
  block,
  loading,
  loadingText,
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
      {loading && <Spinner />}
      {loading && loadingText ? loadingText : children}
    </button>
  );
}
