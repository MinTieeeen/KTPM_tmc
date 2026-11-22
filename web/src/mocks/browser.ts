// Worker MSW cho `npm run dev` với VITE_USE_MOCKS=1 (không đóng gói vào bản build).
import { setupWorker } from 'msw/browser';
import { createCentralHandlers, createMockBackend } from './central.handlers';

export const worker = setupWorker(...createCentralHandlers(createMockBackend()));
