// A-02 Tab Nhân viên – docs/web/screens/A-02-employees-tab.md (tìm kiếm debounce 300 ms, lọc bộ phận).
import { ChevronDown, RefreshCw, Search, SearchX, X } from 'lucide-react';
import { useMemo, useState } from 'react';
import type { EmployeeView } from '@/shared/api/centralApi';
import { Alert } from '@/features/common/Alert';
import { Button } from '@/features/common/Button';
import { EmptyState } from '@/features/common/EmptyState';
import { Skeleton } from '@/features/common/Skeleton';
import { useDebouncedValue } from '@/features/common/useDebouncedValue';
import styles from './Admin.module.css';
import { EmployeeRow } from './EmployeeRow';
import type { QrAction } from './EmployeeRow';
import { QrActionDialog } from './QrActionDialog';
import { useEmployees } from './useEmployees';

const SEARCH_DEBOUNCE_MS = 300;
const SKELETON_ROWS = 8;

interface DialogTarget {
  action: QrAction;
  employee: EmployeeView;
}

const HEADERS = ['Mã', 'Họ tên', 'Bộ phận', 'Nhà máy', 'Nhân viên', 'Mã QR', 'Ngày cấp', 'Thao tác'];

export function EmployeesTab() {
  const [search, setSearch] = useState('');
  const [department, setDepartment] = useState('');
  const [dialog, setDialog] = useState<DialogTarget | null>(null);
  const q = useDebouncedValue(search.trim(), SEARCH_DEBOUNCE_MS);

  const filtered = useEmployees(department, q);
  // Danh sách chưa lọc (cùng khóa với `filtered` khi chưa lọc nên không tốn thêm request).
  const unfiltered = useEmployees('', '');

  const departments = useMemo(() => {
    const map = new Map<string, string>();
    unfiltered.data?.forEach((e) => map.set(e.departmentId, e.departmentName));
    return [...map.entries()].sort((a, b) => a[1].localeCompare(b[1]));
  }, [unfiltered.data]);

  const employees = filtered.data;

  return (
    <section aria-label="Nhân viên">
      <div className={styles.toolbar}>
        <div className={styles.searchWrap}>
          <Search className={styles.searchIcon} size={17} aria-hidden="true" />
          <input
            type="search"
            className={styles.search}
            placeholder="Tìm theo mã hoặc họ tên…"
            aria-label="Tìm nhân viên"
            maxLength={100}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          {search && (
            <button type="button" className={styles.clear} aria-label="Xóa tìm kiếm" onClick={() => setSearch('')}>
              <X size={16} aria-hidden="true" />
            </button>
          )}
        </div>
        <div className={styles.selectWrap}>
          <select
            className={styles.select}
            aria-label="Lọc bộ phận"
            value={department}
            onChange={(e) => setDepartment(e.target.value)}
          >
            <option value="">Tất cả bộ phận</option>
            {departments.map(([id, name]) => (
              <option key={id} value={id}>
                {name}
              </option>
            ))}
          </select>
          <ChevronDown className={styles.selectIcon} size={16} aria-hidden="true" />
        </div>
        {employees && <span className={styles.count}>{employees.length} nhân viên</span>}
      </div>

      {filtered.isError && !employees ? (
        <div className={styles.errorBox}>
          <Alert kind="error">Không tải được danh sách nhân viên.</Alert>
          <Button variant="secondary" icon={RefreshCw} onClick={() => void filtered.refetch()}>
            Thử lại
          </Button>
        </div>
      ) : !employees ? (
        <div className={styles.tableCard} aria-busy="true" role="status" aria-label="Đang tải danh sách nhân viên">
          <table className={styles.table}>
            <thead>
              <tr>
                {HEADERS.map((h) => (
                  <th key={h}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {Array.from({ length: SKELETON_ROWS }, (_, i) => (
                <tr key={i}>
                  {HEADERS.map((h) => (
                    <td key={h}>
                      <Skeleton width={h === 'Họ tên' ? 120 : 64} />
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : employees.length === 0 ? (
        <EmptyState icon={SearchX}>Không có nhân viên nào khớp bộ lọc</EmptyState>
      ) : (
        <div className={`${styles.tableCard} ${styles.tableCardTall}`}>
          <table className={styles.table}>
            <thead>
              <tr>
                {HEADERS.map((h) => (
                  <th key={h}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {employees.map((e) => (
                <EmployeeRow key={e.code} employee={e} onAction={(action, employee) => setDialog({ action, employee })} />
              ))}
            </tbody>
          </table>
        </div>
      )}

      {dialog && <QrActionDialog action={dialog.action} employee={dialog.employee} onClose={() => setDialog(null)} />}
    </section>
  );
}
