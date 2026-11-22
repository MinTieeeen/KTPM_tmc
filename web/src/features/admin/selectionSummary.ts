// Thống kê các nhân viên đang chọn theo trạng thái để hiện ở khung "Đã chọn N nhân viên".
import type { EmployeeView } from '@/shared/api/centralApi';

export interface SelectionSummary {
  total: number;
  /** Mã QR đang ACTIVE. */
  active: number;
  /** Mã QR đã bị thu hồi (REVOKED). */
  revoked: number;
  /** Chưa từng được cấp mã. */
  noCredential: number;
  /** Đã nghỉ việc (không phụ thuộc trạng thái mã). */
  inactive: number;
}

export function summarizeSelection(employees: EmployeeView[]): SelectionSummary {
  return {
    total: employees.length,
    active: employees.filter((e) => e.credentialStatus === 'ACTIVE').length,
    revoked: employees.filter((e) => e.credentialStatus === 'REVOKED').length,
    noCredential: employees.filter((e) => e.credentialStatus === undefined).length,
    inactive: employees.filter((e) => !e.active).length,
  };
}

/** Các phần có giá trị > 0, theo thứ tự cố định; dùng để nối thành câu mô tả. */
export function summaryParts(s: SelectionSummary): string[] {
  const parts: Array<[number, string]> = [
    [s.active, 'mã ACTIVE'],
    [s.revoked, 'mã REVOKED'],
    [s.noCredential, 'chưa cấp mã'],
    [s.inactive, 'đã nghỉ'],
  ];
  return parts.filter(([n]) => n > 0).map(([n, label]) => `${n} ${label}`);
}
