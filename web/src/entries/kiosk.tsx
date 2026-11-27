import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import '../features/common/fonts.css';
import '../features/common/tokens.css';
import '../features/common/global.css';
import { enableMocksIfRequested } from '../features/common/bootstrap';
import { KioskPage } from '../features/kiosk/KioskPage';

void enableMocksIfRequested().then(() => {
  createRoot(document.getElementById('root')!).render(
    <StrictMode><KioskPage /></StrictMode>,
  );
});
