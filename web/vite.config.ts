/// <reference types="vitest" />
import { resolve } from 'node:path';
import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

/**
 * Một project, hai build target (đặc tả mục 10.1, Implementation view của báo cáo):
 *   vite build --mode gate     → kiosk.html              → gate-agent/src/main/resources/static
 *   vite build --mode central  → portal.html, admin.html → central/src/main/resources/static
 * Khi dev (vite), mọi trang chạy chung và gọi API qua proxy.
 */
const pages = {
  gate: { kiosk: 'kiosk.html' },
  central: { portal: 'portal.html', admin: 'admin.html' },
} as const;

const outDirs = {
  gate: '../gate-agent/src/main/resources/static',
  central: '../central/src/main/resources/static',
} as const;

type Target = keyof typeof pages;

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const gateUrl = env.VITE_GATE_URL ?? 'http://localhost:8081';
  const centralUrl = env.VITE_CENTRAL_URL ?? 'http://localhost:8080';

  const target: Target | undefined = mode === 'gate' || mode === 'central' ? mode : undefined;
  const input = Object.fromEntries(
    Object.entries(target ? pages[target] : { ...pages.gate, ...pages.central }).map(
      ([name, file]) => [name, resolve(__dirname, file)],
    ),
  );

  return {
    plugins: [react()],
    // public/ chỉ chứa mockServiceWorker.js của MSW → không đóng gói vào bản build cho backend
    publicDir: target ? false : 'public',
    resolve: {
      alias: { '@': resolve(__dirname, 'src') },
    },
    build: {
      outDir: target ? outDirs[target] : 'dist',
      emptyOutDir: true,
      rollupOptions: { input },
    },
    server: {
      port: 5173,
      proxy: {
        // Gate Agent (G1, G4)
        '/scan': gateUrl,
        '/status': gateUrl,
        '/admin/simulate-offline': gateUrl,
        // Central (C1–C3, C7)
        '/auth/': centralUrl,
        '/portal/': centralUrl,
        '/employees': centralUrl,
        '/stations': centralUrl,
      },
    },
    test: {
      environment: 'jsdom',
      include: ['src/**/*.test.{ts,tsx}'],
    },
  };
});
