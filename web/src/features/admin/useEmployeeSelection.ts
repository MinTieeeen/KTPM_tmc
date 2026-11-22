// Lựa chọn nhân viên ở A-02: theo mã (giữ khi tìm kiếm/lọc), bấm Shift để chọn cả dải đang hiển thị.
import { useCallback, useRef, useState } from 'react';

export interface EmployeeSelection {
  selected: ReadonlySet<string>;
  /** Bật/tắt một dòng; `shiftKey` chọn/bỏ chọn cả dải từ dòng bấm lần trước tới dòng này (trong danh sách đang hiển thị). */
  toggle: (code: string, shiftKey?: boolean) => void;
  /** Chọn hết các dòng đang hiển thị; nếu đã chọn hết thì bỏ chọn các dòng đó (không đụng dòng đang bị lọc ẩn). */
  toggleAllVisible: () => void;
  selectMany: (codes: string[]) => void;
  remove: (code: string) => void;
  clear: () => void;
}

export function useEmployeeSelection(visibleCodes: string[]): EmployeeSelection {
  const [selected, setSelected] = useState<ReadonlySet<string>>(new Set());
  const anchor = useRef<string | null>(null);

  const toggle = useCallback(
    (code: string, shiftKey = false) => {
      // Chụp mốc trước khi ghi đè: hàm cập nhật state chạy muộn hơn dòng `anchor.current = code` bên dưới.
      const previousAnchor = anchor.current;
      setSelected((prev) => {
        const next = new Set(prev);
        const checking = !prev.has(code);
        const from = previousAnchor === null ? -1 : visibleCodes.indexOf(previousAnchor);
        const to = visibleCodes.indexOf(code);
        const codes =
          shiftKey && from >= 0 && to >= 0
            ? visibleCodes.slice(Math.min(from, to), Math.max(from, to) + 1)
            : [code];
        codes.forEach((c) => (checking ? next.add(c) : next.delete(c)));
        return next;
      });
      anchor.current = code;
    },
    [visibleCodes],
  );

  const toggleAllVisible = useCallback(() => {
    setSelected((prev) => {
      const next = new Set(prev);
      const allSelected = visibleCodes.length > 0 && visibleCodes.every((c) => prev.has(c));
      visibleCodes.forEach((c) => (allSelected ? next.delete(c) : next.add(c)));
      return next;
    });
    anchor.current = null;
  }, [visibleCodes]);

  const selectMany = useCallback((codes: string[]) => {
    setSelected((prev) => new Set([...prev, ...codes]));
  }, []);

  const remove = useCallback((code: string) => {
    setSelected((prev) => {
      const next = new Set(prev);
      next.delete(code);
      return next;
    });
  }, []);

  const clear = useCallback(() => {
    setSelected(new Set());
    anchor.current = null;
  }, []);

  return { selected, toggle, toggleAllVisible, selectMany, remove, clear };
}
