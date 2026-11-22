// Trang Portal: guard EMPLOYEE → P-01 (có mã) hoặc P-02 (409); 401/403 do AuthGate xử lý.
import { asApiError } from '@/shared/api/apiError';
import { AuthGate } from '@/features/auth/AuthGate';
import { useAuth } from '@/features/auth/authContext';
import { NoQrNotice } from './NoQrNotice';
import { QrPage } from './QrPage';
import { useQrToken } from './useQrToken';

function QrScreen() {
  const { state } = useAuth();
  const query = useQrToken();
  if (state.status !== 'authenticated') return null;

  const error = asApiError(query.error);
  if (error?.status === 409) return <NoQrNotice code={error.code} checkedAtMs={query.errorUpdatedAt} />;

  return (
    <QrPage
      user={state.user}
      token={query.data}
      receivedAtMs={query.dataUpdatedAt}
      refreshFailed={query.isError}
    />
  );
}

export function PortalApp() {
  return (
    <AuthGate page="portal">
      <QrScreen />
    </AuthGate>
  );
}
