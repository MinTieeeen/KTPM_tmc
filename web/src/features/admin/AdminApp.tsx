// Trang Admin: guard ADMIN → khung A-01 với hash routing #/employees (mặc định) và #/stations.
import { HashRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthGate } from '@/features/auth/AuthGate';
import { AdminShell } from './AdminShell';
import { EmployeesTab } from './EmployeesTab';
import { StationsTab } from './StationsTab';

export function AdminApp() {
  return (
    <AuthGate page="admin">
      <HashRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
        <Routes>
          <Route element={<AdminShell />}>
            <Route path="/employees" element={<EmployeesTab />} />
            <Route path="/stations" element={<StationsTab />} />
            <Route path="*" element={<Navigate to="/employees" replace />} />
          </Route>
        </Routes>
      </HashRouter>
    </AuthGate>
  );
}
