import { getAqiBand } from "../utils/aqi";

/**
 * Small card showing a city's AQI with a colored dot and its band label.
 * @param {{ name: string, aqi: number }} props
 * @returns {JSX.Element}
 */
export default function CityCard({ name, aqi }) {
    const band = getAqiBand(aqi);

    return (
        <article className="city-card">
            <span
            className="city-card__dot"
            style={{ background: band.color }}
            aria-hidden="true"
            />
            <div>
                <strong>
                    {name}, {aqi}
                </strong>
                <span>{band.label}</span>
            </div>
        </article>
    )
}
