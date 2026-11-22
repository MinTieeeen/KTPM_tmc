import { cleanup, fireEvent, screen, waitFor, within } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it } from 'vitest';
import { authSession } from '@/shared/api/authSession';
import { createTestBackend } from '@/mocks/server';
import { loginAs, renderApp, resetSession } from '@/features/common/testUtils';
import { AdminApp } from './AdminApp';

const mock = createTestBackend();

beforeAll(() => mock.start());
afterAll(() => mock.stop());
beforeEach(() => {
  resetSession();
  window.location.hash = '';
  mock.backend.calls = {};
  mock.backend.delayMs = 0;
  mock.backend.gates.BD.online = true;
  mock.backend.gates.BD.outboxDepth = 0;
});
afterEach(() => {
  cleanup();
  mock.server.resetHandlers();
});

const problem = (status: number, code: string) =>
  HttpResponse.json({ type: 'about:blank', title: 'x', status, code }, { status });

function rowOf(code: string): HTMLElement {
  return screen.getByText(code).closest('tr')!;
}

async function openEmployees() {
  loginAs('admin');
  const utils = renderApp(<AdminApp />);
  await screen.findByText('E0001');
  return utils;
}

describe('A-01 Khung trang quản trị', () => {
  it('mở không hash → #/employees, tab Nhân viên được chọn; hiện admin (ADMIN)', async () => {
    await openEmployees();
    expect(window.location.hash).toBe('#/employees');
    expect(screen.getByRole('link', { name: 'Nhân viên' }).getAttribute('aria-current')).toBe('page');
    expect(screen.getByText('admin (ADMIN)')).toBeTruthy();
    expect(screen.getByRole('heading', { name: 'TMC Admin – Quản trị chấm công' })).toBeTruthy();
  });

  it('mở thẳng #/stations → tab Trạm được chọn', async () => {
    window.location.hash = '#/stations';
    loginAs('admin');
    renderApp(<AdminApp />);
    await screen.findByText('BD-IN-01');
    expect(screen.getByRole('link', { name: 'Trạm' }).getAttribute('aria-current')).toBe('page');
  });

  it('chuyển tab đổi hash', async () => {
    await openEmployees();
    fireEvent.click(screen.getByRole('link', { name: 'Trạm' }));
    await screen.findByText('BD-IN-01');
    expect(window.location.hash).toBe('#/stations');
  });

  it('token hết hạn khi đang ở tab Nhân viên → về AU-01 và xóa token', async () => {
    mock.server.use(http.get('/employees', () => problem(401, 'UNAUTHENTICATED')));
    loginAs('admin');
    renderApp(<AdminApp />);
    await screen.findByRole('heading', { name: 'TMC Admin' });
    expect(authSession.getToken()).toBeNull();
  });

  it('Đăng xuất → về AU-01', async () => {
    await openEmployees();
    fireEvent.click(screen.getByRole('button', { name: 'Đăng xuất' }));
    await screen.findByRole('heading', { name: 'TMC Admin' });
    expect(authSession.getToken()).toBeNull();
  });

  it('API trả 403 ở bất kỳ tab nào → AU-02', async () => {
    mock.server.use(http.get('/employees', () => problem(403, 'FORBIDDEN')));
    loginAs('admin');
    renderApp(<AdminApp />);
    await screen.findByRole('heading', { name: 'Không có quyền truy cập' });
  });
});

