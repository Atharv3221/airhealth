/**
 * US AQI categories (EPA). `color` is the band colour, `text` is readable on top of it.
 */
export const AQI_BANDS = [
  {
    max: 50, label: 'Good', color: '#2fa84f', text: '#ffffff',
    advice: 'Air quality is satisfactory and poses little or no risk.',
  },
  {
    max: 100, label: 'Moderate', color: '#f2c318', text: '#10324a',
    advice: 'Acceptable. People unusually sensitive to air pollution may notice effects.',
  },
  {
    max: 150, label: 'Unhealthy for Sensitive Groups', color: '#f28c28', text: '#10324a',
    advice: 'Children, older adults and people with heart or lung conditions should cut back on long outdoor exertion.',
  },
  {
    max: 200, label: 'Unhealthy', color: '#e0393e', text: '#ffffff',
    advice: 'Everyone may begin to feel effects; sensitive groups more seriously.',
  },
  {
    max: 300, label: 'Very Unhealthy', color: '#8e44ad', text: '#ffffff',
    advice: 'Health alert: the risk of effects is increased for everyone.',
  },
  {
    max: Infinity, label: 'Hazardous', color: '#7e0023', text: '#ffffff',
    advice: 'Emergency conditions. Everyone is likely to be affected; avoid outdoor activity.',
  },
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
  {
    key: 'pm2_5', label: 'PM2.5', name: 'Fine particles',
    about: 'Particles under 2.5 µm, small enough to reach deep into the lungs and bloodstream. From vehicles, burning and industry.',
  },
  {
    key: 'pm10', label: 'PM10', name: 'Coarse particles',
    about: 'Particles under 10 µm such as dust, pollen and construction debris. They irritate the nose, throat and airways.',
  },
  {
    key: 'nitrogen_dioxide', label: 'NO₂', name: 'Nitrogen dioxide',
    about: 'A gas mostly from traffic exhaust. It inflames the airways and is highest near busy roads.',
  },
  {
    key: 'sulphur_dioxide', label: 'SO₂', name: 'Sulphur dioxide',
    about: 'A gas from burning coal and oil in power plants and industry. It can trigger asthma symptoms.',
  },
  {
    key: 'ozone', label: 'O₃', name: 'Ozone',
    about: 'Forms near the ground when sunlight reacts with traffic and industrial emissions. Usually peaks on sunny afternoons.',
  },
  {
    key: 'carbon_monoxide', label: 'CO', name: 'Carbon monoxide',
    about: 'A colourless gas from incomplete burning of fuel. Outdoor levels are usually far below harmful amounts.',
  },
]

export const POLLUTANT_UNIT = 'µg/m³'
