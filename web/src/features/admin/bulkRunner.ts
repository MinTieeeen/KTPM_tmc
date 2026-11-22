// Chạy thao tác hàng loạt TUẦN TỰ từng người (Central chưa có API hàng loạt): lỗi một người không làm hỏng cả lô.
import { asApiError, networkError } from '@/shared/api/apiError';
import type { EmployeeView } from '@/shared/api/centralApi';
import { adminErrorText } from '@/features/common/errorTexts';

export type ItemStatus = 'pending' | 'running' | 'success' | 'failed' | 'cancelled';

export interface ItemState {
  code: string;
  status: ItemStatus;
  /** Câu tiếng Việt khi `status = 'failed'`. */
  error?: string;
}

export interface RunOptions {
  codes: string[];
  perform: (code: string) => Promise<EmployeeView>;
  /** Gọi mỗi khi trạng thái một người đổi; `employee` có khi thành công (để cập nhật dòng ngay). */
  onItem: (state: ItemState, employee?: EmployeeView) => void;
  /** Được hỏi trước mỗi người; true thì các người còn lại chuyển sang 'cancelled'. */
  isCancelled: () => boolean;
}

/** 401/403: phiên hết hạn hoặc mất quyền, tiếp tục chỉ sinh thêm lỗi nên dừng cả lô. */
function isFatal(status: number | undefined): boolean {
  return status === 401 || status === 403;
}

export async function runBulk({ codes, perform, onItem, isCancelled }: RunOptions): Promise<ItemState[]> {
  const results: ItemState[] = [];
  const push = (state: ItemState, employee?: EmployeeView) => {
    results.push(state);
    onItem(state, employee);
  };

  let stopped = false;
  for (const code of codes) {
    if (stopped || isCancelled()) {
      push({ code, status: 'cancelled' });
      continue;
    }
    onItem({ code, status: 'running' });
    try {
      const employee = await perform(code);
      push({ code, status: 'success' }, employee);
    } catch (err) {
      const apiError = asApiError(err) ?? networkError();
      push({ code, status: 'failed', error: adminErrorText(apiError) });
      if (isFatal(apiError.status)) stopped = true;
    }
  }
  return results;
}

export interface RunSummary {
  success: number;
  failed: number;
  cancelled: number;
  pending: number;
}

export function summarizeRun(items: ItemState[]): RunSummary {
  const count = (s: ItemStatus) => items.filter((i) => i.status === s).length;
  return { success: count('success'), failed: count('failed'), cancelled: count('cancelled'), pending: count('pending') + count('running') };
}