describe('A-02 Tab Nhân viên', () => {
  it('20 dòng; E0019 "Đã nghỉ" không có Cấp lại; E0020 REVOKED chỉ có Cấp lại', async () => {
    await openEmployees();
    expect(screen.getAllByRole('row')).toHaveLength(21);
    expect(screen.getByText('20 nhân viên')).toBeTruthy();

    const e19 = within(rowOf('E0019'));
    expect(e19.getByText('Đã nghỉ')).toBeTruthy();
    expect(e19.queryByRole('button', { name: 'Cấp lại' })).toBeNull();

    const e20 = within(rowOf('E0020'));
    expect(e20.getByText('REVOKED')).toBeTruthy();
    expect(e20.getByRole('button', { name: 'Cấp lại' })).toBeTruthy();
    expect(e20.queryByRole('button', { name: 'Thu hồi' })).toBeNull();

    const e1 = within(rowOf('E0001'));
    expect(e1.getByText('ACTIVE')).toBeTruthy();
    expect(e1.getByText('16/11/2026')).toBeTruthy();
    expect(e1.getByRole('button', { name: 'Thu hồi' })).toBeTruthy();
    expect(e1.getByRole('button', { name: 'Cấp lại' })).toBeTruthy();
  });

  it('gõ liên tục chỉ gửi một request sau khi ngừng gõ 300 ms', async () => {
    await openEmployees();
    expect(mock.backend.calls['GET /employees']).toBe(1);
    const input = screen.getByLabelText('Tìm nhân viên');
    for (const value of ['n', 'ng', 'ngu', 'nguy', 'nguyen']) {
      fireEvent.change(input, { target: { value } });
    }
    expect(mock.backend.calls['GET /employees']).toBe(1);
    await waitFor(() => expect(mock.backend.calls['GET /employees']).toBe(2));
    await screen.findByText('1 nhân viên');
    await new Promise((r) => setTimeout(r, 400));
    expect(mock.backend.calls['GET /employees']).toBe(2);
  });

  it('không khớp ai → hộp "Không có nhân viên nào khớp bộ lọc"', async () => {
    await openEmployees();
    fireEvent.change(screen.getByLabelText('Tìm nhân viên'), { target: { value: 'zzzz' } });
    await screen.findByText('Không có nhân viên nào khớp bộ lọc');
    expect(screen.queryByRole('table')).toBeNull();
  });

  it('lọc bộ phận: tùy chọn lấy từ danh sách chưa lọc, sắp theo tên', async () => {
    await openEmployees();
    const select = screen.getByLabelText('Lọc bộ phận') as HTMLSelectElement;
    expect([...select.options].map((o) => o.text)).toEqual([
      'Tất cả bộ phận',
      'Assembly',
      'Packaging',
      'Quality Control',
      'Warehouse',
    ]);
    fireEvent.change(select, { target: { value: 'LA-PKG' } });
    await screen.findByText('5 nhân viên');
    expect([...select.options]).toHaveLength(5);
  });

  it('lỗi tải → thông báo + Thử lại; Thử lại thành công → bảng hiện lại', async () => {
    mock.server.use(http.get('/employees', () => problem(500, 'INTERNAL_ERROR'), { once: true }));
    loginAs('admin');
    renderApp(<AdminApp />);
    await screen.findByText('Không tải được danh sách nhân viên.');
    expect(screen.queryByRole('table')).toBeNull();
    fireEvent.click(screen.getByRole('button', { name: 'Thử lại' }));
    await screen.findByText('E0001');
  });
});

function openDialog(code: string, buttonName: 'Thu hồi' | 'Cấp lại') {
  fireEvent.click(within(rowOf(code)).getByRole('button', { name: buttonName }));
  return screen.getByRole('dialog');
}

