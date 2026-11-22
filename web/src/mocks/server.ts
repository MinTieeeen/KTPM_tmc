// Server MSW cho Vitest (jsdom): fetch của Node cần URL tuyệt đối nên bọc lại để chấp nhận đường dẫn tương đối.
import { setupServer } from 'msw/node';
import { createCentralHandlers, createMockBackend } from './central.handlers';
import type { MockBackend } from './central.handlers';

export interface TestBackend {
  backend: MockBackend;
  /** Dùng `server.use(...)` để ghi đè handler (vd. giả lập mất mạng); gọi `reset()` sau mỗi test. */
  server: ReturnType<typeof setupServer>;
  start: () => void;
  stop: () => void;
}

export function createTestBackend(): TestBackend {
  const backend = createMockBackend();
  const server = setupServer(...createCentralHandlers(backend));
  let restore: (() => void) | undefined;

  return {
    backend,
    server,
    start() {
      server.listen({ onUnhandledRequest: 'error' });
      const patched = globalThis.fetch;
      globalThis.fetch = (input, init) =>
        patched(typeof input === 'string' && input.startsWith('/') ? new URL(input, location.href).href : input, init);
      restore = () => {
        globalThis.fetch = patched;
      };
    },
    stop() {
      restore?.();
      server.close();
    },
  };
}
