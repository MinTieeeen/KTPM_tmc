// Khởi động MSW khi chạy `npm run dev` với VITE_USE_MOCKS=1; điều kiện DEV loại code mock khỏi bản build.
export async function enableMocksIfRequested(): Promise<void> {
  const flag = import.meta.env.VITE_USE_MOCKS;
  if (import.meta.env.DEV && (flag === '1' || flag === 'true')) {
    const { worker } = await import('@/mocks/browser');
    await worker.start({ onUnhandledRequest: 'bypass' });
  }
}
