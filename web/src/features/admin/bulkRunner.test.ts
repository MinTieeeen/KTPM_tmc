import { describe, expect, it, vi } from 'vitest';
import { ApiError, NETWORK_ERROR } from '@/shared/api/apiError';
import type { EmployeeView } from '@/shared/api/centralApi';
import { runBulk, summarizeRun } from './bulkRunner';
import type { ItemState } from './bulkRunner';

const emp = (code: string): EmployeeView => ({
  code,
  fullName: code,
  departmentId: 'D',
  departmentName: 'D',
  factoryId: 'BD',
  active: true,
  credentialStatus: 'REVOKED',
});

const setup = (perform: (code: string) => Promise<EmployeeView>, isCancelled = () => false) => {
  const events: ItemState[] = [];
  const employees: EmployeeView[] = [];
  const run = (codes: string[]) =>
    runBulk({
      codes,
      perform,
      isCancelled,
      onItem: (state, employee) => {
        events.push(state);
        if (employee) employees.push(employee);
      },
    });
  return { run, events, employees };
};

describe('runBulk', () => {
  it('chạy tuần tự từng người: running → success, trả EmployeeView mới', async () => {
    const order: string[] = [];
    const { run, events, employees } = setup(async (code) => {
      order.push(`bắt đầu ${code}`);
      await Promise.resolve();
      order.push(`xong ${code}`);
      return emp(code);
    });
    const results = await run(['E1', 'E2']);
    expect(order).toEqual(['bắt đầu E1', 'xong E1', 'bắt đầu E2', 'xong E2']); // không chạy song song
    expect(results.map((r) => r.status)).toEqual(['success', 'success']);
    expect(events.map((e) => `${e.code}:${e.status}`)).toEqual(['E1:running', 'E1:success', 'E2:running', 'E2:success']);
    expect(employees.map((e) => e.code)).toEqual(['E1', 'E2']);
  });

  it('lỗi một người không làm hỏng cả lô; lỗi có câu tiếng Việt theo mã', async () => {
    const { run } = setup(async (code) => {
      if (code === 'E2') throw new ApiError(409, 'NO_ACTIVE_CREDENTIAL');
      return emp(code);
    });
    const results = await run(['E1', 'E2', 'E3']);
    expect(results.map((r) => r.status)).toEqual(['success', 'failed', 'success']);
    expect(results[1]!.error).toBe('Nhân viên không có mã QR đang hoạt động');
  });

  it('lỗi mạng cũng chỉ tính cho người đó và lô vẫn chạy tiếp', async () => {
    const { run } = setup(async (code) => {
      if (code === 'E1') throw new ApiError(0, NETWORK_ERROR);
      return emp(code);
    });
    const results = await run(['E1', 'E2']);
    expect(results.map((r) => r.status)).toEqual(['failed', 'success']);
    expect(results[0]!.error).toBe('Không kết nối được máy chủ, thử lại');
  });

  it('lỗi không phải ApiError (bất ngờ) được coi như lỗi mạng', async () => {
    const { run } = setup(async () => {
      throw new Error('bất ngờ');
    });
    expect((await run(['E1']))[0]!.status).toBe('failed');
  });

  it.each([401, 403])('gặp %s thì dừng cả lô, các người còn lại là "cancelled"', async (status) => {
    const perform = vi.fn(async (code: string) => {
      if (code === 'E2') throw new ApiError(status, status === 401 ? 'UNAUTHENTICATED' : 'FORBIDDEN');
      return emp(code);
    });
    const { run } = setup(perform);
    const results = await run(['E1', 'E2', 'E3', 'E4']);
    expect(results.map((r) => r.status)).toEqual(['success', 'failed', 'cancelled', 'cancelled']);
    expect(perform).toHaveBeenCalledTimes(2);
  });

  it('Dừng giữa chừng: người đang chạy xong, những người sau chuyển "cancelled"', async () => {
    let cancelled = false;
    const { run } = setup(async (code) => {
      if (code === 'E2') cancelled = true; // bấm Dừng trong lúc E2 đang xử lý
      return emp(code);
    }, () => cancelled);
    const results = await run(['E1', 'E2', 'E3', 'E4']);
    expect(results.map((r) => r.status)).toEqual(['success', 'success', 'cancelled', 'cancelled']);
  });

  it('danh sách rỗng', async () => {
    const { run } = setup(async (code) => emp(code));
    expect(await run([])).toEqual([]);
  });
});

describe('summarizeRun', () => {
  it('đếm theo trạng thái', () => {
    const items: ItemState[] = [
      { code: 'E1', status: 'success' },
      { code: 'E2', status: 'success' },
      { code: 'E3', status: 'failed', error: 'x' },
      { code: 'E4', status: 'cancelled' },
      { code: 'E5', status: 'pending' },
      { code: 'E6', status: 'running' },
    ];
    expect(summarizeRun(items)).toEqual({ success: 2, failed: 1, cancelled: 1, pending: 2 });
  });
});
