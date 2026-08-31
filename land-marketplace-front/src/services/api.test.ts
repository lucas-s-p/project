import { api, ApiRequestError } from './api'
import { authStorage } from './authStorage'

function mockFetchOnce(response: Partial<Response> & { json?: () => Promise<unknown> }) {
  const fullResponse = {
    ok: true,
    status: 200,
    statusText: 'OK',
    json: async () => ({}),
    ...response,
  } as Response
  globalThis.fetch = jest.fn().mockResolvedValue(fullResponse)
  return globalThis.fetch as jest.Mock
}

describe('api', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  afterEach(() => {
    jest.restoreAllMocks()
  })

  it('sends requests without an Authorization header when no token is stored', async () => {
    const fetchMock = mockFetchOnce({ json: async () => ({ ok: true }) })

    await api.get('/land-plots')

    const [, init] = fetchMock.mock.calls[0];
    expect((init.headers as Record<string, string>).Authorization).toBeUndefined()
  })

  it('attaches the stored token as a Bearer Authorization header', async () => {
    authStorage.setToken('a-jwt-token')
    const fetchMock = mockFetchOnce({ json: async () => ({ ok: true }) })

    await api.get('/land-plots')

    const [, init] = fetchMock.mock.calls[0];
    expect((init.headers as Record<string, string>).Authorization).toBe('Bearer a-jwt-token')
  })

  it('resolves with the parsed JSON body on success', async () => {
    mockFetchOnce({ json: async () => ({ id: '1' }) })

    const result = await api.get<{ id: string }>('/land-plots/1')

    expect(result).toEqual({ id: '1' })
  })

  it('resolves with undefined for a 204 No Content response', async () => {
    mockFetchOnce({ status: 204 })

    const result = await api.delete('/land-plots/1')

    expect(result).toBeUndefined()
  })

  it('throws ApiRequestError with the backend message on a non-ok response', async () => {
    mockFetchOnce({ ok: false, status: 400, json: async () => ({ message: 'Price must be greater than zero' }) })

    await expect(api.post('/land-plots', {})).rejects.toMatchObject({
      status: 400,
      message: 'Price must be greater than zero',
    })
  })

  it('falls back to statusText when the error body has no message', async () => {
    mockFetchOnce({ ok: false, status: 500, statusText: 'Internal Server Error', json: async () => { throw new Error('no body') } })

    await expect(api.get('/land-plots')).rejects.toMatchObject({
      status: 500,
      message: 'Internal Server Error',
    })
  })

  // jsdom's Location object rejects any attempt to mock its properties (even on the
  // instance), so the redirect side effect itself isn't observable here; it's covered
  // by the Playwright end-to-end walkthroughs instead. The token clearing is not.
  it('clears the stored token on a 401 response', async () => {
    authStorage.setToken('a-jwt-token')
    mockFetchOnce({ ok: false, status: 401, json: async () => ({ message: 'Unauthorized' }) })

    await expect(api.get('/land-plots')).rejects.toBeInstanceOf(ApiRequestError)

    expect(authStorage.getToken()).toBeNull()
  })
})
