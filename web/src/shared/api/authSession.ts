// Token đăng nhập trong sessionStorage (mất khi đóng tab) và thông báo khi phiên hết hạn (401).
const KEY = 'tmc.accessToken';

type Listener = () => void;
const listeners = new Set<Listener>();
const forbiddenListeners = new Set<Listener>();

function notify(): void {
  listeners.forEach((l) => l());
}

export const authSession = {
  getToken(): string | null {
    return sessionStorage.getItem(KEY);
  },

  setToken(token: string): void {
    sessionStorage.setItem(KEY, token);
  },

  clear(): void {
    sessionStorage.removeItem(KEY);
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
