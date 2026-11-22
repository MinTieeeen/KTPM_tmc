import { cleanup, fireEvent, screen, waitFor, within } from '@testing-library/react';
import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it } from 'vitest';
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
});
afterEach(cleanup);

const rowOf = (code: string) => screen.getByText(code).closest('tr')!;
const pick = (code: string, shiftKey = false) =>
  fireEvent.click(within(rowOf(code)).getByRole('checkbox', { name: `Chọn ${code}` }), { shiftKey });
const bar = () => screen.queryByRole('region', { name: 'Nhân viên đang chọn' });
const headerBox = () => screen.getByRole('checkbox', { name: 'Chọn tất cả nhân viên đang hiển thị' }) as HTMLInputElement;

async function openEmployees() {
  loginAs('admin');
  renderApp(<AdminApp />);
  await screen.findByText('E0001');
}

describe('A-02 chọn một hoặc nhiều nhân viên', () => {
  it('chưa chọn ai thì không hiện khung "Đã chọn"; mỗi dòng có một ô chọn', async () => {
    await openEmployees();
    expect(bar()).toBeNull();
    expect(screen.getAllByRole('checkbox')).toHaveLength(21); // 20 dòng + ô chọn tất cả
  });

  it('chọn một người: hiện khung với số lượng, tên người đó và thống kê trạng thái', async () => {
    await openEmployees();
    pick('E0003');
    const region = within(bar()!);
    expect(region.getByText('Đã chọn 1 nhân viên')).toBeTruthy();
    expect(region.getByText('E0003 – Le Van Cuong')).toBeTruthy();
    expect(region.getByText('1 mã ACTIVE')).toBeTruthy();
    expect(rowOf('E0003').className).toContain('rowSelected');
  });

  it('chọn nhiều người khác trạng thái: thống kê đúng (ACTIVE / REVOKED / đã nghỉ)', async () => {
    await openEmployees();
    ['E0001', 'E0002', 'E0019', 'E0020'].forEach((c) => pick(c));
    const region = within(bar()!);
    expect(region.getByText('Đã chọn 4 nhân viên')).toBeTruthy();
    expect(region.getByText('3 mã ACTIVE · 1 mã REVOKED · 1 đã nghỉ')).toBeTruthy();
    ['E0001 – Nguyen Van An', 'E0002 – Tran Thi Bich', 'E0019 – Lam Van Uy', 'E0020 – Tran Thi Binh'].forEach((t) =>
      expect(region.getByText(t)).toBeTruthy(),
    );
  });

  it('ô chọn tất cả: chọn hết, ở trạng thái nửa chọn khi chọn một phần, bỏ chọn hết khi bấm lại', async () => {
    await openEmployees();
    pick('E0001');
    expect(headerBox().checked).toBe(false);
    expect(headerBox().indeterminate).toBe(true);

    fireEvent.click(headerBox());
    expect(within(bar()!).getByText('Đã chọn 20 nhân viên')).toBeTruthy();
    expect(headerBox().checked).toBe(true);
    expect(headerBox().indeterminate).toBe(false);

    fireEvent.click(headerBox());
    expect(bar()).toBeNull();
    expect(headerBox().checked).toBe(false);
  });

  it('giữ Shift để chọn cả dải liền nhau', async () => {
    await openEmployees();
    pick('E0002');
    pick('E0005', true);
    const region = within(bar()!);
    expect(region.getByText('Đã chọn 4 nhân viên')).toBeTruthy();
    ['E0002', 'E0003', 'E0004', 'E0005'].forEach((c) => expect(region.getByLabelText(`Bỏ chọn ${c}`)).toBeTruthy());
  });

  it('lựa chọn được giữ khi tìm kiếm; người bị lọc ẩn vẫn hiện trong khung và được báo riêng', async () => {
    await openEmployees();
    pick('E0001');
    fireEvent.change(screen.getByLabelText('Tìm nhân viên'), { target: { value: 'khanh' } });
    await screen.findByText('1 nhân viên');
    const region = within(bar()!);
    expect(region.getByText('E0001 – Nguyen Van An')).toBeTruthy();
    expect(region.getByText('1 người đã chọn đang nằm ngoài bộ lọc hiện tại.')).toBeTruthy();
    expect(headerBox().checked).toBe(false);

    fireEvent.click(region.getByRole('button', { name: 'Chọn thêm 1 kết quả đang hiển thị' }));
    await waitFor(() => expect(within(bar()!).getByText('Đã chọn 2 nhân viên')).toBeTruthy());
    // E0001 vẫn nằm ngoài bộ lọc nên ghi chú còn; mọi dòng đang hiển thị đã được chọn.
    expect(within(bar()!).getByText('1 người đã chọn đang nằm ngoài bộ lọc hiện tại.')).toBeTruthy();
    expect(within(bar()!).queryByRole('button', { name: /Chọn thêm/ })).toBeNull();
    expect(headerBox().checked).toBe(true);

    // Xóa bộ lọc: người ẩn quay lại danh sách, ghi chú biến mất.
    fireEvent.change(screen.getByLabelText('Tìm nhân viên'), { target: { value: '' } });
    await screen.findByText('20 nhân viên');
    expect(within(bar()!).queryByText(/nằm ngoài bộ lọc/)).toBeNull();
  });

  it('bỏ chọn từng người bằng nút ✕ và bỏ chọn tất cả', async () => {
    await openEmployees();
    pick('E0001');
    pick('E0002');
    fireEvent.click(within(bar()!).getByLabelText('Bỏ chọn E0001'));
    expect(within(bar()!).getByText('Đã chọn 1 nhân viên')).toBeTruthy();
    expect((within(rowOf('E0001')).getByRole('checkbox') as HTMLInputElement).checked).toBe(false);

    pick('E0003');
    fireEvent.click(within(bar()!).getByRole('button', { name: 'Bỏ chọn tất cả' }));
    expect(bar()).toBeNull();
  });

  it('lựa chọn vẫn còn sau khi thu hồi mã của một người được chọn', async () => {
    await openEmployees();
    pick('E0012');
    fireEvent.click(within(rowOf('E0012')).getByRole('button', { name: 'Thu hồi' }));
    const dialog = within(screen.getByRole('dialog'));
    fireEvent.change(dialog.getByLabelText(/Lý do thu hồi/), { target: { value: 'Mất điện thoại' } });
    fireEvent.click(dialog.getByRole('button', { name: 'Thu hồi' }));
    await screen.findByText('Đã thu hồi mã QR của E0012');
    const region = within(bar()!);
    expect(region.getByText('E0012 – Ly Thi Mai')).toBeTruthy();
    expect(region.getByText('1 mã REVOKED')).toBeTruthy(); // thống kê cập nhật theo dữ liệu mới
  });
});
