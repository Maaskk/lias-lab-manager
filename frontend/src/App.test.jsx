import { describe, expect, it } from 'vitest';
import { LIAS, icaisProgramText, fsbmPublicProfiles, publicDataCoverage } from './data/liasData.js';

describe('LIAS real public data', () => {
  it('contains the official LIAS key figures', () => {
    expect(LIAS.stats.map(s => s.value)).toContain('4');
    expect(LIAS.stats.map(s => s.value)).toContain('42');
    expect(LIAS.stats.map(s => s.value)).toContain('28');
    expect(LIAS.stats.map(s => s.value)).toContain('230+');
    expect(LIAS.stats.map(s => s.value)).toContain('8');
  });

  it('contains the public teams and leadership', () => {
    expect(LIAS.teams.map(t => t.name).sort()).toEqual(['ILIAS', 'ISDIAC', 'SDTIC', 'SIMA'].sort());
    expect(LIAS.leadership.some(p => p.name.includes('Faouzia Benabbou'))).toBe(true);
    expect(LIAS.leadership.some(p => p.name.includes('Abdessamad Belangour'))).toBe(true);
  });

  it('contains the extracted ICAIS program text', () => {
    expect(icaisProgramText).toContain('International Conference on Artificial Intelligence & Systems');
    expect(icaisProgramText).toContain('Day 1: Thursday, November 27, 2025');
    expect(icaisProgramText).toContain('Day 2: Friday, November 28, 2025');
  });

  it('contains ICISCT 2026 public conference data and no invented birth dates', () => {
    expect(LIAS.icisct.deadlines).toHaveLength(4);
    expect(LIAS.icisct.registrationFees).toHaveLength(4);
    expect(LIAS.icisct.committees.some(c => c.name.includes('Nawal Sael'))).toBe(true);
    expect(fsbmPublicProfiles.some(p => p.name.includes('Houssine Azeddoug'))).toBe(true);
    expect(publicDataCoverage.birthDatesFound).toBe(false);
  });
});
