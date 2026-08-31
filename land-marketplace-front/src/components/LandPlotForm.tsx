import { useState } from 'react'
import type { FormEvent } from 'react'
import './LandPlotForm.css'

const CONTACT_PATTERN = /^([^\s@]+@[^\s@]+\.[^\s@]+|\+?[0-9()\-\s]{8,20})$/

interface LandPlotFormValues {
  price: number
  description: string
  contact: string
}

interface LandPlotFormProps {
  submitting: boolean
  errorMessage: string | null
  onSubmit: (values: LandPlotFormValues) => void
  onCancel: () => void
}

function validate(price: string, description: string, contact: string): string | null {
  if (!(Number(price) > 0)) {
    return 'Price must be greater than zero.'
  }
  if (description.trim().length < 5) {
    return 'Description must be at least 5 characters long.'
  }
  if (!CONTACT_PATTERN.test(contact.trim())) {
    return 'Contact must be a valid email address or phone number.'
  }
  return null
}

export function LandPlotForm({ submitting, errorMessage, onSubmit, onCancel }: LandPlotFormProps) {
  const [price, setPrice] = useState('')
  const [description, setDescription] = useState('')
  const [contact, setContact] = useState('')
  const [clientError, setClientError] = useState<string | null>(null)

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    const validationError = validate(price, description, contact)
    if (validationError) {
      setClientError(validationError)
      return
    }
    setClientError(null)
    onSubmit({ price: Number(price), description, contact })
  }

  const displayedError = clientError ?? errorMessage

  return (
    <div className="land-plot-form-backdrop">
      <form className="land-plot-form" onSubmit={handleSubmit} noValidate>
        <h2>Register land plot</h2>
        <p className="land-plot-form__hint">
          The boundary you drew will be saved along with the details below.
        </p>

        <label className="land-plot-form__field">
          Total price (USD)
          <input
            type="number"
            step="0.01"
            value={price}
            onChange={(event) => setPrice(event.target.value)}
          />
        </label>

        <label className="land-plot-form__field">
          Description
          <textarea
            rows={4}
            maxLength={2000}
            value={description}
            onChange={(event) => setDescription(event.target.value)}
          />
        </label>

        <label className="land-plot-form__field">
          Contact
          <input
            type="text"
            maxLength={200}
            placeholder="Phone, e-mail, or other contact info"
            value={contact}
            onChange={(event) => setContact(event.target.value)}
          />
        </label>

        {displayedError && <p className="land-plot-form__error">{displayedError}</p>}

        <div className="land-plot-form__actions">
          <button type="button" onClick={onCancel} disabled={submitting}>
            Cancel
          </button>
          <button type="submit" className="land-plot-form__submit" disabled={submitting}>
            {submitting ? 'Saving…' : 'Save plot'}
          </button>
        </div>
      </form>
    </div>
  )
}
