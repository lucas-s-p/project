import { api } from './api'
import type { CircleSearchParams, Coordinate, LandPlot, LandPlotCreateInput, OverlapCheckResponse } from '../types/landPlot'

export const landPlotService = {
  getAll: (): Promise<LandPlot[]> => api.get<LandPlot[]>('/land-plots'),

  getById: (id: string): Promise<LandPlot> => api.get<LandPlot>(`/land-plots/${id}`),

  create: (input: LandPlotCreateInput): Promise<LandPlot> =>
    api.post<LandPlot>('/land-plots', input),

  remove: (id: string): Promise<void> => api.delete<void>(`/land-plots/${id}`),

  checkOverlap: (boundary: Coordinate[]): Promise<OverlapCheckResponse> =>
    api.post<OverlapCheckResponse>('/land-plots/check-overlap', { boundary }),

  searchWithinCircle: ({ lat, lng, radiusMeters }: CircleSearchParams): Promise<LandPlot[]> => {
    const params = new URLSearchParams({
      lat: String(lat),
      lng: String(lng),
      radiusMeters: String(radiusMeters),
    })
    return api.get<LandPlot[]>(`/land-plots/search?${params.toString()}`)
  },
}
