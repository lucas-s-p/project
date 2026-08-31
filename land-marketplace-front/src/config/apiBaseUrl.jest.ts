// Jest's substitute for apiBaseUrl.ts (see moduleNameMapper in jest.config.js).
// Plain constant, no `import.meta` — Jest runs under a CommonJS-oriented
// transform that can't represent that syntax.
export const API_BASE_URL = '/api'
