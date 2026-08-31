/** @type {import('jest').Config} */
module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'jsdom',
  setupFilesAfterEnv: ['<rootDir>/src/test/setupTests.ts'],
  moduleNameMapper: {
    '\\.css$': '<rootDir>/src/test/styleMock.js',
    '(.*)/config/apiBaseUrl$': '<rootDir>/src/config/apiBaseUrl.jest.ts',
  },
  transform: {
    '^.+\\.tsx?$': ['ts-jest', { tsconfig: 'tsconfig.jest.json' }],
  },
  testMatch: ['<rootDir>/src/**/*.test.{ts,tsx}'],
  coverageProvider: 'babel',
  collectCoverageFrom: [
    'src/**/*.{ts,tsx}',
    '!src/main.tsx',
    '!src/vite-env.d.ts',
    '!src/App.tsx',
    '!src/config/apiBaseUrl.ts',
    '!src/config/apiBaseUrl.jest.ts',
    // OpenLayers talks to real Canvas/WebGL APIs jsdom doesn't implement; this
    // layer is covered instead by manual and Playwright browser verification.
    '!src/hooks/useLandMarketplaceMap.ts',
    '!src/components/MapCanvas.tsx',
    '!src/pages/MapPage.tsx',
    '!src/map/styles.ts',
  ],
  coverageThreshold: {
    global: {
      statements: 80,
      branches: 75,
      functions: 80,
      lines: 80,
    },
  },
}
