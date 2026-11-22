// P-03 Mã gần đây: chỉ giữ thời điểm cấp/hết hạn của từng mã (KHÔNG giữ nội dung mã) và suy ra trạng thái.
export interface RecentCode {
  /** Số thứ tự trong phiên mở trang này (Mã #1, #2…). */
  seq: number;
  issuedAtMs: number;
  /** Hạn theo đồng hồ thiết bị (đã hiệu chỉnh độ lệch với máy chủ). */
  expiresAtMs: number;
}

export type RecentCodeStatus = 'displayed' | 'valid' | 'expired';

export const MAX_RECENT_CODES = 10;

/** `codes` sắp mới nhất trước: mã đầu tiên còn hạn là mã đang hiển thị trên P-01. */
export function codeStatus(code: RecentCode, index: number, nowMs: number): RecentCodeStatus {
  if (nowMs >= code.expiresAtMs) return 'expired';
  return index === 0 ? 'displayed' : 'valid';
}

export function countValid(codes: RecentCode[], nowMs: number): number {
  return codes.filter((c) => nowMs < c.expiresAtMs).length;
}
