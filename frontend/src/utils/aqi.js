/**
 * AQI Data.
 */
export const AQI_BANDS = [
    {max : 50, label: 'Good', color: '#3aa657'},
    {max: 100, label: 'Satisfactory', color: '#9acd32'},
    {max: 200, label: 'Moderate', color: '#f5c518'},
    {max: 300, label: 'Poor', color: '#e03c31'},
    {max: 400, label: 'Very Poor', color: '#8b1a1a'},
]

/**
 * Turns an AQI number into its band (label + color).
 * @param {number | null | undefined} aqi - AQI reading. May be missing.
 * @returns {{ label: string, color: string }} The band, or a grey "No data" if invalid.
 */
export function getAqiBand(aqi) {
  return (
    AQI_BANDS.find((band) => aqi <= band.max) ??
    { label: 'No data', color: '#9ca3af' }
  )
}
