/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_GATE_URL?: string;
  readonly VITE_CENTRAL_URL?: string;
  readonly VITE_USE_MOCKS?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
