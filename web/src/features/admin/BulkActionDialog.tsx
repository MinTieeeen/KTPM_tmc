// A-05 Hộp thoại thao tác hàng loạt – docs/web/screens/A-05-bulk-action-dialog.md (xác nhận → chạy tuần tự → kết quả từng người).
import { CircleCheck, CircleMinus, CircleX, Clock, RefreshCw, ShieldOff } from 'lucide-react';
import { useMemo, useRef, useState } from 'react';
import { centralApi } from '@/shared/api/centralApi';
import type { EmployeeView } from '@/shared/api/centralApi';
import { Alert } from '@/features/common/Alert';
import { Button } from '@/features/common/Button';
import { Dialog } from '@/features/common/Dialog';
import { Spinner } from '@/features/common/Spinner';
import { useToast } from '@/features/common/toastContext';
import styles from './Admin.module.css';
import {
  REVOKE_CONFIRM_PHRASE,
  TYPED_CONFIRM_THRESHOLD,
  countReplacedCredentials,
  matchesPhrase,
  needsTypedConfirm,
  planBulk,
} from './bulkPlan';
import type { BulkAction } from './bulkPlan';
import { runBulk, summarizeRun } from './bulkRunner';
import type { ItemState } from './bulkRunner';
import { useReloadEmployees, useReplaceEmployee } from './useEmployees';

const REASON_MIN = 3;
const REASON_MAX = 200;

type Phase = 'confirm' | 'running' | 'done';

interface Props {
  action: BulkAction;
  /** Những người đang chọn tại thời điểm mở hộp thoại. */
  employees: EmployeeView[];
  /** Đóng hộp thoại; `succeededCodes` là những người đã xử lý xong (để bỏ họ khỏi lựa chọn). */
  onClose: (succeededCodes: string[]) => void;
}

function ItemIcon({ status }: { status: ItemState['status'] }) {
  if (status === 'running') return <Spinner />;
  if (status === 'success') return <CircleCheck size={17} className={styles.iconSuccess} aria-hidden="true" />;
  if (status === 'failed') return <CircleX size={17} className={styles.iconFailed} aria-hidden="true" />;
  if (status === 'cancelled') return <CircleMinus size={17} className={styles.iconMuted} aria-hidden="true" />;
  return <Clock size={17} className={styles.iconMuted} aria-hidden="true" />;
}