describe('A-03 Hộp thoại thao tác QR', () => {
  it('lý do 2 ký tự → nút Thu hồi bị khóa và có thông báo; 3 ký tự → mở khóa', async () => {
    await openEmployees();
    const dialog = within(openDialog('E0012', 'Thu hồi'));
    const confirm = dialog.getByRole('button', { name: 'Thu hồi' }) as HTMLButtonElement;
    expect(confirm.disabled).toBe(true);
    expect(dialog.getByText('Thu hồi có hiệu lực tại các trạm trong ≤ 60 giây. Portal của nhân viên sẽ ngừng hiện mã.')).toBeTruthy();
    fireEvent.change(dialog.getByLabelText(/Lý do thu hồi/), { target: { value: 'ab' } });
    expect(dialog.getByText('Lý do cần từ 3 đến 200 ký tự')).toBeTruthy();
    expect(dialog.getByText('2/200')).toBeTruthy();
    expect(confirm.disabled).toBe(true);
    fireEvent.change(dialog.getByLabelText(/Lý do thu hồi/), { target: { value: 'Mất điện thoại' } });
    expect(confirm.disabled).toBe(false);
    expect(dialog.queryByText('Lý do cần từ 3 đến 200 ký tự')).toBeNull();
  });

  it('thu hồi E0012 → dòng thành REVOKED, đóng hộp, toast, không tải lại cả bảng', async () => {
    await openEmployees();
    const dialog = within(openDialog('E0012', 'Thu hồi'));
    expect(dialog.getByText(/E0012 – Ly Thi Mai · Warehouse \(LA\)/)).toBeTruthy();
    fireEvent.change(dialog.getByLabelText(/Lý do thu hồi/), { target: { value: 'Mất điện thoại' } });
    fireEvent.click(dialog.getByRole('button', { name: 'Thu hồi' }));
    await screen.findByText('Đã thu hồi mã QR của E0012');
    expect(screen.queryByRole('dialog')).toBeNull();
    const row = within(rowOf('E0012'));
    expect(row.getByText('REVOKED')).toBeTruthy();
    expect(row.queryByRole('button', { name: 'Thu hồi' })).toBeNull();
    expect(mock.backend.calls['GET /employees']).toBe(1);
  });

  it('cấp lại E0020 → dòng thành ACTIVE với ngày cấp mới; không có cảnh báo vì chưa có mã ACTIVE', async () => {
    await openEmployees();
    const dialog = within(openDialog('E0020', 'Cấp lại'));
    expect(dialog.queryByText(/sẽ bị thu hồi và một mã mới được cấp ngay/)).toBeNull();
    fireEvent.click(dialog.getByRole('button', { name: 'Cấp lại' }));
    await screen.findByText('Đã cấp mã QR mới cho E0020');
    const row = within(rowOf('E0020'));
    expect(row.getByText('ACTIVE')).toBeTruthy();
    expect(row.queryByText('16/11/2026')).toBeNull();
  });

  it('cấp lại khi đang ACTIVE → cảnh báo mã hiện tại sẽ bị thu hồi', async () => {
    await openEmployees();
    const dialog = within(openDialog('E0001', 'Cấp lại'));
    expect(
      dialog.getByText('Mã QR hiện tại (cấp 16/11/2026) sẽ bị thu hồi và một mã mới được cấp ngay.'),
    ).toBeTruthy();
  });

  it('Hủy và Esc đóng hộp mà không gọi API', async () => {
    await openEmployees();
    fireEvent.click(within(openDialog('E0001', 'Cấp lại')).getByRole('button', { name: 'Hủy' }));
    expect(screen.queryByRole('dialog')).toBeNull();
    openDialog('E0001', 'Thu hồi');
    fireEvent.keyDown(document, { key: 'Escape' });
    expect(screen.queryByRole('dialog')).toBeNull();
    expect(mock.backend.calls['POST /employees/:code/qr/issue']).toBeUndefined();
    expect(mock.backend.calls['POST /employees/:code/qr/revoke']).toBeUndefined();
  });

  it('đang gửi: Esc và nút Hủy không đóng được hộp', async () => {
    await openEmployees();
    mock.backend.delayMs = 150;
    const dialog = within(openDialog('E0020', 'Cấp lại'));
    fireEvent.click(dialog.getByRole('button', { name: 'Cấp lại' }));
    fireEvent.keyDown(document, { key: 'Escape' });
    expect(screen.getByRole('dialog')).toBeTruthy();
    expect((dialog.getByRole('button', { name: 'Hủy' }) as HTMLButtonElement).disabled).toBe(true);
    await screen.findByText('Đã cấp mã QR mới cho E0020');
    expect(screen.queryByRole('dialog')).toBeNull();
  });

  it('CONCURRENT_UPDATE → đóng hộp và tải lại danh sách', async () => {
    await openEmployees();
    mock.server.use(http.post('/employees/:code/qr/issue', () => problem(409, 'CONCURRENT_UPDATE')));
    const dialog = within(openDialog('E0001', 'Cấp lại'));
    fireEvent.click(dialog.getByRole('button', { name: 'Cấp lại' }));
    await screen.findByText('Dữ liệu vừa được người khác thay đổi, danh sách sẽ được tải lại');
    expect(screen.queryByRole('dialog')).toBeNull();
    await waitFor(() => expect(mock.backend.calls['GET /employees']).toBe(2));
  });

  it('NO_ACTIVE_CREDENTIAL khi thu hồi → báo lỗi trong hộp, hộp vẫn mở', async () => {
    await openEmployees();
    mock.server.use(http.post('/employees/:code/qr/revoke', () => problem(409, 'NO_ACTIVE_CREDENTIAL')));
    const dialog = within(openDialog('E0001', 'Thu hồi'));
    fireEvent.change(dialog.getByLabelText(/Lý do thu hồi/), { target: { value: 'Mất thẻ' } });
    fireEvent.click(dialog.getByRole('button', { name: 'Thu hồi' }));
    await dialog.findByText('Nhân viên không có mã QR đang hoạt động');
    expect(screen.getByRole('dialog')).toBeTruthy();
  });
});

