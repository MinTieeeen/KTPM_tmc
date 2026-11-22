// QueryClient dùng chung: không tự thử lại (polling theo chu kỳ đã thay vai trò thử lại), giữ dữ liệu cũ khi lỗi.
import { QueryClient } from '@tanstack/react-query';

export function createQueryClient(): QueryClient {
  return new QueryClient({
    defaultOptions: {
      queries: { retry: false, refetchOnWindowFocus: true },
      mutations: { retry: false },
    },
  });
}
