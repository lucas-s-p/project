import { landPlotService } from './landPlotService'
import { api } from './api'

jest.mock('./api', () => ({
  api: {
    get: jest.fn(),
    post: jest.fn(),
    delete: jest.fn(),
  },
}))

const mockedApi = api as jest.Mocked<typeof api>

describe('landPlotService', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('getAll fetches every land plot', async () => {
    mockedApi.get.mockResolvedValue([])

    await landPlotService.getAll()

    expect(mockedApi.get).toHaveBeenCalledWith('/land-plots')
  })

  it('getById fetches a single land plot by id', async () => {
    mockedApi.get.mockResolvedValue({})

    await landPlotService.getById('abc-123')

    expect(mockedApi.get).toHaveBeenCalledWith('/land-plots/abc-123')
  })

  it('create posts the new land plot', async () => {
    mockedApi.post.mockResolvedValue({})
    const input = { boundary: [], price: 1000, description: 'd', contact: 'c' }

    await landPlotService.create(input)

    expect(mockedApi.post).toHaveBeenCalledWith('/land-plots', input)
  })

  it('remove deletes the land plot by id', async () => {
    mockedApi.delete.mockResolvedValue(undefined)

    await landPlotService.remove('abc-123')

    expect(mockedApi.delete).toHaveBeenCalledWith('/land-plots/abc-123')
  })

  it('checkOverlap posts the boundary and returns the overlap result', async () => {
    mockedApi.post.mockResolvedValue({ overlaps: true })
    const boundary = [{ lng: 0, lat: 0 }]

    const result = await landPlotService.checkOverlap(boundary)

    expect(mockedApi.post).toHaveBeenCalledWith('/land-plots/check-overlap', { boundary })
    expect(result).toEqual({ overlaps: true })
  })

  it('searchWithinCircle builds the query string from lat/lng/radius', async () => {
    mockedApi.get.mockResolvedValue([])

    await landPlotService.searchWithinCircle({ lat: 1.5, lng: 2.5, radiusMeters: 500 })

    expect(mockedApi.get).toHaveBeenCalledWith('/land-plots/search?lat=1.5&lng=2.5&radiusMeters=500')
  })
})
