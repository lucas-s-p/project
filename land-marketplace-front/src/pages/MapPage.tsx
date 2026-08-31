import { useCallback, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { MapCanvas } from '../components/MapCanvas'
import { LandPlotForm } from '../components/LandPlotForm'
import { ModeToggle } from '../components/ModeToggle'
import { useAuth } from '../contexts/authContextDefinition'
import { ApiRequestError } from '../services/api'
import { landPlotService } from '../services/landPlotService'
import type { CircleSearchParams, Coordinate, LandPlot, MapMode } from '../types/landPlot'
import './MapPage.css'

export function MapPage() {
  const { logout } = useAuth()
  const navigate = useNavigate()
  const [plots, setPlots] = useState<LandPlot[]>([])
  const [searchResults, setSearchResults] = useState<LandPlot[] | null>(null)
  const [mode, setMode] = useState<MapMode>('browse')
  const [pendingPolygon, setPendingPolygon] = useState<Coordinate[] | null>(null)
  const [pendingCircle, setPendingCircle] = useState<CircleSearchParams | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState<string | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [overlapWarning, setOverlapWarning] = useState<string | null>(null)

  useEffect(() => {
    landPlotService
      .getAll()
      .then(setPlots)
      .catch(() => setLoadError('Could not load registered land plots. Please refresh the page.'))
  }, [])

  const handleModeChange = (nextMode: MapMode) => {
    setPendingPolygon(null)
    setFormError(null)
    setOverlapWarning(null)
    setMode(nextMode)
  }

  const handlePolygonDrawn = useCallback((boundary: Coordinate[]) => {
    setOverlapWarning(null)
    landPlotService
      .checkOverlap(boundary)
      .then((result) => {
        if (result.overlaps) {
          setOverlapWarning('This boundary overlaps an already registered land plot. Draw a different area.')
        } else {
          setPendingPolygon(boundary)
          setFormError(null)
        }
      })
      .catch(() => {
        // If the check itself fails, let the user proceed; the backend still
        // enforces the rule when the plot is actually submitted.
        setPendingPolygon(boundary)
        setFormError(null)
      })
  }, [])

  const handleCircleDrawn = useCallback((params: CircleSearchParams) => {
    setPendingCircle(params)
    landPlotService
      .searchWithinCircle(params)
      .then((results) => {
        setSearchResults(results)
        setMode('browse')
      })
      .catch(() => setLoadError('Search failed. Please try again.'))
  }, [])

  const handleFormCancel = () => {
    setPendingPolygon(null)
    setFormError(null)
  }

  const handleFormSubmit = (values: { price: number; description: string; contact: string }) => {
    if (!pendingPolygon) {
      return
    }
    setSubmitting(true)
    setFormError(null)

    landPlotService
      .create({ boundary: pendingPolygon, ...values })
      .then((created) => {
        setPlots((current) => [...current, created])
        setPendingPolygon(null)
        setMode('browse')
      })
      .catch((error: unknown) => {
        if (error instanceof ApiRequestError && error.status === 409) {
          setFormError('This boundary overlaps an already registered land plot. Adjust it and try again.')
        } else if (error instanceof ApiRequestError && error.status === 400) {
          setFormError(error.message)
        } else {
          setFormError('Could not save this land plot. Please try again.')
        }
      })
      .finally(() => setSubmitting(false))
  }

  const handleClearSearch = () => {
    setSearchResults(null)
    setPendingCircle(null)
  }

  const handleDeletePlot = (id: string) => {
    landPlotService
      .remove(id)
      .then(() => {
        setPlots((current) => current.filter((plot) => plot.id !== id))
        setSearchResults((current) => (current ? current.filter((plot) => plot.id !== id) : current))
      })
      .catch(() => setLoadError('Could not delete this land plot. Please try again.'))
  }

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  const displayedPlots = searchResults ?? plots

  return (
    <div className="map-page">
      <aside className="map-page__sidebar">
        <h1 className="map-page__brand">Plotline</h1>
        <ModeToggle
          mode={mode}
          onModeChange={handleModeChange}
          searchActive={searchResults !== null}
          onClearSearch={handleClearSearch}
        />
        <button type="button" className="map-page__logout" onClick={handleLogout}>
          Log out
        </button>
      </aside>
      <div className="map-page__main">
        {loadError && <p className="map-page__banner">{loadError}</p>}
        {overlapWarning && <p className="map-page__banner map-page__banner--warning">{overlapWarning}</p>}
        <MapCanvas
          plots={displayedPlots}
          mode={mode}
          pendingPolygon={pendingPolygon}
          pendingCircle={pendingCircle}
          onPolygonDrawn={handlePolygonDrawn}
          onCircleDrawn={handleCircleDrawn}
          onDeletePlot={handleDeletePlot}
        />
        {pendingPolygon && (
          <LandPlotForm
            submitting={submitting}
            errorMessage={formError}
            onSubmit={handleFormSubmit}
            onCancel={handleFormCancel}
          />
        )}
      </div>
    </div>
  )
}
