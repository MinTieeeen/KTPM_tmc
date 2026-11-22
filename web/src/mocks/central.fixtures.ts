// Dữ liệu mẫu của Central theo seed V2__seed_demo.sql (20 nhân viên, 4 trạm, 2 gate); chỉ dùng cho dev/test.
import type { EmployeeView, StationView } from '@/shared/api/centralApi';

export const DEMO_PASSWORD = 'Demo@2026';

const DEPARTMENTS = {
  'BD-ASM': { name: 'Assembly', factory: 'BD' },
  'BD-QC': { name: 'Quality Control', factory: 'BD' },
  'LA-WH': { name: 'Warehouse', factory: 'LA' },
  'LA-PKG': { name: 'Packaging', factory: 'LA' },
} as const;

type DepartmentId = keyof typeof DEPARTMENTS;

const RAW_EMPLOYEES: ReadonlyArray<readonly [string, string, DepartmentId, boolean]> = [
  ['E0001', 'Nguyen Van An', 'BD-ASM', true],
  ['E0002', 'Tran Thi Bich', 'BD-ASM', true],
  ['E0003', 'Le Van Cuong', 'BD-ASM', true],
  ['E0004', 'Pham Thi Dung', 'BD-ASM', true],
  ['E0005', 'Hoang Van Em', 'BD-ASM', true],
  ['E0006', 'Vu Thi Phuong', 'BD-QC', true],
  ['E0007', 'Dang Van Giang', 'BD-QC', true],
  ['E0008', 'Bui Thi Hoa', 'BD-QC', true],
  ['E0009', 'Do Van Hung', 'BD-QC', true],
  ['E0010', 'Ngo Thi Khanh', 'BD-QC', true],
  ['E0011', 'Duong Van Long', 'LA-WH', true],
  ['E0012', 'Ly Thi Mai', 'LA-WH', true],
  ['E0013', 'Truong Van Nam', 'LA-WH', true],
  ['E0014', 'Dinh Thi Oanh', 'LA-WH', true],
  ['E0015', 'Mai Van Phuc', 'LA-WH', true],
  ['E0016', 'Ha Thi Quynh', 'LA-PKG', true],
  ['E0017', 'Phan Van Son', 'LA-PKG', true],
  ['E0018', 'Cao Thi Thu', 'LA-PKG', true],
  ['E0019', 'Lam Van Uy', 'LA-PKG', false],
  ['E0020', 'Tran Thi Binh', 'LA-PKG', true],
];

export function createEmployees(): EmployeeView[] {
  return RAW_EMPLOYEES.map(([code, fullName, departmentId, active], i) => {
    const department = DEPARTMENTS[departmentId];
    return {
      code,
      fullName,
      departmentId,
      departmentName: department.name,
      factoryId: department.factory,
      active,
      credentialId: `c0000000-0000-4000-8000-${String(i + 1).padStart(12, '0')}`,
      credentialStatus: code === 'E0020' ? 'REVOKED' : 'ACTIVE',
      issuedAt: '2026-11-16T01:00:00Z',
    };
  });
}

/** Tài khoản đăng nhập: e0001…e0020 (e0019 bị khóa) và admin. */
export function isKnownUser(username: string): boolean {
  return username === 'admin' || /^e00(0[1-9]|1\d|20)$/.test(username);
}

export function isDisabledUser(username: string): boolean {
  return username === 'e0019';
}

export interface MockGate {
  gateId: string;
  online: boolean;
  outboxDepth: number;
}

export function createGates(): Record<'BD' | 'LA', MockGate> {
  return {
    BD: { gateId: 'gate-bd', online: true, outboxDepth: 0 },
    LA: { gateId: 'gate-la', online: true, outboxDepth: 0 },
  };
}

type StationSeed = Pick<StationView, 'stationId' | 'factoryId' | 'direction'>;

export const STATION_SEEDS: StationSeed[] = [
  { stationId: 'BD-IN-01', factoryId: 'BD', direction: 'IN' },
  { stationId: 'BD-OUT-01', factoryId: 'BD', direction: 'OUT' },
  { stationId: 'LA-IN-01', factoryId: 'LA', direction: 'IN' },
  { stationId: 'LA-OUT-01', factoryId: 'LA', direction: 'OUT' },
];
