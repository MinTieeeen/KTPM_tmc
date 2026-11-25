// Handler MSW giả lập Gate Agent theo docs/api/gate-agent.md
import { http, HttpResponse, delay } from 'msw';
import type { GateStatus, ScanResult } from '@/shared/api/gateApi';
import { INITIAL_GATE_STATUS } from './gate.fixtures';

export interface MockGateState {
  status: GateStatus;
  calls: Record<string, number>;
  delayMs: number;
}

export function createMockGate(initialStatus?: Partial<GateStatus>): MockGateState {
  return {
    status: {
      ...INITIAL_GATE_STATUS,
      ...initialStatus,
    },
    calls: {},
    delayMs: 0,
  };
}

let eventSeq = 1;

export function createGateHandlers(state: MockGateState) {
  const track = async (path: string) => {
    state.calls[path] = (state.calls[path] ?? 0) + 1;
    if (state.delayMs > 0) {
      await delay(state.delayMs);
    }
  };

  return [
    // GET /status – trạng thái kết nối, trạm, outbox
    http.get('/status', async () => {
      await track('/status');
      return HttpResponse.json(state.status);
    }),

    // POST /scan – quét mã QR
    http.post('/scan', async ({ request }) => {
      await track('/scan');
      const body = (await request.json().catch(() => ({}))) as {
        stationId?: string;
        raw?: string;
      };

      const { stationId = '', raw = '' } = body;
      if (!stationId || !raw) {
        return HttpResponse.json(
          {
            type: 'about:blank',
            title: 'Bad Request',
            status: 400,
            detail: 'Request validation failed',
            code: 'VALIDATION_ERROR',
          },
          { status: 400 },
        );
      }

      if (raw.includes('TIMEOUT')) {
        await delay(2500);
      }

      const direction: 'IN' | 'OUT' = stationId.includes('-OUT-') ? 'OUT' : 'IN';
      const eventId = `0b9d6c1e-${String(eventSeq++).padStart(4, '0')}-4a52-9a0e-7a6c2b1d9e01`;

      // Kiểm tra trạm
      if (!state.status.turnstiles[stationId]) {
        const res: ScanResult = {
          result: 'REJECTED',
          reason: 'UNKNOWN_STATION',
          elapsedMs: 25,
          eventId,
        };
        return HttpResponse.json(res);
      }

      // Giả lập offline quá ngưỡng chiều vào
      if (state.status.offlineThresholdExceeded && direction === 'IN') {
        const res: ScanResult = {
          result: 'REJECTED',
          reason: 'OFFLINE_THRESHOLD_EXCEEDED',
          direction,
          elapsedMs: 30,
          eventId,
        };
        return HttpResponse.json(res);
      }

      // Các trường hợp từ chối theo nội dung mã giả lập
      if (raw.includes('EXPIRED')) {
        const res: ScanResult = {
          result: 'REJECTED',
          reason: 'EXPIRED',
          direction,
          elapsedMs: 40,
          eventId,
        };
        return HttpResponse.json(res);
      }

      if (raw.includes('REUSED')) {
        const res: ScanResult = {
          result: 'REJECTED',
          reason: 'NONCE_REUSED',
          direction,
          elapsedMs: 45,
          eventId,
        };
        return HttpResponse.json(res);
      }

      if (raw.includes('REVOKED')) {
        const res: ScanResult = {
          result: 'REJECTED',
          reason: 'CREDENTIAL_REVOKED',
          direction,
          elapsedMs: 35,
          eventId,
        };
        return HttpResponse.json(res);
      }

      if (raw.includes('ANTI_PASSBACK')) {
        const res: ScanResult = {
          result: 'REJECTED',
          reason: 'ANTI_PASSBACK',
          direction,
          elapsedMs: 50,
          eventId,
        };
        return HttpResponse.json(res);
      }

      if (raw.includes('GATE_FAILED') || raw.includes('GATE_TIMEOUT')) {
        const res: ScanResult = {
          result: 'GATE_FAILED',
          reason: 'GATE_TIMEOUT',
          direction,
          employeeCode: 'E0012',
          elapsedMs: 850,
          eventId,
        };
        return HttpResponse.json(res);
      }

      // Khi offline, mỗi lượt quét thành công làm tăng outboxDepth
      if (!state.status.online) {
        state.status.outboxDepth += 1;
      }

      // Thành công mặc định
      const res: ScanResult = {
        result: 'ACCEPTED',
        direction,
        employeeCode: 'E0001',
        elapsedMs: 320,
        eventId,
      };
      return HttpResponse.json(res);
    }),

    // POST /admin/simulate-offline – bật/tắt giả lập mất kết nối WAN
    http.post('/admin/simulate-offline', async ({ request }) => {
      await track('/admin/simulate-offline');
      const body = (await request.json().catch(() => ({}))) as { offline?: boolean };
      const offline = Boolean(body.offline);
      state.status.online = !offline;
      if (!offline) {
        state.status.outboxDepth = 0;
        state.status.offlineThresholdExceeded = false;
      }
      return new HttpResponse(null, { status: 204 });
    }),
  ];
}
