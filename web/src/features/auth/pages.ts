// Cấu hình theo trang: vai trò được phép, tiêu đề và tên hiển thị ở AU-01/AU-02.
import type { Role } from '@/shared/api/centralApi';

export type Page = 'portal' | 'admin';

interface PageConfig {
  role: Role;
  title: string;
  forbiddenName: string;
}

export const PAGES: Record<Page, PageConfig> = {
  portal: { role: 'EMPLOYEE', title: 'TMC Portal', forbiddenName: 'Portal nhân viên' },
  admin: { role: 'ADMIN', title: 'TMC Admin', forbiddenName: 'Quản trị' },
};
