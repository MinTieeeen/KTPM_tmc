import { describe, expect, it } from 'vitest';
import type { EmployeeView } from '@/shared/api/centralApi';
import { summarizeSelection, summaryParts } from './selectionSummary';

const emp = (over: Partial<EmployeeView>): EmployeeView => ({
  code: 'E0001',
  fullName: 'A',
  departmentId: 'D',
  departmentName: 'D',
  factoryId: 'BD',
  active: true,
  ...over,
});

describe('summarizeSelection', () => {
  it('đếm theo trạng thái mã và nghỉ việc (một người có thể vừa nghỉ vừa còn mã ACTIVE)', () => {
    const s = summarizeSelection([
      emp({ code: 'E1', credentialStatus: 'ACTIVE' }),
      emp({ code: 'E2', credentialStatus: 'ACTIVE' }),
      emp({ code: 'E3', credentialStatus: 'REVOKED' }),
      emp({ code: 'E4' }),
      emp({ code: 'E5', credentialStatus: 'ACTIVE', active: false }),
    ]);
    expect(s).toEqual({ total: 5, active: 3, revoked: 1, noCredential: 1, inactive: 1 });
  });

  it('danh sách rỗng', () => {
    expect(summarizeSelection([])).toEqual({ total: 0, active: 0, revoked: 0, noCredential: 0, inactive: 0 });
  });
});

describe('summaryParts', () => {
  it('chỉ nêu các nhóm có người, theo thứ tự cố định, kèm màu khớp nhãn trong bảng', () => {
    expect(summaryParts({ total: 5, active: 3, revoked: 1, noCredential: 0, inactive: 1 })).toEqual([
      { key: 'active', label: '3 mã ACTIVE', color: 'green' },
      { key: 'revoked', label: '1 mã REVOKED', color: 'red' },
      { key: 'inactive', label: '1 đã nghỉ', color: 'gray' },
    ]);
    expect(summaryParts({ total: 0, active: 0, revoked: 0, noCredential: 0, inactive: 0 })).toEqual([]);
  });
});
