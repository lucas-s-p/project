import { formatPrice, formatRadius } from './format'

describe('formatRadius', () => {
  it('formats sub-kilometer radii in meters, rounded', () => {
    expect(formatRadius(842.6)).toBe('843 m')
  })

  it('formats radii of 1km or more in kilometers, one decimal place', () => {
    expect(formatRadius(5432)).toBe('5.4 km')
  })

  it('formats exactly 1000m as kilometers', () => {
    expect(formatRadius(1000)).toBe('1.0 km')
  })
})

describe('formatPrice', () => {
  it('formats a price as whole-dollar USD currency', () => {
    expect(formatPrice(250000)).toBe('$250,000')
  })

  it('rounds fractional cents away', () => {
    expect(formatPrice(99.9)).toBe('$100')
  })
})
