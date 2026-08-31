import { useCallback, useEffect, useRef, useState } from 'react'
import Feature from 'ol/Feature'
import OlMap from 'ol/Map'
import Overlay from 'ol/Overlay'
import View from 'ol/View'
import Draw from 'ol/interaction/Draw'
import Select from 'ol/interaction/Select'
import type { SelectEvent } from 'ol/interaction/Select'
import CircleGeom from 'ol/geom/Circle'
import Polygon from 'ol/geom/Polygon'
import TileLayer from 'ol/layer/Tile'
import VectorLayer from 'ol/layer/Vector'
import OSM from 'ol/source/OSM'
import VectorSource from 'ol/source/Vector'
import { fromLonLat, toLonLat } from 'ol/proj'

import { drawingPolygonStyle, plotStyle, searchCircleStyle, selectedPlotStyle } from '../map/styles'
import type { CircleSearchParams, Coordinate, LandPlot, MapMode } from '../types/landPlot'

const DEFAULT_CENTER: Coordinate = { lng: -46.6333, lat: -23.5505 }
const DEFAULT_ZOOM = 12

interface UseLandMarketplaceMapOptions {
  plots: LandPlot[]
  mode: MapMode
  pendingPolygon: Coordinate[] | null
  pendingCircle: CircleSearchParams | null
  onPolygonDrawn: (boundary: Coordinate[]) => void
  onCircleDrawn: (params: CircleSearchParams) => void
}

function toMapCoordinates(points: Coordinate[]): number[][] {
  return points.map((point) => fromLonLat([point.lng, point.lat]))
}

function toLngLatCoordinates(points: number[][]): Coordinate[] {
  return points.map(([x, y]) => {
    const [lng, lat] = toLonLat([x, y])
    return { lng, lat }
  })
}