export function BulkActionDialog({ action, employees, onClose }: Props) {
  const toast = useToast();
  const replaceEmployee = useReplaceEmployee();
  const reloadEmployees = useReloadEmployees();

  const isRevoke = action === 'revoke';
  const plan = useMemo(() => planBulk(action, employees), [action, employees]);
  const byCode = useMemo(() => new Map(employees.map((e) => [e.code, e])), [employees]);
  const replacedCount = countReplacedCredentials(plan.eligible);

  const [phase, setPhase] = useState<Phase>('confirm');
  const [reason, setReason] = useState('');
  const [touched, setTouched] = useState(false);
  const [confirmText, setConfirmText] = useState('');
  const [items, setItems] = useState<ItemState[]>([]);
  const [stopping, setStopping] = useState(false);
  const cancelRequested = useRef(false);

  const trimmed = reason.trim();
  const reasonValid = trimmed.length >= REASON_MIN && trimmed.length <= REASON_MAX;
  const reasonInvalid = touched && !reasonValid;
  const typedNeeded = needsTypedConfirm(action, plan.eligible.length);
  const typedOk = !typedNeeded || matchesPhrase(confirmText, REVOKE_CONFIRM_PHRASE);
  const canStart = plan.eligible.length > 0 && (!isRevoke || reasonValid) && typedOk;

  const summary = summarizeRun(items);
  const finished = summary.success + summary.failed + summary.cancelled;
  const succeededCodes = items.filter((i) => i.status === 'success').map((i) => i.code);
  const retryCodes = items.filter((i) => i.status === 'failed' || i.status === 'cancelled').map((i) => i.code);

  const nameOf = (code: string) => {
    const e = byCode.get(code);
    return e ? `${e.code} – ${e.fullName}` : code;
  };

  const start = async (codes: string[]) => {
    cancelRequested.current = false;
    setStopping(false);
    setItems((prev) =>
      prev.length === 0
        ? codes.map((code) => ({ code, status: 'pending' as const }))
        : prev.map((i) => (codes.includes(i.code) ? { code: i.code, status: 'pending' as const } : i)),
    );
    setPhase('running');
    await runBulk({
      codes,
      perform: (code) => (isRevoke ? centralApi.revokeQr(code, trimmed) : centralApi.issueQr(code)),
      isCancelled: () => cancelRequested.current,
      onItem: (state, employee) => {
        setItems((prev) => prev.map((i) => (i.code === state.code ? state : i)));
        if (employee) replaceEmployee(employee);
      },
    });
    setPhase('done');
    void reloadEmployees();
  };

  const close = () => {
    if (succeededCodes.length > 0) {
      toast.show(
        isRevoke
          ? `Đã thu hồi mã QR của ${succeededCodes.length} người`
          : `Đã cấp mã QR mới cho ${succeededCodes.length} người`,
      );
    }
    onClose(succeededCodes);
  };

  const verb = isRevoke ? 'Thu hồi' : 'Cấp lại';
  const successLabel = isRevoke ? 'Đã thu hồi' : 'Đã cấp mã mới';

  const footer =
    phase === 'confirm' ? (
      <>
        <Button variant="secondary" onClick={close}>
          Hủy
        </Button>
        {plan.eligible.length > 0 && (
          <Button
            variant={isRevoke ? 'danger' : 'primary'}
            icon={isRevoke ? ShieldOff : RefreshCw}
            disabled={!canStart}
            onClick={() => void start(plan.eligible.map((e) => e.code))}
          >
            {verb} {plan.eligible.length} người
          </Button>
        )}
      </>
    ) : phase === 'running' ? (
      <Button variant="secondary" disabled={stopping} onClick={() => {
        cancelRequested.current = true;
        setStopping(true);
      }}>
        {stopping ? 'Đang dừng…' : 'Dừng'}
      </Button>
    ) : (
      <>
        {retryCodes.length > 0 && (
          <Button variant="secondary" icon={RefreshCw} onClick={() => void start(retryCodes)}>
            {summary.failed > 0 && summary.cancelled === 0 ? `Thử lại ${retryCodes.length} lỗi` : `Xử lý tiếp ${retryCodes.length} người`}
          </Button>
        )}
        <Button onClick={close}>Đóng</Button>
      </>
    );

  return (
    <Dialog
      title={isRevoke ? 'Thu hồi mã QR hàng loạt' : 'Cấp lại mã QR hàng loạt'}
      icon={isRevoke ? ShieldOff : RefreshCw}
      tone={isRevoke ? 'danger' : 'primary'}
      wide
      onClose={close}
      locked={phase === 'running'}
      footer={footer}
    >
      {phase === 'confirm' && (
        <div className={styles.dialogStack}>
          {plan.eligible.length === 0 ? (
            <Alert kind="warning">Không có ai trong số đã chọn đủ điều kiện để {verb.toLowerCase()} mã.</Alert>
          ) : (
            <>
              <p className={styles.bulkLead}>
                Áp dụng cho <strong>{plan.eligible.length}</strong> trong {employees.length} người đã chọn:
              </p>
              <ul className={styles.bulkList} aria-label="Người sẽ được xử lý">
                {plan.eligible.map((e) => (
                  <li key={e.code} className={styles.bulkItem}>
                    <span className={styles.bulkItemName}>
                      {e.code} – {e.fullName}
                    </span>
                  </li>
                ))}
              </ul>
            </>
          )}

          {plan.skipped.length > 0 && (
            <div className={styles.bulkSkipped}>
              <p>
                <strong>Bỏ qua {plan.skipped.length} người</strong> (không đủ điều kiện):
              </p>
              <ul className={styles.bulkList} aria-label="Người bị bỏ qua">
                {plan.skipped.map(({ employee, reason: why }) => (
                  <li key={employee.code} className={styles.bulkItem}>
                    <span className={styles.bulkItemName}>
                      {employee.code} – {employee.fullName}
                    </span>
                    <span className={styles.bulkItemStatus}>{why}</span>
                  </li>
                ))}
              </ul>
            </div>
          )}

          {plan.eligible.length > 0 && isRevoke && (
            <>
              <label className={styles.reasonLabel}>
                Lý do thu hồi (dùng chung cho {plan.eligible.length} người) *
                <textarea
                  className={`${styles.textarea} ${reasonInvalid ? styles.textareaInvalid : ''}`}
                  value={reason}
                  aria-invalid={reasonInvalid ? true : undefined}
                  onChange={(e) => {
                    setReason(e.target.value);
                    setTouched(true);
                  }}
                />
                <span className={styles.counter}>
                  {reason.length}/{REASON_MAX}
                </span>
              </label>
              {reasonInvalid && (
                <p className={styles.fieldError} role="alert">
                  Lý do cần từ {REASON_MIN} đến {REASON_MAX} ký tự
                </p>
              )}
              <Alert kind="info">
                Thu hồi có hiệu lực tại các trạm trong ≤ 60 giây; Portal của những người này sẽ ngừng hiện mã.
              </Alert>
              {typedNeeded && (
                <label className={styles.reasonLabel}>
                  <span>
                    Thu hồi từ {TYPED_CONFIRM_THRESHOLD} người trở lên: gõ <strong>{REVOKE_CONFIRM_PHRASE}</strong> để xác nhận
                  </span>
                  <input
                    className={styles.typedInput}
                    value={confirmText}
                    autoComplete="off"
                    aria-label={`Gõ ${REVOKE_CONFIRM_PHRASE} để xác nhận`}
                    onChange={(e) => setConfirmText(e.target.value)}
                  />
                </label>
              )}
            </>
          )}

          {plan.eligible.length > 0 && !isRevoke && replacedCount > 0 && (
            <Alert kind="warning">
              {replacedCount} người đang có mã ACTIVE: mã hiện tại sẽ bị thu hồi và một mã mới được cấp ngay.
            </Alert>
          )}
        </div>
      )}

      {phase !== 'confirm' && (
        <div className={styles.dialogStack}>
          <div>
            <p className={styles.bulkProgressText} role="status" aria-live="polite">
              {phase === 'running'
                ? `Đang xử lý ${finished}/${items.length}…`
                : `Hoàn tất: ${summary.success} thành công${summary.failed > 0 ? `, ${summary.failed} lỗi` : ''}${
                    summary.cancelled > 0 ? `, ${summary.cancelled} chưa xử lý` : ''
                  }`}
            </p>
            <div
              className={styles.bar}
              role="progressbar"
              aria-label="Tiến trình xử lý"
              aria-valuemin={0}
              aria-valuemax={items.length}
              aria-valuenow={finished}
            >
              <div className={styles.barFill} style={{ width: `${items.length === 0 ? 0 : (finished / items.length) * 100}%` }} />
            </div>
          </div>
          {phase === 'done' && summary.failed > 0 && (
            <Alert kind="error">Một số người chưa xử lý được; bạn có thể thử lại riêng phần lỗi.</Alert>
          )}
          <ul className={styles.bulkList} aria-label="Kết quả từng người">
            {items.map((item) => (
              <li key={item.code} className={styles.bulkItem}>
                <ItemIcon status={item.status} />
                <span className={styles.bulkItemName}>{nameOf(item.code)}</span>
                <span
                  className={`${styles.bulkItemStatus} ${
                    item.status === 'failed' ? styles.statusFailed : item.status === 'success' ? styles.statusSuccess : ''
                  }`}
                >
                  {item.status === 'pending' && 'Đang chờ'}
                  {item.status === 'running' && 'Đang xử lý'}
                  {item.status === 'success' && successLabel}
                  {item.status === 'failed' && item.error}
                  {item.status === 'cancelled' && 'Chưa xử lý'}
                </span>
              </li>
            ))}
          </ul>
        </div>
      )}
    </Dialog>
  );
}
