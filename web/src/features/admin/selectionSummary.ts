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

export interface SummaryPart {
  key: 'active' | 'revoked' | 'noCredential' | 'inactive';
  label: string;
  color: 'green' | 'red' | 'gray';
}

/** Các nhóm có người (> 0), theo thứ tự cố định; màu khớp với nhãn trong bảng và chấm trên chip. */
export function summaryParts(s: SelectionSummary): SummaryPart[] {
  const parts: Array<[number, SummaryPart]> = [
    [s.active, { key: 'active', label: 'mã ACTIVE', color: 'green' }],
    [s.revoked, { key: 'revoked', label: 'mã REVOKED', color: 'red' }],
    [s.noCredential, { key: 'noCredential', label: 'chưa cấp mã', color: 'gray' }],
    [s.inactive, { key: 'inactive', label: 'đã nghỉ', color: 'gray' }],
  ];
  return parts.filter(([n]) => n > 0).map(([n, part]) => ({ ...part, label: `${n} ${part.label}` }));
}
