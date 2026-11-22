// Sao chép văn bản vào clipboard; trả về true nếu thành công (không ghi nội dung ra log).
import { useCallback } from 'react';

export function useCopy(): (text: string) => Promise<boolean> {
  return useCallback(async (text: string) => {
    try {
      await navigator.clipboard.writeText(text);
      return true;
    } catch {
      return false;
    }
  }, []);
}
