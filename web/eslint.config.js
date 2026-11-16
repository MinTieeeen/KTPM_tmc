import js from '@eslint/js';
import globals from 'globals';
import reactHooks from 'eslint-plugin-react-hooks';
import reactRefresh from 'eslint-plugin-react-refresh';
import tseslint from 'typescript-eslint';

export default tseslint.config(
  { ignores: ['dist', 'node_modules', 'public/mockServiceWorker.js', 'src/shared/api/*.schema.ts'] },
  {
    extends: [js.configs.recommended, ...tseslint.configs.recommended],
    files: ['**/*.{ts,tsx}'],
    languageOptions: {
      ecmaVersion: 2020,
      globals: globals.browser,
    },
    plugins: {
      'react-hooks': reactHooks,
      'react-refresh': reactRefresh,
    },
    rules: {
      ...reactHooks.configs.recommended.rules,
      'react-refresh/only-export-components': ['warn', { allowConstantExport: true }],
      // Quy ước 15: mọi lời gọi API đi qua src/shared/api
      'no-restricted-globals': [
        'error',
        { name: 'fetch', message: 'Gọi API qua src/shared/api, không dùng fetch trực tiếp trong component.' },
      ],
    },
  },
  {
    files: ['src/shared/api/**/*.{ts,tsx}', 'src/mocks/**/*.{ts,tsx}'],
    rules: { 'no-restricted-globals': 'off' },
  },
);
