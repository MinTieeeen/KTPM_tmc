// Chặn theo vai trò của trang: sai vai trò hoặc API đã trả 403 → AU-02; con chỉ mount khi đúng quyền.
import type { ReactNode } from 'react';
import type { CurrentUser } from '@/shared/api/centralApi';
import { Forbidden } from './Forbidden';
import { PAGES } from './pages';
import type { Page } from './pages';

interface Props {
  page: Page;
  user: CurrentUser;
  forbidden: boolean;
  children: ReactNode;
}

export function RequireRole({ page, user, forbidden, children }: Props) {
  if (forbidden || user.role !== PAGES[page].role) return <Forbidden page={page} user={user} />;
  return <>{children}</>;
}
