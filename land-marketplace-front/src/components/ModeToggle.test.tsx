import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { ModeToggle } from './ModeToggle'

describe('ModeToggle', () => {
  it('marks the current mode button as active', () => {
    render(
      <ModeToggle mode="register" onModeChange={jest.fn()} searchActive={false} onClearSearch={jest.fn()} />,
    )

    expect(screen.getByText('Register land')).toHaveClass('mode-toggle__button--active')
    expect(screen.getByText('Browse')).not.toHaveClass('mode-toggle__button--active')
  })

  it('calls onModeChange with the clicked mode', async () => {
    const onModeChange = jest.fn()
    render(
      <ModeToggle mode="browse" onModeChange={onModeChange} searchActive={false} onClearSearch={jest.fn()} />,
    )

    await userEvent.click(screen.getByText('Search area'))

    expect(onModeChange).toHaveBeenCalledWith('search')
  })

  it('hides the clear-search button when no search is active', () => {
    render(<ModeToggle mode="browse" onModeChange={jest.fn()} searchActive={false} onClearSearch={jest.fn()} />)

    expect(screen.queryByText('Clear search')).not.toBeInTheDocument()
  })

  it('shows the clear-search button and wires it up when a search is active', async () => {
    const onClearSearch = jest.fn()
    render(<ModeToggle mode="browse" onModeChange={jest.fn()} searchActive onClearSearch={onClearSearch} />)

    await userEvent.click(screen.getByText('Clear search'))

    expect(onClearSearch).toHaveBeenCalledTimes(1)
  })
})
