/**
 * US AQI categories (EPA). `color` is the band colour, `text` is readable on top of it.
 */
export const AQI_BANDS = [
  { max: 50, label: 'Good', color: '#2fa84f', text: '#ffffff' },
  { max: 100, label: 'Moderate', color: '#f2c318', text: '#10324a' },
  { max: 150, label: 'Unhealthy for Sensitive Groups', color: '#f28c28', text: '#10324a' },
  { max: 200, label: 'Unhealthy', color: '#e0393e', text: '#ffffff' },
  { max: 300, label: 'Very Unhealthy', color: '#8e44ad', text: '#ffffff' },
  { max: Infinity, label: 'Hazardous', color: '#7e0023', text: '#ffffff' },
]

const NO_DATA = { label: 'No data', color: '#9ca3af', text: '#ffffff' }

/**
 * Turns a US AQI number into its category (label + colours).
 * @param {number | null | undefined} aqi - AQI reading. May be missing.
 * @returns {{ label: string, color: string, text: string }} The band, or a grey "No data" if invalid.
 */
export function getAqiBand(aqi) {
  if (typeof aqi !== 'number' || !Number.isFinite(aqi) || aqi < 0) {
    return NO_DATA
  }
  return AQI_BANDS.find((band) => aqi <= band.max)
}

export const POLLUTANTS = [
  { key: 'pm2_5', label: 'PM2.5', name: 'Fine particles' },
  { key: 'pm10', label: 'PM10', name: 'Coarse particles' },
  { key: 'nitrogen_dioxide', label: 'NO₂', name: 'Nitrogen dioxide' },
  { key: 'sulphur_dioxide', label: 'SO₂', name: 'Sulphur dioxide' },
  { key: 'ozone', label: 'O₃', name: 'Ozone' },
  { key: 'carbon_monoxide', label: 'CO', name: 'Carbon monoxide' },
]

export const POLLUTANT_UNIT = 'µg/m³'
