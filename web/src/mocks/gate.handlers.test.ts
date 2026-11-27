import { describe, it, expect } from 'vitest';
import { createMockGate, createGateHandlers } from './gate.handlers';

describe('gate.handlers', () => {
  it('creates mock gate with initial status', () => {
    const gate = createMockGate();
    expect(gate.status.gateId).toBe('gate-bd');
    expect(gate.status.online).toBe(true);
    expect(gate.status.turnstiles['BD-IN-01']).toBe('UP');
  });

  it('creates handlers for status, scan, and simulate-offline', () => {
    const gate = createMockGate();
    const handlers = createGateHandlers(gate);
    expect(handlers).toHaveLength(3);
  });
});
