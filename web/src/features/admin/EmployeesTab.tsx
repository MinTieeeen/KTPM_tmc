// A-02 Tab Nhân viên – docs/web/screens/A-02-employees-tab.md (tìm kiếm, lọc, chọn nhiều dòng, thao tác hàng loạt A-05).
import { ChevronDown, RefreshCw, Search, SearchX, X } from 'lucide-react';
import { useEffect, useMemo, useRef, useState } from 'react';
import type { EmployeeView } from '@/shared/api/centralApi';
import { Alert } from '@/features/common/Alert';
import { Button } from '@/features/common/Button';
import { EmptyState } from '@/features/common/EmptyState';
import { Skeleton } from '@/features/common/Skeleton';
import { useDebouncedValue } from '@/features/common/useDebouncedValue';
import styles from './Admin.module.css';
import { BulkActionDialog } from './BulkActionDialog';
import type { BulkAction } from './bulkPlan';
import { EmployeeRow } from './EmployeeRow';
import type { QrAction } from './EmployeeRow';
import { QrActionDialog } from './QrActionDialog';
import { SelectionBar } from './SelectionBar';
import { useEmployeeSelection } from './useEmployeeSelection';
import { useEmployees } from './useEmployees';

const SEARCH_DEBOUNCE_MS = 300;
const SKELETON_ROWS = 8;

interface DialogTarget {
  action: QrAction;
  employee: EmployeeView;
}

interface BulkTarget {
  action: BulkAction;
  /** Ảnh chụp những người đang chọn lúc mở hộp thoại (không đổi khi dữ liệu cập nhật giữa chừng). */
  employees: EmployeeView[];
}

const HEADERS = ['Mã', 'Họ tên', 'Bộ phận', 'Nhà máy', 'Nhân viên', 'Mã QR', 'Ngày cấp', 'Thao tác'];

/** Ô "chọn tất cả" ở đầu bảng: có trạng thái nửa chọn khi mới chọn một phần các dòng đang hiển thị. */
function SelectAllCheckbox({ checked, indeterminate, onChange }: { checked: boolean; indeterminate: boolean; onChange: () => void }) {
  const ref = useRef<HTMLInputElement>(null);
  useEffect(() => {
    if (ref.current) ref.current.indeterminate = indeterminate;
  }, [indeterminate]);
  return (
    <input
      ref={ref}
      type="checkbox"
      className={styles.checkbox}
      checked={checked}
      aria-label="Chọn tất cả nhân viên đang hiển thị"
      onChange={onChange}
    />
  );
}

export function EmployeesTab() {
  const [search, setSearch] = useState('');
  const [department, setDepartment] = useState('');
  const [dialog, setDialog] = useState<DialogTarget | null>(null);
  const [bulk, setBulk] = useState<BulkTarget | null>(null);
  // Khác null = đang "Chỉ xem người đã chọn": cố định danh sách mã để dòng không biến mất ngay khi vừa bỏ tick.
  const [pinned, setPinned] = useState<string[] | null>(null);
  const q = useDebouncedValue(search.trim(), SEARCH_DEBOUNCE_MS);

  const filtered = useEmployees(department, q);
  // Danh sách chưa lọc (cùng khóa với `filtered` khi chưa lọc nên không tốn thêm request).
  const unfiltered = useEmployees('', '');

  const departments = useMemo(() => {
    const map = new Map<string, string>();
    unfiltered.data?.forEach((e) => map.set(e.departmentId, e.departmentName));
    return [...map.entries()].sort((a, b) => a[1].localeCompare(b[1]));
  }, [unfiltered.data]);

  const rows = useMemo(() => {
    if (!pinned) return filtered.data;
    const byCode = new Map((unfiltered.data ?? []).map((e) => [e.code, e]));
    return pinned.flatMap((code) => byCode.get(code) ?? []);
  }, [pinned, filtered.data, unfiltered.data]);
  const visibleCodes = useMemo(() => rows?.map((e) => e.code) ?? [], [rows]);
  const selection = useEmployeeSelection(visibleCodes);

  // Người đang chọn lấy từ danh sách chưa lọc để vẫn thấy được khi bộ lọc đang ẩn họ.
  const selectedEmployees = useMemo(
    () => (unfiltered.data ?? []).filter((e) => selection.selected.has(e.code)),
    [unfiltered.data, selection.selected],
  );
  const visibleSet = useMemo(() => new Set(visibleCodes), [visibleCodes]);
  const selectedVisible = visibleCodes.filter((c) => selection.selected.has(c)).length;
  const hiddenCount = selectedEmployees.filter((e) => !visibleSet.has(e.code)).length;
  const allVisibleSelected = visibleCodes.length > 0 && selectedVisible === visibleCodes.length;

  // Hết người được chọn thì thoát chế độ "Chỉ xem người đã chọn".
  useEffect(() => {
    if (pinned && selection.selected.size === 0) setPinned(null);
  }, [pinned, selection.selected.size]);

  const showError = !pinned && filtered.isError && !filtered.data;

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
        {rows && <span className={styles.count}>{rows.length} nhân viên</span>}
      </div>

      <SelectionBar
        employees={selectedEmployees}
        hiddenCount={hiddenCount}
        unselectedVisibleCount={visibleCodes.length - selectedVisible}
        onlySelected={pinned !== null}
        onToggleOnlySelected={() => setPinned((p) => (p ? null : selectedEmployees.map((e) => e.code)))}
        onRemove={selection.remove}
        onSelectAllVisible={() => selection.selectMany(visibleCodes)}
        onClear={selection.clear}
        onBulk={(action) => setBulk({ action, employees: selectedEmployees })}
      />

      {showError ? (
        <div className={styles.errorBox}>
          <Alert kind="error">Không tải được danh sách nhân viên.</Alert>
          <Button variant="secondary" icon={RefreshCw} onClick={() => void filtered.refetch()}>
            Thử lại
          </Button>
        </div>
      ) : !rows ? (
        <div className={styles.tableCard} aria-busy="true" role="status" aria-label="Đang tải danh sách nhân viên">
          <table className={styles.table}>
            <thead>
              <tr>
                <th className={styles.selectCell} />
                {HEADERS.map((h) => (
                  <th key={h}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {Array.from({ length: SKELETON_ROWS }, (_, i) => (
                <tr key={i}>
                  <td className={styles.selectCell}>
                    <Skeleton width={17} height={17} />
                  </td>
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
      ) : rows.length === 0 ? (
        <EmptyState icon={SearchX}>Không có nhân viên nào khớp bộ lọc</EmptyState>
      ) : (
        <div className={`${styles.tableCard} ${styles.tableCardTall}`}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th className={styles.selectCell}>
                  <SelectAllCheckbox
                    checked={allVisibleSelected}
                    indeterminate={selectedVisible > 0 && !allVisibleSelected}
                    onChange={selection.toggleAllVisible}
                  />
                </th>
                {HEADERS.map((h) => (
                  <th key={h}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {rows.map((e) => (
                <EmployeeRow
                  key={e.code}
                  employee={e}
                  selected={selection.selected.has(e.code)}
                  onSelect={selection.toggle}
                  onAction={(action, employee) => setDialog({ action, employee })}
                />
              ))}
            </tbody>
          </table>
        </div>
      )}

      {dialog && <QrActionDialog action={dialog.action} employee={dialog.employee} onClose={() => setDialog(null)} />}
      {bulk && (
        <BulkActionDialog
          action={bulk.action}
          employees={bulk.employees}
          onClose={(succeededCodes) => {
            selection.removeMany(succeededCodes);
            setBulk(null);
          }}
        />
      )}
    </section>
  );
}
