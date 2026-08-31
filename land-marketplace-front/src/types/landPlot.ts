export interface Coordinate {
  lng: number
  lat: number
}

export interface LandPlot {
  id: string
  boundary: Coordinate[]
  price: number
  description: string
  contact: string
  createdAt: string
}

export interface LandPlotCreateInput {
  boundary: Coordinate[]
  price: number
  description: string
  contact: string
}

export interface CircleSearchParams {
  lat: number
  lng: number
  radiusMeters: number
}

export interface OverlapCheckResponse {
  overlaps: boolean
}

export type MapMode = 'browse' | 'register' | 'search'
