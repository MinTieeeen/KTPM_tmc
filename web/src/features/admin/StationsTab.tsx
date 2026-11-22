// A-04 Tab Trạm – docs/web/screens/A-04-stations-tab.md (nhóm theo nhà máy, làm mới 5 s, giữ dữ liệu cũ khi lỗi).
import { Activity, Factory, RefreshCw, WifiOff } from 'lucide-react';
import { useMemo } from 'react';
import type { StationView } from '@/shared/api/centralApi';
import { Alert } from '@/features/common/Alert';
import { Badge } from '@/features/common/Badge';
import { Button } from '@/features/common/Button';
import { factoryName, formatRelative, formatShortTime, formatTime } from '@/features/common/format';
import { Skeleton } from '@/features/common/Skeleton';
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
        <Button variant="secondary" icon={RefreshCw} onClick={() => void query.refetch()}>
          Thử lại
        </Button>
      </div>
    ) : (
      <div className={styles.groups} aria-busy="true" role="status" aria-label="Đang tải danh sách trạm">
        {[0, 1].map((i) => (
          <div key={i} className={styles.group}>
            <div className={styles.groupHead}>
              <Skeleton width={38} height={38} />
              <Skeleton width={220} height={18} />
            </div>
          </div>
        ))}
      </div>
    );
  }

  return (
    <section aria-label="Trạm">
      <p className={styles.refreshInfo}>
        <RefreshCw size={13} aria-hidden="true" />
        <span>Tự làm mới mỗi 5 giây · cập nhật lúc {formatTime(query.dataUpdatedAt)}</span>
      </p>
      {query.isError && (
        <Alert kind="notice" className={styles.staleAlert}>
          Không tải được dữ liệu – đang hiển thị dữ liệu lúc {formatTime(query.dataUpdatedAt)}
        </Alert>
      )}
      <div className={styles.groups}>
        {groups.map(([factoryId, stations]) => {
          const gate = stations[0]!;
          const SyncIcon = gate.online ? Activity : WifiOff;
          return (
            <div key={factoryId} className={styles.group} data-testid={`group-${factoryId}`}>
              <div className={styles.groupHead}>
                <span className={styles.factoryIcon}>
                  <Factory size={20} aria-hidden="true" />
                </span>
                <h2 className={styles.groupTitle}>
                  Nhà máy {factoryName(factoryId)} · {gate.gateId}
                </h2>
                {gate.online ? (
                  <Badge color="green" pulse>
                    ONLINE
                  </Badge>
                ) : (
                  <Badge color="red">OFFLINE</Badge>
                )}
                <p className={`${styles.syncInfo} ${gate.online ? '' : styles.syncOffline}`}>
                  <SyncIcon size={14} aria-hidden="true" />
                  <span>{syncText(gate, nowMs)}</span>
                </p>
              </div>
              <div className={styles.groupTable}>
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
                        <td className={styles.code}>{s.stationId}</td>
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
      </div>
    </section>
  );
}
