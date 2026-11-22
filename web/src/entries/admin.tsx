// Entry của trang admin. Màn hình thật nằm ở src/features/admin (A-01…A-04) và features/auth (AU-01, AU-02).
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { QueryClientProvider } from '@tanstack/react-query';
import { AdminApp } from '@/features/admin/AdminApp';
import { AuthProvider } from '@/features/auth/AuthProvider';
import { enableMocksIfRequested } from '@/features/common/bootstrap';
import { createQueryClient } from '@/features/common/queryClient';
import { ToastProvider } from '@/features/common/Toast';
import '@/features/common/tokens.css';
import '@/features/common/global.css';

const queryClient = createQueryClient();

void enableMocksIfRequested().then(() => {
  createRoot(document.getElementById('root')!).render(
    <StrictMode>
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <ToastProvider>
            <AdminApp />
          </ToastProvider>
        </AuthProvider>
      </QueryClientProvider>
    </StrictMode>,
  );
});