describe('A-04 Tab Trạm', () => {
  async function openStations() {
    window.location.hash = '#/stations';
    loginAs('admin');
    const utils = renderApp(<AdminApp />);
    await screen.findByText('BD-IN-01');
    return utils;
  }

  it('nhóm theo nhà máy, mỗi nhóm có tiêu đề, trạng thái và bảng trạm', async () => {
    await openStations();
    const bd = within(screen.getByTestId('group-BD'));
    expect(bd.getByRole('heading', { name: /Nhà máy Bình Dương · gate-bd/ })).toBeTruthy();
    expect(bd.getByText('ONLINE')).toBeTruthy();
    expect(bd.getByText('BD-IN-01')).toBeTruthy();
    expect(bd.getByText('BD-OUT-01')).toBeTruthy();
    expect(bd.getByText('VÀO')).toBeTruthy();
    expect(bd.getByText('RA')).toBeTruthy();
    const la = within(screen.getByTestId('group-LA'));
    expect(la.getByRole('heading', { name: /Nhà máy Long An · gate-la/ })).toBeTruthy();
    expect(screen.getByText(/Tự làm mới mỗi 5 giây · cập nhật lúc \d{2}:\d{2}:\d{2}/)).toBeTruthy();
  });

  it('gate offline → nhóm đó OFFLINE và hiện outbox, nhóm kia vẫn ONLINE', async () => {
    mock.backend.gates.BD.online = false;
    mock.backend.gates.BD.outboxDepth = 37;
    await openStations();
    const bd = within(screen.getByTestId('group-BD'));
    expect(bd.getByText('OFFLINE')).toBeTruthy();
    expect(bd.getByText(/Heartbeat cuối: 2 phút trước · Outbox: 37 bản ghi/)).toBeTruthy();
    expect(within(screen.getByTestId('group-LA')).getByText('ONLINE')).toBeTruthy();
  });

  it('lỗi làm mới → giữ dữ liệu cũ và hiện cảnh báo dữ liệu cũ', async () => {
    const { queryClient } = await openStations();
    mock.server.use(http.get('/stations', () => problem(500, 'INTERNAL_ERROR')));
    await queryClient.refetchQueries({ queryKey: ['stations'] });
    await screen.findByText(/Không tải được dữ liệu – đang hiển thị dữ liệu lúc \d{2}:\d{2}:\d{2}/);
    expect(screen.getByText('BD-IN-01')).toBeTruthy();
  });

  it('lỗi ngay lần tải đầu → thông báo + Thử lại', async () => {
    mock.server.use(http.get('/stations', () => problem(500, 'INTERNAL_ERROR'), { once: true }));
    window.location.hash = '#/stations';
    loginAs('admin');
    renderApp(<AdminApp />);
    await screen.findByText('Không tải được danh sách trạm.');
    fireEvent.click(screen.getByRole('button', { name: 'Thử lại' }));
    await screen.findByText('BD-IN-01');
  });

  it('chưa có heartbeat và trạng thái cổng xoay các loại', async () => {
    mock.server.use(
      http.get('/stations', () =>
        HttpResponse.json([
          { stationId: 'BD-IN-01', factoryId: 'BD', direction: 'IN', enabled: true, gateId: 'gate-bd', online: false },
          { stationId: 'BD-OUT-01', factoryId: 'BD', direction: 'OUT', enabled: false, gateId: 'gate-bd', online: false, turnstileStatus: 'CIRCUIT_OPEN' },
          { stationId: 'LA-IN-01', factoryId: 'LA', direction: 'IN', enabled: true, gateId: 'gate-la', online: true, lastHeartbeatAt: new Date().toISOString(), outboxDepth: 0, turnstileStatus: 'DOWN' },
        ]),
      ),
    );
    await openStations();
    const bd = within(screen.getByTestId('group-BD'));
    expect(bd.getByText('chưa có heartbeat')).toBeTruthy();
    expect(bd.getByText('—')).toBeTruthy();
    expect(bd.getByText('CIRCUIT_OPEN')).toBeTruthy();
    expect(bd.getByText('Tắt')).toBeTruthy();
    expect(within(screen.getByTestId('group-LA')).getByText('DOWN')).toBeTruthy();
  });
});
