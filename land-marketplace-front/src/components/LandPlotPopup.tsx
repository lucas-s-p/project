import { formatPrice } from '../map/format'
import type { LandPlot } from '../types/landPlot'
import './LandPlotPopup.css'

interface LandPlotPopupProps {
  plot: LandPlot
  onClose: () => void
  onDelete: () => void
}

export function LandPlotPopup({ plot, onClose, onDelete }: LandPlotPopupProps) {
  const handleDelete = () => {
    if (window.confirm('Delete this land plot? This cannot be undone.')) {
      onDelete()
    }
  }

  return (
    <div className="land-plot-popup">
      <button type="button" className="land-plot-popup__close" onClick={onClose} aria-label="Close">
        ×
      </button>
      <p className="land-plot-popup__price">{formatPrice(plot.price)}</p>
      <p className="land-plot-popup__description">{plot.description}</p>
      <p className="land-plot-popup__contact">Contact: {plot.contact}</p>
      <button type="button" className="land-plot-popup__delete" onClick={handleDelete}>
        Delete plot
      </button>
    </div>
  )
}