export function useLandMarketplaceMap({
  plots,
  mode,
  pendingPolygon,
  pendingCircle,
  onPolygonDrawn,
  onCircleDrawn,
}: UseLandMarketplaceMapOptions) {
  const mapContainerRef = useRef<HTMLDivElement | null>(null)
  const popupContainerRef = useRef<HTMLDivElement | null>(null)

  const mapRef = useRef<OlMap | null>(null)
  const plotsLayerRef = useRef<VectorLayer<VectorSource> | null>(null)
  const pendingPolygonSourceRef = useRef(new VectorSource())
  const pendingCircleSourceRef = useRef(new VectorSource())
  const overlayRef = useRef<Overlay | null>(null)
  const selectedIdRef = useRef<string | null>(null)
  const plotsByIdRef = useRef<Map<string, LandPlot>>(new Map())

  const [selectedPlot, setSelectedPlot] = useState<LandPlot | null>(null)
  const [liveRadiusMeters, setLiveRadiusMeters] = useState<number | null>(null)

  const closePopup = useCallback(() => {
    selectedIdRef.current = null
    setSelectedPlot(null)
    overlayRef.current?.setPosition(undefined)
    plotsLayerRef.current?.changed()
  }, [])

  // Create the map once.
  useEffect(() => {
    if (!mapContainerRef.current || !popupContainerRef.current) {
      return
    }

    const plotsSource = new VectorSource()
    const plotsLayer = new VectorLayer({
      source: plotsSource,
      style: (feature) =>
        feature.get('plotId') === selectedIdRef.current ? selectedPlotStyle : plotStyle,
    })
    plotsLayerRef.current = plotsLayer

    const pendingPolygonLayer = new VectorLayer({
      source: pendingPolygonSourceRef.current,
      style: drawingPolygonStyle,
    })
    const pendingCircleLayer = new VectorLayer({
      source: pendingCircleSourceRef.current,
      style: searchCircleStyle,
    })

    const overlay = new Overlay({
      element: popupContainerRef.current,
      positioning: 'bottom-center',
      offset: [0, -12],
      stopEvent: true,
    })
    overlayRef.current = overlay

    const map = new OlMap({
      target: mapContainerRef.current,
      layers: [new TileLayer({ source: new OSM() }), plotsLayer, pendingPolygonLayer, pendingCircleLayer],
      overlays: [overlay],
      view: new View({
        center: fromLonLat([DEFAULT_CENTER.lng, DEFAULT_CENTER.lat]),
        zoom: DEFAULT_ZOOM,
      }),
    })
    mapRef.current = map

    return () => {
      map.setTarget(undefined)
      mapRef.current = null
      plotsLayerRef.current = null
      overlayRef.current = null
    }
  }, [])

  // Keep the plots layer in sync with the plots currently being displayed.
  useEffect(() => {
    const layer = plotsLayerRef.current
    if (!layer) {
      return
    }
    const source = layer.getSource()
    if (!source) {
      return
    }

    plotsByIdRef.current = new Map(plots.map((plot) => [plot.id, plot]))

    source.clear()
    const features = plots.map((plot) => {
      const feature = new Feature({
        geometry: new Polygon([toMapCoordinates(plot.boundary)]),
      })
      feature.set('plotId', plot.id)
      return feature
    })
    source.addFeatures(features)
  }, [plots])

  // Render the boundary being drawn for a new registration, controlled by the parent.
  useEffect(() => {
    const source = pendingPolygonSourceRef.current
    source.clear()
    if (pendingPolygon && pendingPolygon.length >= 3) {
      source.addFeature(new Feature({ geometry: new Polygon([toMapCoordinates(pendingPolygon)]) }))
    }
  }, [pendingPolygon])

  // Render the last drawn search circle, controlled by the parent.
  useEffect(() => {
    const source = pendingCircleSourceRef.current
    source.clear()
    if (pendingCircle) {
      const center = fromLonLat([pendingCircle.lng, pendingCircle.lat])
      source.addFeature(new Feature({ geometry: new CircleGeom(center, pendingCircle.radiusMeters) }))
    }
  }, [pendingCircle])

  // Swap the active interaction whenever the mode changes.
  useEffect(() => {
    const map = mapRef.current
    if (!map) {
      return
    }

    // These resets are tied to swapping the active OL interaction below, not to a
    // state change worth its own effect, so they run inline with that swap.
    setLiveRadiusMeters(null)
    if (mode !== 'browse') {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      closePopup()
    }

    if (mode === 'register') {
      const sketchSource = new VectorSource()
      const draw = new Draw({ source: sketchSource, type: 'Polygon', style: drawingPolygonStyle })
      draw.on('drawend', (event) => {
        const geometry = event.feature.getGeometry() as Polygon
        const [ring] = geometry.getCoordinates()
        onPolygonDrawn(toLngLatCoordinates(ring))
        sketchSource.clear()
      })
      map.addInteraction(draw)
      return () => {
        map.removeInteraction(draw)
      }
    }

    if (mode === 'search') {
      const sketchSource = new VectorSource()
      const draw = new Draw({ source: sketchSource, type: 'Circle', freehand: true, style: searchCircleStyle })
      draw.on('drawstart', (event) => {
        const geometry = event.feature.getGeometry() as CircleGeom
        geometry.on('change', () => {
          setLiveRadiusMeters(geometry.getRadius())
        })
      })
      draw.on('drawend', (event) => {
        const geometry = event.feature.getGeometry() as CircleGeom
        const [lng, lat] = toLonLat(geometry.getCenter())
        onCircleDrawn({ lat, lng, radiusMeters: geometry.getRadius() })
        sketchSource.clear()
        setLiveRadiusMeters(null)
      })
      map.addInteraction(draw)
      return () => {
        map.removeInteraction(draw)
      }
    }

    // browse mode: clicking a plot opens its details popup.
    const select = new Select({ layers: [plotsLayerRef.current!].filter(Boolean) })
    select.on('select', (event: SelectEvent) => {
      const feature = event.selected[0]
      if (!feature) {
        closePopup()
        return
      }
      const plotId = feature.get('plotId') as string
      const plot = plotsByIdRef.current.get(plotId) ?? null
      selectedIdRef.current = plotId
      setSelectedPlot(plot)
      plotsLayerRef.current?.changed()

      const geometry = feature.getGeometry() as Polygon
      overlayRef.current?.setPosition(geometry.getInteriorPoint().getCoordinates())
      select.getFeatures().clear()
    })
    map.addInteraction(select)
    return () => {
      map.removeInteraction(select)
    }
  }, [mode, onPolygonDrawn, onCircleDrawn, closePopup])

  return { mapContainerRef, popupContainerRef, selectedPlot, closePopup, liveRadiusMeters }
}
