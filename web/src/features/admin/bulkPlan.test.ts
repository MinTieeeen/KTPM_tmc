import { describe, expect, it } from 'vitest';
import type { EmployeeView } from '@/shared/api/centralApi';
import {
  TYPED_CONFIRM_THRESHOLD,
  countReplacedCredentials,
  matchesPhrase,
  needsTypedConfirm,
  planBulk,
  skipReason,
} from './bulkPlan';

const emp = (code: string, over: Partial<EmployeeView> = {}): EmployeeView => ({
  code,
  fullName: `Nhan vien ${code}`,
  departmentId: 'D',
  departmentName: 'D',
  factoryId: 'BD',
  active: true,
  credentialStatus: 'ACTIVE',
  ...over,
});

describe('skipReason', () => {
  it('thu hồi: chỉ người có mã ACTIVE; nêu đúng lý do với người còn lại', () => {
    expect(skipReason('revoke', emp('E1'))).toBeNull();
    expect(skipReason('revoke', emp('E2', { credentialStatus: 'REVOKED' }))).toBe('Mã đã bị thu hồi');
    expect(skipReason('revoke', emp('E3', { credentialStatus: undefined }))).toBe('Chưa được cấp mã');
  });

  it('thu hồi áp dụng cả cho người đã nghỉ còn mã ACTIVE (giống nút ở từng dòng)', () => {
    expect(skipReason('revoke', emp('E4', { active: false }))).toBeNull();
  });

  it('cấp lại: chỉ người đang làm việc, bất kể trạng thái mã', () => {
    expect(skipReason('issue', emp('E1'))).toBeNull();
    expect(skipReason('issue', emp('E2', { credentialStatus: 'REVOKED' }))).toBeNull();
    expect(skipReason('issue', emp('E3', { credentialStatus: undefined }))).toBeNull();
    expect(skipReason('issue', emp('E4', { active: false }))).toBe('Đã nghỉ việc');
  });
});

describe('planBulk', () => {
  const list = [
    emp('E1'),
    emp('E2', { credentialStatus: 'REVOKED' }),
    emp('E3', { credentialStatus: undefined }),
    emp('E4', { active: false }),
  ];

  it('thu hồi: E1 và E4 đủ điều kiện, E2 và E3 bị bỏ qua kèm lý do', () => {
    const plan = planBulk('revoke', list);
    expect(plan.eligible.map((e) => e.code)).toEqual(['E1', 'E4']);
    expect(plan.skipped.map((s) => [s.employee.code, s.reason])).toEqual([
      ['E2', 'Mã đã bị thu hồi'],
      ['E3', 'Chưa được cấp mã'],
    ]);
  });

  it('cấp lại: chỉ E4 (đã nghỉ) bị bỏ qua', () => {
    const plan = planBulk('issue', list);
    expect(plan.eligible.map((e) => e.code)).toEqual(['E1', 'E2', 'E3']);
    expect(plan.skipped.map((s) => s.employee.code)).toEqual(['E4']);
  });

  it('danh sách rỗng', () => {
    expect(planBulk('revoke', [])).toEqual({ eligible: [], skipped: [] });
  });
});

describe('countReplacedCredentials', () => {
  it('đếm người đang có mã ACTIVE (mã đó sẽ bị thu hồi khi cấp lại)', () => {
    expect(
      countReplacedCredentials([emp('E1'), emp('E2', { credentialStatus: 'REVOKED' }), emp('E3', { credentialStatus: undefined })]),
    ).toBe(1);
  });
});

describe('needsTypedConfirm', () => {
  it('chỉ thu hồi và từ đúng ngưỡng trở lên', () => {
    expect(needsTypedConfirm('revoke', TYPED_CONFIRM_THRESHOLD - 1)).toBe(false);
    expect(needsTypedConfirm('revoke', TYPED_CONFIRM_THRESHOLD)).toBe(true);
    expect(needsTypedConfirm('issue', 20)).toBe(false);
  });
});

describe('matchesPhrase', () => {
  it('bỏ qua hoa/thường, dấu và khoảng trắng thừa', () => {
    expect(matchesPhrase('THU HỒI', 'THU HỒI')).toBe(true);
    expect(matchesPhrase('thu hoi', 'THU HỒI')).toBe(true);
    expect(matchesPhrase('  Thu   Hồi ', 'THU HỒI')).toBe(true);
  });

  it('chữ khác hoặc thiếu thì không khớp', () => {
    expect(matchesPhrase('thu', 'THU HỒI')).toBe(false);
    expect(matchesPhrase('', 'THU HỒI')).toBe(false);
    expect(matchesPhrase('thu hồi hết', 'THU HỒI')).toBe(false);
  });
});
