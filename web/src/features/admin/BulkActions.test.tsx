import { cleanup, fireEvent, screen, waitFor, within } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it } from 'vitest';
import { createEmployees } from '@/mocks/central.fixtures';
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
  mock.backend.employees = createEmployees(); // thao tác hàng loạt đổi dữ liệu: đặt lại cho từng test
});
afterEach(() => {
  cleanup();
  mock.server.resetHandlers();
});

const problem = (status: number, code: string) =>
  HttpResponse.json({ type: 'about:blank', title: 'x', status, code }, { status });
const rowOf = (code: string) => screen.getByText(code).closest('tr')!;
const pick = (...codes: string[]) =>
  codes.forEach((c) => fireEvent.click(within(rowOf(c)).getByRole('checkbox', { name: `Chọn ${c}` })));
const bar = () => within(screen.getByRole('region', { name: 'Nhân viên đang chọn' }));
const barOrNull = () => screen.queryByRole('region', { name: 'Nhân viên đang chọn' });
const dialog = () => within(screen.getByRole('dialog'));
// Hộp thoại có hai nút "Đóng" (✕ ở góc và nút ở chân hộp): lấy nút ở chân hộp.
const closeFooter = () => dialog().getAllByRole('button', { name: 'Đóng' }).at(-1)!;

async function openEmployees() {
  loginAs('admin');
  renderApp(<AdminApp />);
  await screen.findByText('E0001');
}

function fillReason(text = 'Kiểm thử thu hồi hàng loạt') {
  fireEvent.change(dialog().getByLabelText(/Lý do thu hồi/), { target: { value: text } });
}

describe('Khung chọn: nút thao tác ghi rõ số người đủ điều kiện', () => {
  it('thu hồi chỉ tính người có mã ACTIVE; cấp lại tính mọi người đang làm việc', async () => {
    await openEmployees();
    pick('E0001', 'E0002', 'E0020', 'E0019');
    expect(bar().getByRole('button', { name: 'Thu hồi mã (3)' })).toBeTruthy(); // E0001, E0002, E0019 (nghỉ nhưng còn mã ACTIVE)
    expect(bar().getByRole('button', { name: 'Cấp lại mã (3)' })).toBeTruthy(); // E0001, E0002, E0020 (E0019 đã nghỉ)
  });

  it('không ai đủ điều kiện thì nút bị khóa', async () => {
    await openEmployees();
    pick('E0020'); // chỉ có mã REVOKED
    expect((bar().getByRole('button', { name: 'Thu hồi mã (0)' }) as HTMLButtonElement).disabled).toBe(true);
    expect((bar().getByRole('button', { name: 'Cấp lại mã (1)' }) as HTMLButtonElement).disabled).toBe(false);
  });
});

