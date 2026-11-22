// Tab hiện tại của Portal (Mã QR | Mã gần đây), đồng bộ với hash `#/recent` để tải lại trang giữ nguyên tab.
import { useCallback, useEffect, useState } from 'react';

export type PortalTab = 'qr' | 'recent';

const readTab = (): PortalTab => (window.location.hash === '#/recent' ? 'recent' : 'qr');

export function usePortalTab(): [PortalTab, (tab: PortalTab) => void] {
  const [tab, setTab] = useState<PortalTab>(readTab);

  useEffect(() => {
    const onHashChange = () => setTab(readTab());
    window.addEventListener('hashchange', onHashChange);
    return () => window.removeEventListener('hashchange', onHashChange);
  }, []);

  const change = useCallback((next: PortalTab) => {
    const url = next === 'recent' ? '#/recent' : window.location.pathname + window.location.search;
    window.history.replaceState(null, '', url);
    setTab(next);
  }, []);

  return [tab, change];
}
