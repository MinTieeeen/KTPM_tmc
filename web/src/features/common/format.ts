// Định dạng ngày giờ theo giờ Việt Nam và thời gian tương đối; tên nhà máy hiển thị.
const TIME_ZONE = 'Asia/Ho_Chi_Minh';

const dateFormat = new Intl.DateTimeFormat('en-GB', {
  timeZone: TIME_ZONE,
  day: '2-digit',
  month: '2-digit',
  year: 'numeric',
});

const timeFormat = new Intl.DateTimeFormat('en-GB', {
  timeZone: TIME_ZONE,
  hour: '2-digit',
  minute: '2-digit',
  second: '2-digit',
  hourCycle: 'h23',
});

const shortTimeFormat = new Intl.DateTimeFormat('en-GB', {
  timeZone: TIME_ZONE,
  hour: '2-digit',
  minute: '2-digit',
  hourCycle: 'h23',
});

/** dd/MM/yyyy theo giờ VN; chuỗi rỗng khi chưa có giá trị. */
export function formatDate(iso: string | undefined): string {
  return iso ? dateFormat.format(new Date(iso)) : '';
}

/** HH:mm:ss theo giờ VN. */
export function formatTime(date: Date | number): string {
  return timeFormat.format(date);
}

/** HH:mm theo giờ VN. */
export function formatShortTime(iso: string): string {
  return shortTimeFormat.format(new Date(iso));
}

/** "3 giây trước", "2 phút trước", "1 giờ trước" (không âm). */
export function formatRelative(iso: string, nowMs: number): string {
  const seconds = Math.max(0, Math.floor((nowMs - new Date(iso).getTime()) / 1000));
  if (seconds < 60) return `${seconds} giây trước`;
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return `${minutes} phút trước`;
  return `${Math.floor(minutes / 60)} giờ trước`;
}

const FACTORY_NAMES: Record<string, string> = { BD: 'Bình Dương', LA: 'Long An' };

export function factoryName(factoryId: string): string {
  return FACTORY_NAMES[factoryId] ?? factoryId;
}
