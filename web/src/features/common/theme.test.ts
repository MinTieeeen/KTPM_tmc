import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { act, renderHook } from '@testing-library/react';
import { THEME_KEY, applyTheme, readPreference, resolveTheme, savePreference } from './theme';
import { useTheme } from './useTheme';

function mockSystemDark(dark: boolean) {
  const listeners = new Set<(e: MediaQueryListEvent) => void>();
  window.matchMedia = ((query: string) => ({
    matches: dark,
    media: query,
    addEventListener: (_: string, cb: (e: MediaQueryListEvent) => void) => listeners.add(cb),
    removeEventListener: (_: string, cb: (e: MediaQueryListEvent) => void) => listeners.delete(cb),
  })) as unknown as typeof window.matchMedia;
  return (next: boolean) => listeners.forEach((cb) => cb({ matches: next } as MediaQueryListEvent));
}

beforeEach(() => {
  localStorage.clear();
  delete document.documentElement.dataset.theme;
});
afterEach(() => {
  // @ts-expect-error khôi phục jsdom (không có matchMedia)
  delete window.matchMedia;
});

describe('theme', () => {
  it('mặc định "system"; chỉ nhận light/dark đã lưu', () => {
    expect(readPreference()).toBe('system');
    localStorage.setItem(THEME_KEY, 'rác');
    expect(readPreference()).toBe('system');
    savePreference('dark');
    expect(readPreference()).toBe('dark');
    savePreference('system');
    expect(localStorage.getItem(THEME_KEY)).toBeNull();
  });

  it('resolveTheme: system theo hệ điều hành, còn lại giữ nguyên', () => {
    expect(resolveTheme('system', true)).toBe('dark');
    expect(resolveTheme('system', false)).toBe('light');
    expect(resolveTheme('light', true)).toBe('light');
    expect(resolveTheme('dark', false)).toBe('dark');
  });

  it('applyTheme đặt data-theme lên <html>', () => {
    applyTheme('dark');
    expect(document.documentElement.dataset.theme).toBe('dark');
  });
});

describe('useTheme', () => {
  it('system + hệ điều hành tối → data-theme=dark; đổi lựa chọn được lưu', () => {
    mockSystemDark(true);
    const { result } = renderHook(() => useTheme());
    expect(result.current.preference).toBe('system');
    expect(document.documentElement.dataset.theme).toBe('dark');
    act(() => result.current.setPreference('light'));
    expect(document.documentElement.dataset.theme).toBe('light');
    expect(localStorage.getItem(THEME_KEY)).toBe('light');
  });

  it('chế độ system tự đổi khi hệ điều hành đổi; chế độ cố định thì không', () => {
    const emit = mockSystemDark(false);
    const { result } = renderHook(() => useTheme());
    expect(document.documentElement.dataset.theme).toBe('light');
    act(() => emit(true));
    expect(document.documentElement.dataset.theme).toBe('dark');
    act(() => result.current.setPreference('light'));
    act(() => emit(true));
    expect(document.documentElement.dataset.theme).toBe('light');
  });

  it('không có matchMedia (trình duyệt cũ) vẫn chạy, mặc định sáng', () => {
    const { result } = renderHook(() => useTheme());
    expect(result.current.resolved).toBe('light');
  });
});
