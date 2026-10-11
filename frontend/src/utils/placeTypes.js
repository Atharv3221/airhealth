/**
 * Kinds of place a subscription can be (backend SubscriptionType). `color` tints the
 * subscription card; hues are spread around the wheel so cards are easy to tell apart.
 * All are dark enough for white text and icons on top.
 */
export const PLACE_TYPES = [
  { value: 'HOME', label: 'Home', color: '#0f766e' },
  { value: 'SCHOOL', label: 'School', color: '#1d4ed8' },
  { value: 'OFFICE', label: 'Office', color: '#7c3aed' },
  { value: 'PLAYGROUND', label: 'Playground', color: '#be185d' },
  { value: 'OTHER', label: 'Other', color: '#57534e' },
]

export function getPlaceType(value) {
  return PLACE_TYPES.find((t) => t.value === value) ?? PLACE_TYPES[PLACE_TYPES.length - 1]
}

// one line per AQI level: good (0-50), moderate (51-100), sensitive (101-150), unhealthy (151+)
const ADVICE = {
  HOME: [
    'Good time to open the windows and air out your home.',
    'Air is okay. Sensitive family members may want to limit long spells outdoors.',
    'Keep windows closed at busy times and run an air purifier if you have one.',
    'Keep windows shut and stay indoors as much as you can.',
  ],
  SCHOOL: [
    'Fine for outdoor play and sports at school.',
    'Outdoor activities are fine. Keep an eye on children with asthma.',
    'Consider moving PE and recess indoors for sensitive children.',
    'Move outdoor activities indoors today.',
  ],
  OFFICE: [
    'Good day to walk or cycle to work.',
    'Commute as usual. Sensitive people may prefer a mask.',
    'Consider a mask on your commute, or working from home if you can.',
    'Work from home if possible, and wear an N95 mask if you have to go out.',
  ],
  PLAYGROUND: [
    'Great air for playing outside.',
    'Fine for play. Take breaks if anyone has asthma.',
    'Keep play short and gentle. Sensitive children should skip it today.',
    'Skip the playground today.',
  ],
  OTHER: [
    'Air is clean. A good time to be outside.',
    'Air is acceptable for most people.',
    'Sensitive groups should cut back on long outdoor activity.',
    'Everyone should limit time outdoors today.',
  ],
}

function aqiLevel(aqi) {
  if (aqi <= 50) return 0
  if (aqi <= 100) return 1
  if (aqi <= 150) return 2
  return 3
}

/**
 * Advice for this kind of place given the current AQI and nearby heat spots.
 * @param {string} type - a PLACE_TYPES value
 * @param {number | null | undefined} aqi - current US AQI; no message without it
 * @param {{ count: number, radiusKm: number } | null} fire - nearby heat spot summary, if loaded
 * @returns {string | null}
 */
export function personalMessage(type, aqi, fire) {
  if (typeof aqi !== 'number' || !Number.isFinite(aqi) || aqi < 0) return null
  const level = aqiLevel(aqi)
  let message = (ADVICE[type] ?? ADVICE.OTHER)[level]

  if (fire?.count > 0) {
    const spots = `${fire.count} heat ${fire.count === 1 ? 'spot' : 'spots'} within ${fire.radiusKm} km`
    message += level <= 1
      ? ` ${spots} could bring smoke and make it worse.`
      : ` ${spots} may be adding to the smoke.`
  }
  return message
}
