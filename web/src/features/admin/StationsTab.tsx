// A-04 Tab Trạm – docs/web/screens/A-04-stations-tab.md (nhóm theo nhà máy, làm mới 5 s, giữ dữ liệu cũ khi lỗi).
import { useMemo } from 'react';
import type { StationView } from '@/shared/api/centralApi';
import { Alert } from '@/features/common/Alert';
import { Badge } from '@/features/common/Badge';
import { Button } from '@/features/common/Button';
import { factoryName, formatRelative, formatShortTime, formatTime } from '@/features/common/format';
import { Spinner } from '@/features/common/Spinner';
import { useNow } from '@/features/common/useNow';
import styles from './Admin.module.css';
import { useStations } from './useStations';

function TurnstileBadge({ status }: { status: StationView['turnstileStatus'] }) {
  if (status === 'UP') return <Badge color="green">UP</Badge>;
  if (status === 'DOWN') return <Badge color="red">DOWN</Badge>;
  if (status === 'CIRCUIT_OPEN') return <Badge color="orange">CIRCUIT_OPEN</Badge>;
  return <Badge color="gray">—</Badge>;
}

function syncText(gate: StationView, nowMs: number): string {
  if (!gate.lastHeartbeatAt) return 'chưa có heartbeat';
  const parts = [`Heartbeat cuối: ${formatRelative(gate.lastHeartbeatAt, nowMs)}`];
  if (gate.outboxDepth !== undefined) parts.push(`Outbox: ${gate.outboxDepth} bản ghi`);
  if (gate.lastSyncAt) parts.push(`đồng bộ cuối ${formatShortTime(gate.lastSyncAt)}`);
  return parts.join(' · ');
}

function groupByFactory(stations: StationView[]): Array<[string, StationView[]]> {
  const groups = new Map<string, StationView[]>();
  [...stations]
    .sort((a, b) => a.stationId.localeCompare(b.stationId))
    .forEach((s) => groups.set(s.factoryId, [...(groups.get(s.factoryId) ?? []), s]));
  return [...groups.entries()].sort((a, b) => a[0].localeCompare(b[0]));
}

export function StationsTab() {
  const query = useStations();
  const nowMs = useNow(1000);
  const groups = useMemo(() => (query.data ? groupByFactory(query.data) : []), [query.data]);

  if (!query.data) {
    return query.isError ? (
      <div className={styles.errorBox}>
        <Alert kind="error">Không tải được danh sách trạm.</Alert>
        <Button variant="secondary" onClick={() => void query.refetch()}>
          Thử lại
        </Button>
      </div>
    ) : (
      <Spinner />
    );
  }

  return (
    <section aria-label="Trạm">
      <p className={styles.refreshInfo}>
        Tự làm mới mỗi 5 giây · cập nhật lúc {formatTime(query.dataUpdatedAt)}
      </p>
      {query.isError && (
        <Alert kind="notice" className={styles.refreshInfo}>
          Không tải được dữ liệu – đang hiển thị dữ liệu lúc {formatTime(query.dataUpdatedAt)}
        </Alert>
      )}
      {groups.map(([factoryId, stations]) => {
        const gate = stations[0]!;
        return (
          <div key={factoryId} className={styles.group} data-testid={`group-${factoryId}`}>
            <h2 className={styles.groupHeader}>
              Nhà máy {factoryName(factoryId)} · {gate.gateId}
              {gate.online ? <Badge color="green">ONLINE</Badge> : <Badge color="red">OFFLINE</Badge>}
            </h2>
            <p className={`${styles.syncInfo} ${gate.online ? '' : styles.syncOffline}`}>{syncText(gate, nowMs)}</p>
            <div className={styles.tableWrap}>
              <table className={styles.table}>
                <thead>
                  <tr>
                    <th>Trạm</th>
                    <th>Chiều</th>
                    <th>Trạng thái trạm</th>
                    <th>Cổng xoay</th>
                  </tr>
                </thead>
                <tbody>
                  {stations.map((s) => (
                    <tr key={s.stationId}>
                      <td>{s.stationId}</td>
                      <td>{s.direction === 'IN' ? 'VÀO' : 'RA'}</td>
                      <td>{s.enabled ? 'Bật' : 'Tắt'}</td>
                      <td>
                        <TurnstileBadge status={s.turnstileStatus} />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        );
      })}
    </section>
  );
}
