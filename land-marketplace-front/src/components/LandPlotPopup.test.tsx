import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { LandPlotPopup } from './LandPlotPopup'
import type { LandPlot } from '../types/landPlot'

const plot: LandPlot = {
  id: 'plot-1',
  boundary: [{ lng: 0, lat: 0 }],
  price: 250000,
  description: 'A nice flat plot',
  contact: 'owner@example.com',
  createdAt: '2026-01-01T00:00:00Z',
}

describe('LandPlotPopup', () => {
  it('renders the plot price, description, and contact', () => {
    render(<LandPlotPopup plot={plot} onClose={jest.fn()} onDelete={jest.fn()} />)

    expect(screen.getByText('$250,000')).toBeInTheDocument()
    expect(screen.getByText('A nice flat plot')).toBeInTheDocument()
    expect(screen.getByText('Contact: owner@example.com')).toBeInTheDocument()
  })

  it('calls onClose when the close button is clicked', async () => {
    const onClose = jest.fn()
    render(<LandPlotPopup plot={plot} onClose={onClose} onDelete={jest.fn()} />)

    await userEvent.click(screen.getByLabelText('Close'))

    expect(onClose).toHaveBeenCalledTimes(1)
  })

  it('calls onDelete when the user confirms the delete prompt', async () => {
    jest.spyOn(window, 'confirm').mockReturnValue(true)
    const onDelete = jest.fn()
    render(<LandPlotPopup plot={plot} onClose={jest.fn()} onDelete={onDelete} />)

    await userEvent.click(screen.getByText('Delete plot'))

    expect(onDelete).toHaveBeenCalledTimes(1)
  })

  it('does not call onDelete when the user cancels the delete prompt', async () => {
    jest.spyOn(window, 'confirm').mockReturnValue(false)
    const onDelete = jest.fn()
    render(<LandPlotPopup plot={plot} onClose={jest.fn()} onDelete={onDelete} />)

    await userEvent.click(screen.getByText('Delete plot'))

    expect(onDelete).not.toHaveBeenCalled()
  })
})