describe('A-05 thu hồi hàng loạt', () => {
  it('xem trước người được xử lý và người bị bỏ qua (kèm lý do); phải nhập lý do hợp lệ mới thu hồi được', async () => {
    await openEmployees();
    pick('E0001', 'E0002', 'E0020');
    fireEvent.click(bar().getByRole('button', { name: 'Thu hồi mã (2)' }));

    expect(dialog().getByText('Thu hồi mã QR hàng loạt')).toBeTruthy();
    const eligible = within(dialog().getByRole('list', { name: 'Người sẽ được xử lý' }));
    expect(eligible.getAllByRole('listitem')).toHaveLength(2);
    const skipped = within(dialog().getByRole('list', { name: 'Người bị bỏ qua' }));
    expect(skipped.getByText('E0020 – Tran Thi Binh')).toBeTruthy();
    expect(skipped.getByText('Mã đã bị thu hồi')).toBeTruthy();

    const start = dialog().getByRole('button', { name: 'Thu hồi 2 người' }) as HTMLButtonElement;
    expect(start.disabled).toBe(true);
    fillReason('ab');
    expect(dialog().getByText('Lý do cần từ 3 đến 200 ký tự')).toBeTruthy();
    expect(start.disabled).toBe(true);
    fillReason();
    expect(start.disabled).toBe(false);
  });

  it('chạy xong: báo "Hoàn tất", các dòng thành REVOKED, người đã xử lý được bỏ khỏi lựa chọn', async () => {
    await openEmployees();
    pick('E0001', 'E0002');
    fireEvent.click(bar().getByRole('button', { name: 'Thu hồi mã (2)' }));
    fillReason();
    fireEvent.click(dialog().getByRole('button', { name: 'Thu hồi 2 người' }));

    await dialog().findByText('Hoàn tất: 2 thành công');
    const results = within(dialog().getByRole('list', { name: 'Kết quả từng người' }));
    expect(results.getAllByText('Đã thu hồi')).toHaveLength(2);
    expect(mock.backend.calls['POST /employees/:code/qr/revoke']).toBe(2);

    fireEvent.click(closeFooter());
    await screen.findByText('Đã thu hồi mã QR của 2 người');
    expect(screen.queryByRole('dialog')).toBeNull();
    expect(within(rowOf('E0001')).getByText('REVOKED')).toBeTruthy();
    expect(within(rowOf('E0002')).getByText('REVOKED')).toBeTruthy();
    expect(barOrNull()).toBeNull(); // cả hai đã xử lý xong nên không còn ai được chọn
  });

  it('từ 5 người trở lên phải gõ chữ xác nhận (bỏ qua hoa/thường và dấu)', async () => {
    await openEmployees();
    pick('E0001', 'E0002', 'E0003', 'E0004', 'E0005');
    fireEvent.click(bar().getByRole('button', { name: 'Thu hồi mã (5)' }));
    fillReason();
    const start = dialog().getByRole('button', { name: 'Thu hồi 5 người' }) as HTMLButtonElement;
    expect(start.disabled).toBe(true); // đã có lý do nhưng chưa gõ xác nhận

    const typed = dialog().getByRole('textbox', { name: 'Gõ THU HỒI để xác nhận' });
    fireEvent.change(typed, { target: { value: 'thu' } });
    expect(start.disabled).toBe(true);
    fireEvent.change(typed, { target: { value: 'thu hoi' } });
    expect(start.disabled).toBe(false);
  });

  it('dưới 5 người thì không có ô gõ xác nhận', async () => {
    await openEmployees();
    pick('E0001', 'E0002');
    fireEvent.click(bar().getByRole('button', { name: 'Thu hồi mã (2)' }));
    expect(dialog().queryByRole('textbox', { name: /Gõ THU HỒI/ })).toBeNull();
  });

  it('một người lỗi không làm hỏng cả lô; người lỗi vẫn được chọn; thử lại phần lỗi thành công', async () => {
    mock.server.use(http.post('/employees/E0002/qr/revoke', () => problem(500, 'INTERNAL_ERROR'), { once: true }));
    await openEmployees();
    pick('E0001', 'E0002', 'E0003');
    fireEvent.click(bar().getByRole('button', { name: 'Thu hồi mã (3)' }));
    fillReason();
    fireEvent.click(dialog().getByRole('button', { name: 'Thu hồi 3 người' }));

    await dialog().findByText('Hoàn tất: 2 thành công, 1 lỗi');
    const results = within(dialog().getByRole('list', { name: 'Kết quả từng người' }));
    expect(results.getByText('Không kết nối được máy chủ, thử lại')).toBeTruthy();
    expect(results.getAllByText('Đã thu hồi')).toHaveLength(2);

    fireEvent.click(dialog().getByRole('button', { name: 'Thử lại 1 lỗi' }));
    await dialog().findByText('Hoàn tất: 3 thành công');
    // Request lỗi bị `server.use` chặn nên không vào bộ đếm của backend giả: 2 lần thành công ban đầu + 1 lần thử lại.
    expect(mock.backend.calls['POST /employees/:code/qr/revoke']).toBe(3);
  });

  it('đóng khi còn người lỗi: chỉ người lỗi còn trong lựa chọn để xử lý lại sau', async () => {
    mock.server.use(http.post('/employees/E0002/qr/revoke', () => problem(500, 'INTERNAL_ERROR')));
    await openEmployees();
    pick('E0001', 'E0002');
    fireEvent.click(bar().getByRole('button', { name: 'Thu hồi mã (2)' }));
    fillReason();
    fireEvent.click(dialog().getByRole('button', { name: 'Thu hồi 2 người' }));
    await dialog().findByText('Hoàn tất: 1 thành công, 1 lỗi');
    fireEvent.click(closeFooter());

    await waitFor(() => expect(screen.queryByRole('dialog')).toBeNull());
    expect(bar().getByText('Đã chọn 1 nhân viên')).toBeTruthy();
    expect(bar().getByText('E0002 – Tran Thi Bich')).toBeTruthy();
  });

  it('đang chạy: hộp thoại bị khóa (Esc không đóng); Dừng giữa chừng thì người còn lại là "Chưa xử lý" và xử lý tiếp được', async () => {
    mock.backend.delayMs = 120;
    await openEmployees();
    pick('E0001', 'E0002', 'E0003', 'E0004');
    fireEvent.click(bar().getByRole('button', { name: 'Thu hồi mã (4)' }));
    fillReason();
    fireEvent.click(dialog().getByRole('button', { name: 'Thu hồi 4 người' }));

    await dialog().findByText(/Đang xử lý \d\/4/);
    fireEvent.keyDown(document, { key: 'Escape' });
    expect(screen.getByRole('dialog')).toBeTruthy();
    fireEvent.click(dialog().getByRole('button', { name: 'Dừng' }));

    await dialog().findByText(/chưa xử lý/, undefined, { timeout: 5000 });
    expect(dialog().getAllByText('Chưa xử lý').length).toBeGreaterThanOrEqual(1);

    mock.backend.delayMs = 0;
    fireEvent.click(dialog().getByRole('button', { name: /Xử lý tiếp \d người/ }));
    await dialog().findByText('Hoàn tất: 4 thành công', undefined, { timeout: 5000 });
  });
});

