// Isolated in its own module because `import.meta.env` is Vite-specific syntax;
// Jest substitutes this file with a plain constant (see jest.config.js) rather
// than trying to transform `import.meta` itself.
export const API_BASE_URL: string = import.meta.env.VITE_API_BASE_URL ?? '/api'
