// Token đăng nhập: mặc định trong sessionStorage (mất khi đóng tab); "ghi nhớ" thì giữ trong localStorage tới expiresAt.
const KEY = 'tmc.accessToken';
const REMEMBER_KEY = 'tmc.rememberedSession';

type Listener = () => void;
const listeners = new Set<Listener>();
const forbiddenListeners = new Set<Listener>();

function notify(): void {
  listeners.forEach((l) => l());
}

function safe<T>(action: () => T, fallback: T): T {
  try {
    return action();
  } catch {
    return fallback; // trình duyệt chặn storage
  }
}

/** Token đã ghi nhớ còn hạn; hỏng hoặc hết hạn thì xóa và trả null. */
function readRemembered(): string | null {
  const raw = safe(() => localStorage.getItem(REMEMBER_KEY), null);
  if (!raw) return null;
  try {
    const value = JSON.parse(raw) as { token?: unknown; expiresAt?: unknown };
    if (typeof value.token === 'string' && typeof value.expiresAt === 'string' && Date.parse(value.expiresAt) > Date.now()) {
      return value.token;
    }
  } catch {
    // dữ liệu hỏng: xóa bên dưới
  }
  safe(() => localStorage.removeItem(REMEMBER_KEY), undefined);
  return null;
}

export interface SetTokenOptions {
  /** Giữ đăng nhập qua việc đóng tab, tới `expiresAt` (cần có expiresAt từ phản hồi đăng nhập). */
  remember?: boolean;
  expiresAt?: string;
}

export const authSession = {
  getToken(): string | null {
    return safe(() => sessionStorage.getItem(KEY), null) ?? readRemembered();
  },

  setToken(token: string, options: SetTokenOptions = {}): void {
    if (options.remember && options.expiresAt) {
      safe(() => localStorage.setItem(REMEMBER_KEY, JSON.stringify({ token, expiresAt: options.expiresAt })), undefined);
      safe(() => sessionStorage.removeItem(KEY), undefined);
    } else {
      safe(() => sessionStorage.setItem(KEY, token), undefined);
      safe(() => localStorage.removeItem(REMEMBER_KEY), undefined);
    }
  },

  /** Phiên hiện tại đang được ghi nhớ (còn lại sau khi đóng tab). */
  isRemembered(): boolean {
    return safe(() => sessionStorage.getItem(KEY), null) === null && readRemembered() !== null;
  },

  clear(): void {
    safe(() => sessionStorage.removeItem(KEY), undefined);
    safe(() => localStorage.removeItem(REMEMBER_KEY), undefined);
    notify();
  },

  /** centralApi gọi khi nhận 401: xóa token và báo cho AuthProvider. */
  onUnauthorized(): void {
    authSession.clear();
  },

  /** centralApi gọi khi nhận 403: giữ token, báo để hiện màn "Không có quyền truy cập". */
  onForbidden(): void {
    forbiddenListeners.forEach((l) => l());
  },

  subscribe(listener: Listener): () => void {
    listeners.add(listener);
    return () => {
      listeners.delete(listener);
    };
  },

  subscribeForbidden(listener: Listener): () => void {
    forbiddenListeners.add(listener);
    return () => {
      forbiddenListeners.delete(listener);
    };
  },
};

// Đăng xuất ở tab khác (xóa token đã ghi nhớ) thì tab này cũng về màn đăng nhập.
if (typeof window !== 'undefined') {
  window.addEventListener('storage', (e) => {
    if (e.key === REMEMBER_KEY || e.key === null) notify();
  });
}
