// A-02 Tab Nhân viên – docs/web/screens/A-02-employees-tab.md (tìm kiếm debounce 300 ms, lọc bộ phận).
import { useMemo, useState } from 'react';
import type { EmployeeView } from '@/shared/api/centralApi';
import { Alert } from '@/features/common/Alert';
import { Button } from '@/features/common/Button';
import { Spinner } from '@/features/common/Spinner';
import { useDebouncedValue } from '@/features/common/useDebouncedValue';
import styles from './Admin.module.css';
import { EmployeeRow } from './EmployeeRow';
import type { QrAction } from './EmployeeRow';
import { QrActionDialog } from './QrActionDialog';
import { useEmployees } from './useEmployees';

const SEARCH_DEBOUNCE_MS = 300;

interface DialogTarget {
  action: QrAction;
  employee: EmployeeView;
}

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
        <input
          type="search"
          className={styles.search}
          placeholder="Tìm theo mã hoặc họ tên…"
          aria-label="Tìm nhân viên"
          maxLength={100}
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
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
        {employees && <span className={styles.count}>{employees.length} nhân viên</span>}
      </div>

      {filtered.isError && !employees ? (
        <div className={styles.errorBox}>
          <Alert kind="error">Không tải được danh sách nhân viên.</Alert>
          <Button variant="secondary" onClick={() => void filtered.refetch()}>
            Thử lại
          </Button>
        </div>
      ) : !employees ? (
        <Spinner />
      ) : employees.length === 0 ? (
        <div className={styles.empty}>Không có nhân viên nào khớp bộ lọc</div>
      ) : (
        <div className={styles.tableWrap}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Mã</th>
                <th>Họ tên</th>
                <th>Bộ phận</th>
                <th>Nhà máy</th>
                <th>Nhân viên</th>
                <th>Mã QR</th>
                <th>Ngày cấp</th>
                <th>Thao tác</th>
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
