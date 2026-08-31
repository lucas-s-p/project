import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { LandPlotForm } from './LandPlotForm'

function fillForm(price: string, description: string, contact: string) {
  return async () => {
    await userEvent.clear(screen.getByLabelText('Total price (USD)'))
    if (price) await userEvent.type(screen.getByLabelText('Total price (USD)'), price)
    await userEvent.clear(screen.getByLabelText('Description'))
    if (description) await userEvent.type(screen.getByLabelText('Description'), description)
    await userEvent.clear(screen.getByLabelText('Contact'))
    if (contact) await userEvent.type(screen.getByLabelText('Contact'), contact)
  }
}

describe('LandPlotForm', () => {
  it('rejects a price of zero with an English message, without calling onSubmit', async () => {
    const onSubmit = jest.fn()
    render(<LandPlotForm submitting={false} errorMessage={null} onSubmit={onSubmit} onCancel={jest.fn()} />)

    await fillForm('0', 'A perfectly nice plot', 'owner@example.com')()
    await userEvent.click(screen.getByText('Save plot'))

    expect(screen.getByText('Price must be greater than zero.')).toBeInTheDocument()
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('rejects a description shorter than 5 characters', async () => {
    const onSubmit = jest.fn()
    render(<LandPlotForm submitting={false} errorMessage={null} onSubmit={onSubmit} onCancel={jest.fn()} />)

    await fillForm('1000', 'abc', 'owner@example.com')()
    await userEvent.click(screen.getByText('Save plot'))

    expect(screen.getByText('Description must be at least 5 characters long.')).toBeInTheDocument()
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('rejects a contact that is neither an email nor a phone number', async () => {
    const onSubmit = jest.fn()
    render(<LandPlotForm submitting={false} errorMessage={null} onSubmit={onSubmit} onCancel={jest.fn()} />)

    await fillForm('1000', 'A perfectly nice plot', 'not-a-contact')()
    await userEvent.click(screen.getByText('Save plot'))

    expect(screen.getByText('Contact must be a valid email address or phone number.')).toBeInTheDocument()
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('accepts a phone number as a valid contact', async () => {
    const onSubmit = jest.fn()
    render(<LandPlotForm submitting={false} errorMessage={null} onSubmit={onSubmit} onCancel={jest.fn()} />)

    await fillForm('1000', 'A perfectly nice plot', '+55 11 98765-4321')()
    await userEvent.click(screen.getByText('Save plot'))

    expect(onSubmit).toHaveBeenCalledWith({
      price: 1000,
      description: 'A perfectly nice plot',
      contact: '+55 11 98765-4321',
    })
  })

  it('calls onSubmit with valid values', async () => {
    const onSubmit = jest.fn()
    render(<LandPlotForm submitting={false} errorMessage={null} onSubmit={onSubmit} onCancel={jest.fn()} />)

    await fillForm('1000', 'A perfectly nice plot', 'owner@example.com')()
    await userEvent.click(screen.getByText('Save plot'))

    expect(onSubmit).toHaveBeenCalledWith({
      price: 1000,
      description: 'A perfectly nice plot',
      contact: 'owner@example.com',
    })
  })

  it('shows the backend error message when one is passed and no client-side error applies', () => {
    render(
      <LandPlotForm
        submitting={false}
        errorMessage="This boundary overlaps an already registered land plot."
        onSubmit={jest.fn()}
        onCancel={jest.fn()}
      />,
    )

    expect(screen.getByText('This boundary overlaps an already registered land plot.')).toBeInTheDocument()
  })

  it('calls onCancel when Cancel is clicked', async () => {
    const onCancel = jest.fn()
    render(<LandPlotForm submitting={false} errorMessage={null} onSubmit={jest.fn()} onCancel={onCancel} />)

    await userEvent.click(screen.getByText('Cancel'))

    expect(onCancel).toHaveBeenCalledTimes(1)
  })

  it('disables the buttons while submitting', () => {
    render(<LandPlotForm submitting={true} errorMessage={null} onSubmit={jest.fn()} onCancel={jest.fn()} />)

    expect(screen.getByText('Cancel')).toBeDisabled()
    expect(screen.getByText('Saving…')).toBeDisabled()
  })
})
