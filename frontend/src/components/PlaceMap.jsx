import { useEffect, useRef } from 'react'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import './PlaceMap.css'

const DEFAULT_CENTER = [20.6, 78.9] // India
const DEFAULT_ZOOM = 4
const PLACE_ZOOM = 11

/**
 * Click-to-pick map. Shows a pin at `marker` and calls `onPick(lat, lon)` on click.
 * @param {{ marker: { latitude: number, longitude: number } | null, onPick: (lat: number, lon: number) => void }} props
 */
export default function PlaceMap({ marker, onPick }) {
  const containerRef = useRef(null)
  const mapRef = useRef(null)
  const pinRef = useRef(null)
  const onPickRef = useRef(onPick)

  useEffect(() => {
    onPickRef.current = onPick
  }, [onPick])

  // create the map once
  useEffect(() => {
    const el = containerRef.current
    const map = L.map(el, {
      worldCopyJump: true,
      zoomSnap: 0.5,
      wheelPxPerZoomLevel: 80,
    }).setView(DEFAULT_CENTER, DEFAULT_ZOOM)

    // OpenStreetMap standard tiles (no API key). Colours are muted in PlaceMap.css so the
    // AQI colours stay the strongest on the page. OSM has no @2x tiles, so they are not
    // Retina-sharp; detectRetina would fix that but shrinks labels to half size.
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    }).addTo(map)

    // Wheel handling, so the map works on laptop trackpads:
    // - pinch arrives as a ctrl+wheel event: always zoom the map, never the whole page
    // - plain two-finger scroll / mouse wheel: scroll the page until the user clicks the map,
    //   then zoom the map until the pointer leaves it
    let engaged = false
    function onWheel(e) {
      if (e.ctrlKey) {
        e.preventDefault() // stop browser page zoom; Leaflet still gets the event and zooms
      } else if (!engaged) {
        e.stopPropagation() // hide it from Leaflet so the page scrolls normally
      }
    }
    // Safari pinches with non-standard gesture events instead of ctrl+wheel
    function onGesture(e) {
      e.preventDefault()
    }
    function disengage() {
      engaged = false
    }
    // capture phase so this runs before Leaflet's own wheel listener on the same element
    el.addEventListener('wheel', onWheel, { capture: true, passive: false })
    el.addEventListener('gesturestart', onGesture)
    el.addEventListener('gesturechange', onGesture)
    el.addEventListener('mouseleave', disengage)

    map.on('click', (e) => {
      engaged = true
      // wrap() folds clicks on repeated world copies back into -180..180
      const { lat, lng } = e.latlng.wrap()
      onPickRef.current(lat, lng)
    })
    map.on('focus', () => {
      engaged = true
    })
    map.on('blur', disengage)

    // Leaflet only measures its box on init; re-measure when the layout changes
    // (window resize, breakpoint switch, sidebar content growing) to avoid grey gaps
    const observer = new ResizeObserver(() => map.invalidateSize({ pan: false }))
    observer.observe(el)

    mapRef.current = map
    return () => {
      el.removeEventListener('wheel', onWheel, { capture: true })
      el.removeEventListener('gesturestart', onGesture)
      el.removeEventListener('gesturechange', onGesture)
      el.removeEventListener('mouseleave', disengage)
      observer.disconnect()
      map.remove()
      mapRef.current = null
      pinRef.current = null
    }
  }, [])

  // move the pin (a vector circle, so no marker image assets are needed)
  const lat = marker?.latitude
  const lon = marker?.longitude
  useEffect(() => {
    const map = mapRef.current
    if (!map) return
    if (lat === undefined || lon === undefined) {
      pinRef.current?.remove()
      pinRef.current = null
      return
    }
    if (!pinRef.current) {
      pinRef.current = L.circleMarker([lat, lon], {
        radius: 9,
        weight: 3,
        color: '#ffffff',
        fillColor: '#0f766e',
        fillOpacity: 1,
      }).addTo(map)
    } else {
      pinRef.current.setLatLng([lat, lon])
    }
    if (!map.getBounds().pad(-0.2).contains([lat, lon]) || map.getZoom() < PLACE_ZOOM - 3) {
      map.setView([lat, lon], Math.max(map.getZoom(), PLACE_ZOOM), { animate: true })
    }
  }, [lat, lon])

  return <div ref={containerRef} className="place-map" role="application" aria-label="Map. Click to pick a place." />
}
