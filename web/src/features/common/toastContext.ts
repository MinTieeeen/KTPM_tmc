// Context + hook của Toast (tách khỏi Toast.tsx để file component chỉ export component).
import { createContext, useContext } from 'react';

export interface ToastApi {
  show: (message: string) => void;
}

export const ToastContext = createContext<ToastApi>({ show: () => undefined });

export function useToast(): ToastApi {
  return useContext(ToastContext);
}
