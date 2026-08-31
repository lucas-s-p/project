import type { MapMode } from '../types/landPlot'
import './ModeToggle.css'

interface ModeToggleProps {
  mode: MapMode
  onModeChange: (mode: MapMode) => void
  searchActive: boolean
  onClearSearch: () => void
}

const OPTIONS: { mode: MapMode; label: string }[] = [
  { mode: 'browse', label: 'Browse' },
  { mode: 'register', label: 'Register land' },
  { mode: 'search', label: 'Search area' },
]

export function ModeToggle({ mode, onModeChange, searchActive, onClearSearch }: ModeToggleProps) {
  return (
    <div className="mode-toggle">
      {OPTIONS.map((option) => (
        <button
          key={option.mode}
          type="button"
          className={option.mode === mode ? 'mode-toggle__button mode-toggle__button--active' : 'mode-toggle__button'}
          onClick={() => onModeChange(option.mode)}
        >
          {option.label}
        </button>
      ))}
      {searchActive && (
        <button type="button" className="mode-toggle__button mode-toggle__clear" onClick={onClearSearch}>
          Clear search
        </button>
      )}
    </div>
  )
}
