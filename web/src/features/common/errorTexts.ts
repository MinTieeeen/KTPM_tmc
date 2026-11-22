// Bảng câu tiếng Việt theo `code` của API (docs/api/central.md); rẽ nhánh theo code/status, không theo detail.
import type { ApiError } from '@/shared/api/apiError';

export const TEXT_UNAVAILABLE = 'Không kết nối được máy chủ, thử lại';
export const TEXT_GENERIC = 'Có lỗi xảy ra, vui lòng thử lại';
export const TEXT_INVALID_CREDENTIALS = 'Sai thông tin đăng nhập hoặc tài khoản bị khóa';
export const TEXT_QR_REFRESH_FAILED = 'Không lấy được mã QR, kiểm tra kết nối';
export const TEXT_QR_REFRESH_WARNING = 'Không lấy được mã QR mới – kiểm tra kết nối';

export function loginErrorText(err: ApiError): string {
  if (err.code === 'INVALID_CREDENTIALS') return TEXT_INVALID_CREDENTIALS;
  return err.isUnavailable ? TEXT_UNAVAILABLE : TEXT_GENERIC;
}

export interface QrBlockedText {
  heading: string;
  hint: string;
}

const QR_BLOCKED: Record<string, QrBlockedText> = {
  CREDENTIAL_REVOKED: {
    heading: 'Mã QR đã bị thu hồi',
    hint: 'Liên hệ phòng Nhân sự (HR) để được cấp lại mã.',
  },
  NO_ACTIVE_CREDENTIAL: {
    heading: 'Bạn chưa được cấp mã QR',
    hint: 'Liên hệ phòng Nhân sự (HR) để được cấp mã.',
  },
  EMPLOYEE_INACTIVE: {
    heading: 'Hồ sơ nhân viên không còn hiệu lực',
    hint: 'Liên hệ phòng Nhân sự (HR) nếu bạn cho rằng đây là nhầm lẫn.',
  },
};

/** Câu cho P-02; code lạ rơi về nhóm "chưa được cấp mã". */
export function qrBlockedText(code: string): QrBlockedText {
  return QR_BLOCKED[code] ?? QR_BLOCKED.NO_ACTIVE_CREDENTIAL!;
}

const ADMIN_TEXTS: Record<string, string> = {
  NO_ACTIVE_CREDENTIAL: 'Nhân viên không có mã QR đang hoạt động',
  EMPLOYEE_INACTIVE: 'Không thể cấp mã cho nhân viên đã nghỉ việc',
  EMPLOYEE_NOT_FOUND: 'Không tìm thấy nhân viên, danh sách sẽ được tải lại',
  CONCURRENT_UPDATE: 'Dữ liệu vừa được người khác thay đổi, danh sách sẽ được tải lại',
};

export function adminErrorText(err: ApiError): string {
  const text = ADMIN_TEXTS[err.code];
  if (text) return text;
  return err.isUnavailable ? TEXT_UNAVAILABLE : TEXT_GENERIC;
}

/** Lỗi nghiệp vụ buộc A-02 tải lại danh sách (A-03, hành động A03-A4). */
export function shouldReloadEmployees(err: ApiError): boolean {
  return err.code === 'CONCURRENT_UPDATE' || err.code === 'EMPLOYEE_NOT_FOUND';
}
