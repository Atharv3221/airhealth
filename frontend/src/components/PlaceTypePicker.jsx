import { PLACE_TYPES } from '../utils/placeTypes'
import PlaceTypeIcon from './PlaceTypeIcon'
import './PlaceTypePicker.css'

/**
 * Row of icon chips for choosing a place type (radio buttons underneath, so arrow keys work).
 * @param {{ legend: string, name: string, value: string, onChange: (type: string) => void, disabled?: boolean, className?: string }} props
 */
export default function PlaceTypePicker({ legend, name, value, onChange, disabled = false, className = '' }) {
  return (
    <fieldset className={`type-picker ${className}`} disabled={disabled}>
      <legend className="type-picker__legend">{legend}</legend>
      <div className="type-picker__options">
        {PLACE_TYPES.map((t) => (
          <label
            key={t.value}
            className={`type-picker__option${value === t.value ? ' is-selected' : ''}`}
            style={{ '--type': t.color }}
          >
            <input
              type="radio"
              className="sr-only"
              name={name}
              value={t.value}
              checked={value === t.value}
              onChange={() => onChange(t.value)}
            />
            <PlaceTypeIcon type={t.value} size={18} />
            {t.label}
          </label>
        ))}
      </div>
    </fieldset>
  )
}
