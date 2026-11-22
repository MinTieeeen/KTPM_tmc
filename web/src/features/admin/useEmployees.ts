// Danh sách nhân viên (GET /employees) và thay một dòng trong mọi bản cache sau khi thu hồi/cấp lại.
import { keepPreviousData, useQuery, useQueryClient } from '@tanstack/react-query';
import { useCallback } from 'react';
import { centralApi } from '@/shared/api/centralApi';
import type { EmployeeView } from '@/shared/api/centralApi';

export function useEmployees(department: string, q: string) {
  return useQuery({
    queryKey: ['employees', department, q],
    queryFn: () => centralApi.listEmployees(department || undefined, q || undefined),
    placeholderData: keepPreviousData,
  });
}

export function useReplaceEmployee() {
  const queryClient = useQueryClient();
  return useCallback(
    (updated: EmployeeView) => {
      queryClient.setQueriesData<EmployeeView[]>({ queryKey: ['employees'] }, (old) =>
        old?.map((e) => (e.code === updated.code ? updated : e)),
      );
    },
    [queryClient],
  );
}

export function useReloadEmployees() {
  const queryClient = useQueryClient();
  return useCallback(() => queryClient.invalidateQueries({ queryKey: ['employees'] }), [queryClient]);
}
