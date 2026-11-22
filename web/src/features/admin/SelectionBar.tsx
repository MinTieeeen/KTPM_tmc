// Khung "Đã chọn" ở A-02: số lượng, thống kê màu, thao tác hàng loạt (ghi rõ số người đủ điều kiện) và từng người đang chọn.
import { RefreshCw, ShieldOff, X } from 'lucide-react';
import type { EmployeeView } from '@/shared/api/centralApi';
import { Badge } from '@/features/common/Badge';
import { Button } from '@/features/common/Button';
import styles from './Admin.module.css';
import { planBulk } from './bulkPlan';
import type { BulkAction } from './bulkPlan';
import { summarizeSelection, summaryParts } from './selectionSummary';

interface Props {
  /** Nhân viên đang chọn (đầy đủ, kể cả người đang bị bộ lọc ẩn), sắp theo mã. */
  employees: EmployeeView[];
  /** Số người đang chọn nhưng nằm ngoài các dòng đang hiển thị. */
  hiddenCount: number;
  /** Số dòng đang hiển thị chưa được chọn (để đề nghị "Chọn thêm"). */
  unselectedVisibleCount: number;
  /** Đang chỉ xem riêng những người đã chọn (bộ lọc tạm không áp dụng). */
  onlySelected: boolean;
  onToggleOnlySelected: () => void;
  onRemove: (code: string) => void;
  onSelectAllVisible: () => void;
  onClear: () => void;
  onBulk: (action: BulkAction) => void;
}

function chipTone(e: EmployeeView): 'green' | 'red' | 'gray' {
  if (!e.active) return 'gray';
  if (e.credentialStatus === 'ACTIVE') return 'green';
  return e.credentialStatus === 'REVOKED' ? 'red' : 'gray';
}

function chipTitle(e: EmployeeView): string {
  const credential = e.credentialStatus ? `mã ${e.credentialStatus}` : 'chưa cấp mã';
  return e.active ? credential : `${credential}, đã nghỉ`;
}

export function SelectionBar({
  employees,
  hiddenCount,
  unselectedVisibleCount,
  onlySelected,
  onToggleOnlySelected,
  onRemove,
  onSelectAllVisible,
  onClear,
  onBulk,
}: Props) {
  if (employees.length === 0) return null;
  const parts = summaryParts(summarizeSelection(employees));
  const revokeCount = planBulk('revoke', employees).eligible.length;
  const issueCount = planBulk('issue', employees).eligible.length;

  return (
    <section className={styles.selection} aria-label="Nhân viên đang chọn">
      <div className={styles.selectionHead}>
        <span className={styles.selectionCount} aria-hidden="true">
          {employees.length}
        </span>
        <div className={styles.selectionSummary}>
          <strong className={styles.selectionTitle}>Đã chọn {employees.length} nhân viên</strong>
          <div className={styles.selectionBadges}>
            {parts.map((part) => (
              <Badge key={part.key} color={part.color}>
                {part.label}
              </Badge>
            ))}
          </div>
        </div>
        <div className={styles.selectionActions}>
          <Button
            variant="dangerSoft"
            small
            icon={ShieldOff}
            disabled={revokeCount === 0}
            title={revokeCount === 0 ? 'Không ai trong số đã chọn có mã ACTIVE để thu hồi' : undefined}
            onClick={() => onBulk('revoke')}
          >
            Thu hồi mã ({revokeCount})
          </Button>
          <Button
            variant="secondary"
            small
            icon={RefreshCw}
            disabled={issueCount === 0}
            title={issueCount === 0 ? 'Không ai trong số đã chọn đang làm việc để cấp lại mã' : undefined}
            onClick={() => onBulk('issue')}
          >
            Cấp lại mã ({issueCount})
          </Button>
        </div>
      </div>

      <div className={styles.selectionTools}>
        <label className={styles.onlySelected}>
          <input type="checkbox" checked={onlySelected} onChange={onToggleOnlySelected} />
          Chỉ xem người đã chọn
        </label>
        {unselectedVisibleCount > 0 && (
          <Button variant="ghost" small onClick={onSelectAllVisible}>
            Chọn thêm {unselectedVisibleCount} kết quả đang hiển thị
          </Button>
        )}
        <Button variant="ghost" small icon={X} onClick={onClear}>
          Bỏ chọn tất cả
        </Button>
      </div>

      {onlySelected && (
        <p className={styles.selectionNote}>Đang chỉ hiện người đã chọn; ô tìm kiếm và bộ lọc tạm thời không áp dụng.</p>
      )}
      {hiddenCount > 0 && !onlySelected && (
        <p className={styles.selectionNote}>{hiddenCount} người đã chọn đang nằm ngoài bộ lọc hiện tại.</p>
      )}

      <ul className={styles.chips}>
        {employees.map((e) => (
          <li key={e.code} className={styles.chip} title={chipTitle(e)}>
            <span className={`${styles.chipDot} ${styles[`dot_${chipTone(e)}`]}`} aria-hidden="true" />
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
