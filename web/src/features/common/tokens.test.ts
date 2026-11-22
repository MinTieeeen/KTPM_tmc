import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { describe, expect, it } from 'vitest';

// Vitest bỏ qua nội dung file .css khi import (?raw trả chuỗi rỗng) nên đọc bằng fs; test chạy với cwd = web/.
const tokensCss = readFileSync(resolve(process.cwd(), 'src/features/common/tokens.css'), 'utf8');

type Tokens = Record<string, string>;

function parseBlock(css: string, selector: string): Tokens {
  const start = css.indexOf(`${selector} {`);
  const end = css.indexOf('\n}', start);
  const body = css.slice(start, end);
  const tokens: Tokens = {};
  for (const m of body.matchAll(/--([\w-]+):\s*(#[0-9a-fA-F]{6})\s*;/g)) tokens[m[1]!] = m[2]!;
  return tokens;
}

function luminance(hex: string): number {
  const channel = (i: number) => {
    const c = parseInt(hex.slice(1 + i * 2, 3 + i * 2), 16) / 255;
    return c <= 0.03928 ? c / 12.92 : ((c + 0.055) / 1.055) ** 2.4;
  };
  return 0.2126 * channel(0) + 0.7152 * channel(1) + 0.0722 * channel(2);
}

function contrast(a: string, b: string): number {
  const [hi, lo] = [luminance(a), luminance(b)].sort((x, y) => y - x);
  return (hi! + 0.05) / (lo! + 0.05);
}

const light = parseBlock(tokensCss, ':root');
const dark = { ...light, ...parseBlock(tokensCss, "[data-theme='dark']") };

// [chữ, nền, ngưỡng tối thiểu]: 4.5 cho chữ, 3 cho thành phần giao diện (WCAG 2.1 AA).
const PAIRS: Array<[string, string, number]> = [
  ['text', 'bg', 4.5],
  ['text', 'surface', 4.5],
  ['text', 'surface-2', 4.5],
  ['text-muted', 'bg', 4.5],
  ['text-muted', 'surface', 4.5],
  ['text-muted', 'surface-2', 4.5],
  ['primary-contrast', 'primary', 4.5],
  ['primary-contrast', 'primary-hover', 4.5],
  ['primary-fg', 'surface', 4.5],
  ['primary-soft-text', 'primary-soft', 4.5],
  ['danger-contrast', 'danger', 4.5],
  ['danger-contrast', 'danger-hover', 4.5],
  ['danger-fg', 'surface', 4.5],
  ['danger-soft-text', 'danger-soft', 4.5],
  ['success-fg', 'surface', 4.5],
  ['success-soft-text', 'success-soft', 4.5],
  ['warning-fg', 'surface', 4.5],
  ['warning-soft-text', 'warning-soft', 4.5],
  ['notice-soft-text', 'notice-soft', 4.5],
  ['appbar-text', 'appbar-bg', 4.5],
  ['appbar-muted', 'appbar-bg', 4.5],
  ['input-border', 'surface', 3],
  ['focus', 'surface', 3],
  ['focus', 'bg', 3],
];

describe.each([
  ['sáng', light],
  ['tối', dark],
] as const)('độ tương phản theme %s', (_name, tokens) => {
  it.each(PAIRS)('%s trên %s ≥ %s:1', (fg, bg, min) => {
    expect(tokens[fg], `thiếu token ${fg}`).toBeTruthy();
    expect(tokens[bg], `thiếu token ${bg}`).toBeTruthy();
    expect(contrast(tokens[fg]!, tokens[bg]!)).toBeGreaterThanOrEqual(min);
  });
});
