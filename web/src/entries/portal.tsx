// Entry của trang portal. Màn hình thật nằm ở src/features/portal (P-01, P-02) và features/auth (AU-01, AU-02).
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from '@/features/auth/AuthProvider';
import { enableMocksIfRequested } from '@/features/common/bootstrap';
import { createQueryClient } from '@/features/common/queryClient';
import { ToastProvider } from '@/features/common/Toast';
import { PortalApp } from '@/features/portal/PortalApp';
import '@/features/common/tokens.css';
import '@/features/common/global.css';

const queryClient = createQueryClient();

void enableMocksIfRequested().then(() => {
  createRoot(document.getElementById('root')!).render(
    <StrictMode>
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <ToastProvider>
            <PortalApp />
          </ToastProvider>
        </AuthProvider>
      </QueryClientProvider>
    </StrictMode>,
  );
});
