// AU-01 Đăng nhập – docs/web/screens/AU-01-login.md (dùng chung cho Portal và Admin).
import { Eye, EyeOff, Lock, LogIn, QrCode, User } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import type { FormEvent } from 'react';
import { asApiError, networkError } from '@/shared/api/apiError';
import { Alert } from '@/features/common/Alert';
import { Button } from '@/features/common/Button';
import { loginErrorText } from '@/features/common/errorTexts';
import { ThemeToggle } from '@/features/common/ThemeToggle';
import styles from './Auth.module.css';
import { useAuth } from './authContext';
import { PAGES } from './pages';
import type { Page } from './pages';

export function LoginForm({ page }: { page: Page }) {
  const { login } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [remember, setRemember] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [passwordInvalid, setPasswordInvalid] = useState(false);
  const inFlight = useRef(false);
  const usernameRef = useRef<HTMLInputElement>(null);
  const passwordRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    usernameRef.current?.focus();
  }, []);

  const canSubmit = username.length > 0 && password.length > 0 && !submitting;

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!canSubmit || inFlight.current) return;
    inFlight.current = true;
    setSubmitting(true);
    setError(null);
    setPasswordInvalid(false);
    try {
      await login(username, password, remember);
    } catch (err) {
      const apiError = asApiError(err) ?? networkError();
      setError(loginErrorText(apiError));
      if (apiError.code === 'INVALID_CREDENTIALS') {
        setPassword('');
        setPasswordInvalid(true);
        passwordRef.current?.focus();
      }
      setSubmitting(false);
    } finally {
      inFlight.current = false;
    }
  };

  return (
    <main className={styles.screen}>
      <ThemeToggle className={styles.themeCorner} />
      <div className={styles.card}>
        <div className={styles.brand}>
          <span className={styles.logo}>
            <QrCode size={24} aria-hidden="true" />
          </span>
          <div>
            <h1 className={styles.title}>{PAGES[page].title}</h1>
            <p className={styles.subtitle}>Đăng nhập để tiếp tục</p>
          </div>
        </div>
        <form className={styles.form} onSubmit={onSubmit}>
          <label className={styles.field}>
            Tên đăng nhập
            <span className={styles.inputWrap}>
              <User className={styles.inputIcon} size={17} aria-hidden="true" />
              <input
                ref={usernameRef}
                className={styles.input}
                type="text"
                autoComplete="username"
                maxLength={32}
                value={username}
                onChange={(e) => setUsername(e.target.value)}
              />
            </span>
          </label>
          <label className={styles.field}>
            Mật khẩu
            <span className={styles.inputWrap}>
              <Lock className={styles.inputIcon} size={17} aria-hidden="true" />
              <input
                ref={passwordRef}
                className={`${styles.input} ${passwordInvalid ? styles.inputInvalid : ''}`}
                type={showPassword ? 'text' : 'password'}
                autoComplete="current-password"
                maxLength={128}
                value={password}
                aria-invalid={passwordInvalid || undefined}
                onChange={(e) => setPassword(e.target.value)}
              />
              <button
                type="button"
                className={styles.eye}
                aria-label={showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                aria-pressed={showPassword}
                onClick={() => setShowPassword((v) => !v)}
              >
                {showPassword ? <EyeOff size={18} aria-hidden="true" /> : <Eye size={18} aria-hidden="true" />}
              </button>
            </span>
          </label>
          <div className={styles.rememberBlock}>
            <label className={styles.remember}>
              <input type="checkbox" checked={remember} onChange={(e) => setRemember(e.target.checked)} />
              Ghi nhớ đăng nhập
            </label>
            {remember && (
              <p className={styles.rememberHint}>
                Giữ đăng nhập trên thiết bị này tối đa 8 giờ, cho tới khi bạn đăng xuất. Không nên bật trên máy dùng
                chung.
              </p>
            )}
          </div>
          {error && <Alert kind="error">{error}</Alert>}
          <Button
            type="submit"
            block
            icon={LogIn}
            disabled={!canSubmit}
            loading={submitting}
            loadingText="Đang đăng nhập…"
          >
            Đăng nhập
          </Button>
          <p className={styles.note}>
            <Lock size={12} aria-hidden="true" />
            {remember ? 'Phiên được giữ khi đóng tab' : 'Phiên đăng nhập kết thúc khi đóng tab'}
          </p>
        </form>
      </div>
    </main>
  );
}
