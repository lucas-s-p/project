import { useLandMarketplaceMap } from '../hooks/useLandMarketplaceMap'
import { formatRadius } from '../map/format'
import type { CircleSearchParams, Coordinate, LandPlot, MapMode } from '../types/landPlot'
import { LandPlotPopup } from './LandPlotPopup'
import './MapCanvas.css'

interface MapCanvasProps {
  plots: LandPlot[]
  mode: MapMode
  pendingPolygon: Coordinate[] | null
  pendingCircle: CircleSearchParams | null
  onPolygonDrawn: (boundary: Coordinate[]) => void
  onCircleDrawn: (params: CircleSearchParams) => void
  onDeletePlot: (id: string) => void
}

export function MapCanvas({
  plots,
  mode,
  pendingPolygon,
  pendingCircle,
  onPolygonDrawn,
  onCircleDrawn,
  onDeletePlot,
}: MapCanvasProps) {
  const { mapContainerRef, popupContainerRef, selectedPlot, closePopup, liveRadiusMeters } =
    useLandMarketplaceMap({ plots, mode, pendingPolygon, pendingCircle, onPolygonDrawn, onCircleDrawn })

  return (
    <div className="map-canvas">
      <div ref={mapContainerRef} className="map-canvas__surface" />
      <div ref={popupContainerRef}>
        {selectedPlot && (
          <LandPlotPopup
            plot={selectedPlot}
            onClose={closePopup}
            onDelete={() => {
              onDeletePlot(selectedPlot.id)
              closePopup()
            }}
          />
        )}
      </div>
      {mode === 'search' && liveRadiusMeters !== null && (
        <div className="map-canvas__radius-badge">{formatRadius(liveRadiusMeters)}</div>
      )}
      {mode === 'register' && (
        <p className="map-canvas__hint">Click on the map to trace the plot boundary, double-click to finish.</p>
      )}
      {mode === 'search' && liveRadiusMeters === null && (
        <p className="map-canvas__hint">Press and drag on the map to draw the search area.</p>
      )}
    </div>
  )
}
