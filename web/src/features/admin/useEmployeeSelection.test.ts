import { act, renderHook } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { useEmployeeSelection } from './useEmployeeSelection';

const CODES = ['E0001', 'E0002', 'E0003', 'E0004', 'E0005'];
const setup = (codes = CODES) =>
  renderHook(({ c }) => useEmployeeSelection(c), { initialProps: { c: codes } });
const list = (set: ReadonlySet<string>) => [...set].sort();

describe('useEmployeeSelection', () => {
  it('bật/tắt từng dòng', () => {
    const { result } = setup();
    act(() => result.current.toggle('E0002'));
    act(() => result.current.toggle('E0004'));
    expect(list(result.current.selected)).toEqual(['E0002', 'E0004']);
    act(() => result.current.toggle('E0002'));
    expect(list(result.current.selected)).toEqual(['E0004']);
  });

  it('Shift chọn cả dải từ dòng bấm lần trước (cả hai chiều)', () => {
    const { result } = setup();
    act(() => result.current.toggle('E0002'));
    act(() => result.current.toggle('E0004', true));
    expect(list(result.current.selected)).toEqual(['E0002', 'E0003', 'E0004']);
    act(() => result.current.toggle('E0001'));
    act(() => result.current.toggle('E0001', true)); // cùng dòng: bỏ chọn một dòng
    expect(result.current.selected.has('E0001')).toBe(false);
  });

  it('Shift ngược chiều và Shift khi bỏ chọn thì bỏ chọn cả dải', () => {
    const { result } = setup();
    act(() => result.current.toggle('E0005'));
    act(() => result.current.toggle('E0003', true));
    expect(list(result.current.selected)).toEqual(['E0003', 'E0004', 'E0005']);
    act(() => result.current.toggle('E0004')); // bỏ chọn E0004, mốc = E0004
    act(() => result.current.toggle('E0001'));
    expect(result.current.selected.has('E0004')).toBe(false);
  });

  it('Shift mà chưa có mốc thì chỉ chọn một dòng', () => {
    const { result } = setup();
    act(() => result.current.toggle('E0003', true));
    expect(list(result.current.selected)).toEqual(['E0003']);
  });

  it('chọn tất cả dòng đang hiển thị, bấm lại thì bỏ chọn các dòng đó', () => {
    const { result } = setup();
    act(() => result.current.toggleAllVisible());
    expect(result.current.selected.size).toBe(5);
    act(() => result.current.toggleAllVisible());
    expect(result.current.selected.size).toBe(0);
  });

  it('đã chọn một phần thì "chọn tất cả" chọn nốt phần còn lại', () => {
    const { result } = setup();
    act(() => result.current.toggle('E0001'));
    act(() => result.current.toggleAllVisible());
    expect(result.current.selected.size).toBe(5);
  });

  it('giữ lựa chọn của dòng đang bị lọc ẩn khi danh sách hiển thị đổi', () => {
    const { result, rerender } = setup();
    act(() => result.current.toggle('E0001'));
    act(() => result.current.toggle('E0002'));
    rerender({ c: ['E0002', 'E0003'] });
    expect(list(result.current.selected)).toEqual(['E0001', 'E0002']);
    // bỏ chọn tất cả dòng hiển thị: chỉ ảnh hưởng E0002, E0003; E0001 (đang ẩn) được giữ
    act(() => result.current.toggleAllVisible());
    act(() => result.current.toggleAllVisible());
    act(() => result.current.toggleAllVisible());
    expect(result.current.selected.has('E0001')).toBe(true);
  });

  it('selectMany, remove, clear', () => {
    const { result } = setup();
    act(() => result.current.selectMany(['E0001', 'E0003']));
    act(() => result.current.selectMany(['E0003', 'E0005']));
    expect(list(result.current.selected)).toEqual(['E0001', 'E0003', 'E0005']);
    act(() => result.current.remove('E0003'));
    expect(list(result.current.selected)).toEqual(['E0001', 'E0005']);
    act(() => result.current.clear());
    expect(result.current.selected.size).toBe(0);
  });
});
