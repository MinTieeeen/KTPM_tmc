// Thao tác hàng loạt ở A-02/A-05: ai đủ điều kiện, ai bị bỏ qua (kèm lý do) và khi nào phải gõ chữ xác nhận.
import type { EmployeeView } from '@/shared/api/centralApi';

export type BulkAction = 'revoke' | 'issue';

export interface SkippedEmployee {
  employee: EmployeeView;
  reason: string;
}

export interface BulkPlan {
  eligible: EmployeeView[];
  skipped: SkippedEmployee[];
}

/** Từ ngưỡng này trở lên, thu hồi hàng loạt phải gõ chữ xác nhận. */
export const TYPED_CONFIRM_THRESHOLD = 5;
export const REVOKE_CONFIRM_PHRASE = 'THU HỒI';

/** Lý do không áp dụng được cho một người; null nếu đủ điều kiện (cùng luật với nút ở từng dòng của A-02). */
export function skipReason(action: BulkAction, employee: EmployeeView): string | null {
  if (action === 'revoke') {
    if (employee.credentialStatus === 'ACTIVE') return null;
    return employee.credentialStatus === 'REVOKED' ? 'Mã đã bị thu hồi' : 'Chưa được cấp mã';
  }
  return employee.active ? null : 'Đã nghỉ việc';
}

export function planBulk(action: BulkAction, employees: EmployeeView[]): BulkPlan {
  const plan: BulkPlan = { eligible: [], skipped: [] };
  for (const employee of employees) {
    const reason = skipReason(action, employee);
    if (reason === null) plan.eligible.push(employee);
    else plan.skipped.push({ employee, reason });
  }
  return plan;
}

/** Cấp lại: số người đang có mã ACTIVE sẽ bị thu hồi mã hiện tại trước khi cấp mã mới. */
export function countReplacedCredentials(eligible: EmployeeView[]): number {
  return eligible.filter((e) => e.credentialStatus === 'ACTIVE').length;
}

export function needsTypedConfirm(action: BulkAction, eligibleCount: number): boolean {
  return action === 'revoke' && eligibleCount >= TYPED_CONFIRM_THRESHOLD;
}

const normalize = (s: string) =>
  s
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .replace(/đ/gi, 'd')
    .trim()
    .replace(/\s+/g, ' ')
    .toUpperCase();

/** So khớp chữ xác nhận, bỏ qua hoa/thường, dấu tiếng Việt và khoảng trắng thừa ("thu hoi" cũng được). */
export function matchesPhrase(input: string, phrase: string): boolean {
  return normalize(input) === normalize(phrase);
}
