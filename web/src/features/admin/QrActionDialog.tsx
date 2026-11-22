// A-03 Hộp thoại thao tác QR – docs/web/screens/A-03-qr-action-dialog.md (thu hồi cần lý do 3–200 ký tự).
import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { asApiError, networkError } from '@/shared/api/apiError';
import { centralApi } from '@/shared/api/centralApi';
import type { EmployeeView } from '@/shared/api/centralApi';
import { Alert } from '@/features/common/Alert';
import { Button } from '@/features/common/Button';
import { Dialog } from '@/features/common/Dialog';
import { adminErrorText, shouldReloadEmployees } from '@/features/common/errorTexts';
import { formatDate } from '@/features/common/format';
import { useToast } from '@/features/common/toastContext';
import styles from './Admin.module.css';
import type { QrAction } from './EmployeeRow';
import { useReloadEmployees, useReplaceEmployee } from './useEmployees';

const REASON_MIN = 3;
const REASON_MAX = 200;

interface Props {
  action: QrAction;
  employee: EmployeeView;
  onClose: () => void;
}

export function QrActionDialog({ action, employee, onClose }: Props) {
  const toast = useToast();
  const replaceEmployee = useReplaceEmployee();
  const reloadEmployees = useReloadEmployees();
  const [reason, setReason] = useState('');
  const [touched, setTouched] = useState(false);
  const [error, setError] = useState<string | null>(null);
  // Cờ đặt đồng bộ khi bấm xác nhận; isPending của React Query cập nhật chậm hơn một nhịp.
  const [sending, setSending] = useState(false);

  const isRevoke = action === 'revoke';
  const trimmed = reason.trim();
  const reasonValid = trimmed.length >= REASON_MIN && trimmed.length <= REASON_MAX;

  const mutation = useMutation({
    mutationFn: () => (isRevoke ? centralApi.revokeQr(employee.code, trimmed) : centralApi.issueQr(employee.code)),
    onSuccess: (updated) => {
      replaceEmployee(updated);
      toast.show(
        isRevoke ? `Đã thu hồi mã QR của ${employee.code}` : `Đã cấp mã QR mới cho ${employee.code}`,
      );
      onClose();
    },
    onError: (err) => {
      const apiError = asApiError(err) ?? networkError();
      const text = adminErrorText(apiError);
      if (shouldReloadEmployees(apiError)) {
        toast.show(text);
        void reloadEmployees();
        onClose();
        return;
      }
      setSending(false);
      setError(text);
    },
  });

  const submit = () => {
    if (sending || (isRevoke && !reasonValid)) return;
    setSending(true);
    setError(null);
    mutation.mutate();
  };

  const hasCredential = employee.credentialStatus !== undefined;

  return (
    <Dialog
      title={isRevoke ? 'Thu hồi mã QR' : 'Cấp lại mã QR'}
      onClose={onClose}
      locked={sending}
      footer={
        <>
          <Button variant="ghost" onClick={onClose} disabled={sending}>
            Hủy
          </Button>
          <Button
            variant={isRevoke ? 'danger' : 'primary'}
            onClick={submit}
            disabled={isRevoke && !reasonValid}
            loading={sending}
          >
            {isRevoke ? 'Thu hồi' : 'Cấp lại'}
          </Button>
        </>
      }
    >
      <div className={styles.dialogStack}>
        <div>
          <p className={styles.dialogInfo}>
            {employee.code} – {employee.fullName} · {employee.departmentName} ({employee.factoryId})
          </p>
          <p className={styles.dialogSub}>
            {hasCredential
              ? `Mã hiện tại: ${employee.credentialStatus}, cấp ${formatDate(employee.issuedAt)}`
              : 'Mã hiện tại: chưa cấp'}
          </p>
        </div>

        {isRevoke ? (
          <>
            <label className={styles.reasonLabel}>
              Lý do thu hồi *
              <textarea
                className={`${styles.textarea} ${touched && !reasonValid ? styles.textareaInvalid : ''}`}
                value={reason}
                aria-invalid={touched && !reasonValid ? true : undefined}
                onChange={(e) => {
                  setReason(e.target.value);
                  setTouched(true);
                }}
              />
              <span className={styles.counter}>
                {reason.length}/{REASON_MAX}
              </span>
            </label>
            {touched && !reasonValid && (
              <p className={styles.fieldError} role="alert">
                Lý do cần từ {REASON_MIN} đến {REASON_MAX} ký tự
              </p>
            )}
            <Alert kind="info">
              Thu hồi có hiệu lực tại các trạm trong ≤ 60 giây. Portal của nhân viên sẽ ngừng hiện mã.
            </Alert>
          </>
        ) : (
          employee.credentialStatus === 'ACTIVE' && (
            <Alert kind="warning">
              Mã QR hiện tại (cấp {formatDate(employee.issuedAt)}) sẽ bị thu hồi và một mã mới được cấp ngay.
            </Alert>
          )
        )}

        {error && <Alert kind="error">{error}</Alert>}
      </div>
    </Dialog>
  );
}