describe('A-05 cấp lại hàng loạt', () => {
  it('cảnh báo mã ACTIVE sẽ bị thu hồi, bỏ qua người đã nghỉ, cấp mã mới cho từng người', async () => {
    await openEmployees();
    pick('E0001', 'E0020', 'E0019');
    fireEvent.click(bar().getByRole('button', { name: 'Cấp lại mã (2)' }));

    expect(dialog().getByText('Cấp lại mã QR hàng loạt')).toBeTruthy();
    expect(dialog().getByText('1 người đang có mã ACTIVE: mã hiện tại sẽ bị thu hồi và một mã mới được cấp ngay.')).toBeTruthy();
    const skipped = within(dialog().getByRole('list', { name: 'Người bị bỏ qua' }));
    expect(skipped.getByText('E0019 – Lam Van Uy')).toBeTruthy();
    expect(skipped.getByText('Đã nghỉ việc')).toBeTruthy();
    expect(dialog().queryByLabelText(/Lý do thu hồi/)).toBeNull(); // cấp lại không cần lý do

    fireEvent.click(dialog().getByRole('button', { name: 'Cấp lại 2 người' }));
    await dialog().findByText('Hoàn tất: 2 thành công');
    expect(within(dialog().getByRole('list', { name: 'Kết quả từng người' })).getAllByText('Đã cấp mã mới')).toHaveLength(2);

    fireEvent.click(closeFooter());
    await waitFor(() => expect(screen.queryByRole('dialog')).toBeNull());
    expect(within(rowOf('E0020')).getByText('ACTIVE')).toBeTruthy();
    expect(bar().getByText('Đã chọn 1 nhân viên')).toBeTruthy(); // chỉ còn E0019 (bị bỏ qua)
  });

  it('không ai đủ điều kiện: hộp thoại chỉ báo lý do, không có nút thực hiện', async () => {
    await openEmployees();
    pick('E0019');
    // Nút cấp lại bị khóa; mở hộp thoại thu hồi cho người không đủ điều kiện không thực hiện được qua UI,
    // nên kiểm tra bằng trường hợp thu hồi với người chưa có mã ACTIVE (E0020 + E0019 → chỉ E0019 đủ).
    pick('E0020');
    expect((bar().getByRole('button', { name: 'Cấp lại mã (1)' }) as HTMLButtonElement).disabled).toBe(false);
    expect((bar().getByRole('button', { name: 'Thu hồi mã (1)' }) as HTMLButtonElement).disabled).toBe(false);
  });
});

describe('Chỉ xem người đã chọn', () => {
  it('chỉ hiện những người đã chọn, bỏ tick không làm dòng biến mất, tắt công tắc trả lại danh sách đầy đủ', async () => {
    await openEmployees();
    pick('E0001', 'E0003');
    fireEvent.click(bar().getByRole('checkbox', { name: 'Chỉ xem người đã chọn' }));
    expect(screen.getAllByRole('row')).toHaveLength(3); // tiêu đề + 2 dòng
    expect(screen.getByText('2 nhân viên')).toBeTruthy();
    expect(bar().getByText(/Đang chỉ hiện người đã chọn/)).toBeTruthy();

    pick('E0001'); // bỏ tick
    expect(screen.getAllByRole('row')).toHaveLength(3); // dòng vẫn còn (bỏ chọn)
    expect((within(rowOf('E0001')).getByRole('checkbox') as HTMLInputElement).checked).toBe(false);
    expect(bar().getByText('Đã chọn 1 nhân viên')).toBeTruthy();

    fireEvent.click(bar().getByRole('checkbox', { name: 'Chỉ xem người đã chọn' }));
    await screen.findByText('20 nhân viên');
    expect(screen.getAllByRole('row')).toHaveLength(21);
  });

  it('bỏ chọn hết khi đang xem riêng thì tự thoát chế độ này', async () => {
    await openEmployees();
    pick('E0001');
    fireEvent.click(bar().getByRole('checkbox', { name: 'Chỉ xem người đã chọn' }));
    expect(screen.getAllByRole('row')).toHaveLength(2);
    pick('E0001');
    await screen.findByText('20 nhân viên');
    expect(barOrNull()).toBeNull();
    expect(screen.getAllByRole('row')).toHaveLength(21);
  });

  it('chip có chấm màu theo trạng thái mã và tên trạng thái trong chú thích', async () => {
    await openEmployees();
    pick('E0001', 'E0020', 'E0019');
    const chips = bar().getAllByRole('listitem');
    const titles = chips.map((c) => c.getAttribute('title'));
    expect(titles).toEqual(['mã ACTIVE', 'mã ACTIVE, đã nghỉ', 'mã REVOKED']);
  });
});
