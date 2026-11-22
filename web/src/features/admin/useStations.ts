// Danh sách trạm (GET /stations) tự làm mới mỗi 5 giây, chỉ khi tab trình duyệt đang hiện.
import { useQuery } from '@tanstack/react-query';
import { centralApi } from '@/shared/api/centralApi';

const REFRESH_MS = 5000;

export function useStations() {
  return useQuery({
    queryKey: ['stations'],
    queryFn: () => centralApi.listStations(),
    refetchInterval: REFRESH_MS,
    refetchIntervalInBackground: false,
  });
}
