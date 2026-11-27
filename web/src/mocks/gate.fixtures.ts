// Dữ liệu mẫu giả lập cho Gate Agent (G1, G4)
import type { GateStatus } from '@/shared/api/gateApi';

export const DEFAULT_STATIONS: Record<string, string> = {
  'BD-IN-01': 'UP',
  'BD-OUT-01': 'UP',
};

export const INITIAL_GATE_STATUS: GateStatus = {
  gateId: 'gate-bd',
  online: true,
  outboxDepth: 0,
  lastSyncAt: new Date().toISOString(),
  offlineThresholdExceeded: false,
  pendingReconcile: 0,
  turnstiles: DEFAULT_STATIONS,
};
