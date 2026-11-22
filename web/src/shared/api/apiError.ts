// Lỗi API thống nhất (Problem Details RFC 9457 + lỗi mạng); client rẽ nhánh theo `code`, không theo `detail`.
export const NETWORK_ERROR = 'NETWORK_ERROR';
export const UNKNOWN_ERROR = 'UNKNOWN_ERROR';

export class ApiError extends Error {
  readonly status: number;
  readonly code: string;

  constructor(status: number, code: string, detail?: string) {
    super(detail ?? code);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
  }

  get isNetwork(): boolean {
    return this.code === NETWORK_ERROR;
  }

  /** Lỗi máy chủ hoặc lỗi mạng: hiển thị câu "không kết nối được máy chủ". */
  get isUnavailable(): boolean {
    return this.isNetwork || this.status >= 500;
  }
}

export async function toApiError(res: Response): Promise<ApiError> {
  try {
    const body = (await res.json()) as { code?: unknown; detail?: unknown };
    const code = typeof body.code === 'string' ? body.code : UNKNOWN_ERROR;
    const detail = typeof body.detail === 'string' ? body.detail : undefined;
    return new ApiError(res.status, code, detail);
  } catch {
    return new ApiError(res.status, UNKNOWN_ERROR);
  }
}

export function networkError(): ApiError {
  return new ApiError(0, NETWORK_ERROR);
}

export function asApiError(err: unknown): ApiError | undefined {
  return err instanceof ApiError ? err : undefined;
}
