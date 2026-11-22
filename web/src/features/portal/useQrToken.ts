// Lấy mã QR động: làm mới theo refreshAfterSec và khi tab hiện lại; lỗi 409 hỏi lại mỗi 30 giây.
import { useQuery } from '@tanstack/react-query';
import { centralApi } from '@/shared/api/centralApi';

const RETRY_MS = 30_000;

export function useQrToken() {
  return useQuery({
    queryKey: ['qr-token'],
    queryFn: () => centralApi.getQrToken(),
    refetchOnWindowFocus: true,
    refetchInterval: (query) =>
      query.state.error || !query.state.data ? RETRY_MS : query.state.data.refreshAfterSec * 1000,
  });
}
