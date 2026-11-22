import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { authSession } from './authSession';

const REMEMBER_KEY = 'tmc.rememberedSession';
const future = () => new Date(Date.now() + 8 * 3600_000).toISOString();
const past = () => new Date(Date.now() - 1000).toISOString();

beforeEach(() => {
  sessionStorage.clear();
  localStorage.clear();
});
afterEach(() => vi.restoreAllMocks());

describe('authSession – mặc định (không ghi nhớ)', () => {
  it('token nằm trong sessionStorage, không nằm trong localStorage', () => {
    authSession.setToken('abc');
    expect(sessionStorage.getItem('tmc.accessToken')).toBe('abc');
    expect(localStorage.getItem(REMEMBER_KEY)).toBeNull();
    expect(authSession.getToken()).toBe('abc');
    expect(authSession.isRemembered()).toBe(false);
  });

  it('đóng tab (mất sessionStorage) thì mất đăng nhập', () => {
    authSession.setToken('abc');
    sessionStorage.clear();
    expect(authSession.getToken()).toBeNull();
  });
});

describe('authSession – ghi nhớ đăng nhập', () => {
  it('lưu token + hạn trong localStorage, không lưu trong sessionStorage', () => {
    const expiresAt = future();
    authSession.setToken('abc', { remember: true, expiresAt });
    expect(JSON.parse(localStorage.getItem(REMEMBER_KEY)!)).toEqual({ token: 'abc', expiresAt });
    expect(sessionStorage.getItem('tmc.accessToken')).toBeNull();
    expect(authSession.getToken()).toBe('abc');
    expect(authSession.isRemembered()).toBe(true);
  });

  it('đóng tab (mất sessionStorage) vẫn còn đăng nhập', () => {
    authSession.setToken('abc', { remember: true, expiresAt: future() });
    sessionStorage.clear();
    expect(authSession.getToken()).toBe('abc');
  });

  it('token đã hết hạn bị bỏ qua và bị xóa khỏi localStorage', () => {
    localStorage.setItem(REMEMBER_KEY, JSON.stringify({ token: 'cu', expiresAt: past() }));
    expect(authSession.getToken()).toBeNull();
    expect(localStorage.getItem(REMEMBER_KEY)).toBeNull();
  });

  it('dữ liệu hỏng bị bỏ qua và bị xóa', () => {
    localStorage.setItem(REMEMBER_KEY, '{khong phai json');
    expect(authSession.getToken()).toBeNull();
    expect(localStorage.getItem(REMEMBER_KEY)).toBeNull();
    localStorage.setItem(REMEMBER_KEY, JSON.stringify({ token: 123 }));
    expect(authSession.getToken()).toBeNull();
  });

  it('thiếu expiresAt thì không ghi nhớ (rơi về sessionStorage)', () => {
    authSession.setToken('abc', { remember: true });
    expect(localStorage.getItem(REMEMBER_KEY)).toBeNull();
    expect(sessionStorage.getItem('tmc.accessToken')).toBe('abc');
  });

  it('đăng nhập lại không ghi nhớ sẽ xóa token đã ghi nhớ trước đó', () => {
    authSession.setToken('abc', { remember: true, expiresAt: future() });
    authSession.setToken('xyz');
    expect(localStorage.getItem(REMEMBER_KEY)).toBeNull();
    expect(authSession.getToken()).toBe('xyz');
  });

  it('clear xóa cả hai nơi và báo cho người nghe', () => {
    const listener = vi.fn();
    const off = authSession.subscribe(listener);
    authSession.setToken('abc', { remember: true, expiresAt: future() });
    authSession.clear();
    expect(authSession.getToken()).toBeNull();
    expect(localStorage.getItem(REMEMBER_KEY)).toBeNull();
    expect(listener).toHaveBeenCalledTimes(1);
    off();
  });

  it('đăng xuất ở tab khác (sự kiện storage) báo cho tab này', () => {
    const listener = vi.fn();
    const off = authSession.subscribe(listener);
    window.dispatchEvent(new StorageEvent('storage', { key: REMEMBER_KEY, newValue: null }));
    expect(listener).toHaveBeenCalledTimes(1);
    window.dispatchEvent(new StorageEvent('storage', { key: 'khoa-khac', newValue: 'x' }));
    expect(listener).toHaveBeenCalledTimes(1);
    off();
  });

  it('trình duyệt chặn storage thì không ném lỗi', () => {
    vi.spyOn(Storage.prototype, 'getItem').mockImplementation(() => {
      throw new Error('blocked');
    });
    expect(authSession.getToken()).toBeNull();
    expect(authSession.isRemembered()).toBe(false);
  });
});
