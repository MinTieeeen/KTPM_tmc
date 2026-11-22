// Một dòng của bảng A-02: badge trạng thái nhân viên/QR và nút Thu hồi / Cấp lại theo luật của đặc tả.
import { RefreshCw, ShieldOff } from 'lucide-react';
import type { EmployeeView } from '@/shared/api/centralApi';
import { Badge } from '@/features/common/Badge';
import { Button } from '@/features/common/Button';
import { formatDate } from '@/features/common/format';
import styles from './Admin.module.css';

export type QrAction = 'revoke' | 'issue';

interface Props {
  employee: EmployeeView;
  onAction: (action: QrAction, employee: EmployeeView) => void;
}

function CredentialBadge({ status }: { status: EmployeeView['credentialStatus'] }) {
  if (status === 'ACTIVE') return <Badge color="green">ACTIVE</Badge>;
  if (status === 'REVOKED') return <Badge color="red">REVOKED</Badge>;
  return <Badge color="gray">Chưa cấp</Badge>;
}

export function EmployeeRow({ employee, onAction }: Props) {
  return (
    <tr>
      <td className={styles.code}>{employee.code}</td>
      <td>{employee.fullName}</td>
      <td>{employee.departmentName}</td>
      <td>{employee.factoryId}</td>
      <td>{employee.active ? <Badge color="green">Đang làm</Badge> : <Badge color="gray">Đã nghỉ</Badge>}</td>
      <td>
        <CredentialBadge status={employee.credentialStatus} />
      </td>
      <td className={styles.muted}>{formatDate(employee.issuedAt)}</td>
      <td>
        <div className={styles.actions}>
          {employee.credentialStatus === 'ACTIVE' && (
            <Button variant="dangerSoft" small icon={ShieldOff} onClick={() => onAction('revoke', employee)}>
              Thu hồi
            </Button>
          )}
          {employee.active && (
            <Button variant="secondary" small icon={RefreshCw} onClick={() => onAction('issue', employee)}>
              Cấp lại
            </Button>
          )}
        </div>
      </td>
    </tr>
  );
}
