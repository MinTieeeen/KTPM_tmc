// Khung hiển thị các nhân viên đang chọn ở A-02: số lượng, thống kê, từng người (bỏ chọn được) và nút bỏ chọn tất cả.
import { CheckSquare, X } from 'lucide-react';
import type { EmployeeView } from '@/shared/api/centralApi';
import { Button } from '@/features/common/Button';
import styles from './Admin.module.css';
import { summarizeSelection, summaryParts } from './selectionSummary';

interface Props {
  /** Nhân viên đang chọn (đầy đủ, kể cả người đang bị bộ lọc ẩn), sắp theo mã. */
  employees: EmployeeView[];
  /** Số người đang chọn nhưng nằm ngoài danh sách đang hiển thị. */
  hiddenCount: number;
  /** Số dòng đang hiển thị chưa được chọn (để đề nghị "Chọn tất cả"). */
  unselectedVisibleCount: number;
  onRemove: (code: string) => void;
  onSelectAllVisible: () => void;
  onClear: () => void;
}

export function SelectionBar({ employees, hiddenCount, unselectedVisibleCount, onRemove, onSelectAllVisible, onClear }: Props) {
  if (employees.length === 0) return null;
  const parts = summaryParts(summarizeSelection(employees));

  return (
    <section className={styles.selection} aria-label="Nhân viên đang chọn">
      <div className={styles.selectionHead}>
        <CheckSquare size={18} aria-hidden="true" />
        <strong className={styles.selectionTitle}>Đã chọn {employees.length} nhân viên</strong>
        {parts.length > 0 && <span className={styles.selectionMeta}>{parts.join(' · ')}</span>}
        <div className={styles.selectionActions}>
          {unselectedVisibleCount > 0 && (
            <Button variant="ghost" small onClick={onSelectAllVisible}>
              Chọn thêm {unselectedVisibleCount} kết quả đang hiển thị
            </Button>
          )}
          <Button variant="secondary" small icon={X} onClick={onClear}>
            Bỏ chọn tất cả
          </Button>
        </div>
      </div>
      {hiddenCount > 0 && (
        <p className={styles.selectionNote}>{hiddenCount} người đã chọn đang nằm ngoài bộ lọc hiện tại.</p>
      )}
      <ul className={styles.chips}>
        {employees.map((e) => (
          <li key={e.code} className={styles.chip}>
            <span>
              {e.code} – {e.fullName}
            </span>
            <button type="button" className={styles.chipRemove} aria-label={`Bỏ chọn ${e.code}`} onClick={() => onRemove(e.code)}>
              <X size={13} aria-hidden="true" />
            </button>
          </li>
        ))}
      </ul>
    </section>
  );
}
