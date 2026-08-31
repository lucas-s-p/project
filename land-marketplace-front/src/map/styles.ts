import { Fill, Stroke, Style } from 'ol/style'

const GREEN_DARK = '#33562f'
const GREEN_FILL = 'rgba(74, 124, 78, 0.25)'
const TERRACOTTA = '#c1622d'
const TERRACOTTA_FILL = 'rgba(193, 98, 45, 0.3)'
const SKY = '#4a7f9e'
const SKY_FILL = 'rgba(74, 127, 158, 0.12)'

export const plotStyle = new Style({
  fill: new Fill({ color: GREEN_FILL }),
  stroke: new Stroke({ color: GREEN_DARK, width: 2 }),
})

export const selectedPlotStyle = new Style({
  fill: new Fill({ color: TERRACOTTA_FILL }),
  stroke: new Stroke({ color: TERRACOTTA, width: 3 }),
})

export const drawingPolygonStyle = new Style({
  fill: new Fill({ color: GREEN_FILL }),
  stroke: new Stroke({ color: GREEN_DARK, width: 2, lineDash: [6, 6] }),
})

export const searchCircleStyle = new Style({
  fill: new Fill({ color: SKY_FILL }),
  stroke: new Stroke({ color: SKY, width: 2, lineDash: [4, 4] }),
})
