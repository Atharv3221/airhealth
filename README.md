Airhealth
Know the air before you step out. Airhealth lets you check the air quality of any place on a map, see nearby satellite-detected heat spots, and subscribe to areas so you get notified when the air gets bad.

Live Demo
https://d2m0b3oibu3wv6.cloudfront.net (AWS: S3 + CloudFront + Elastic Beanstalk + RDS)

Screenshots
![Airhealth screenshot](docs/screenshot.png) ![Airhealth screenshot2](docs/screenshot2.png) ![Airhealth screenshot3](docs/screenshot3.png)

Features
AQI map: look up air quality for any place, by search, map click, or your current location
Pollutants: PM2.5, PM10, NO2, SO2, O3 and CO with a plain-language health meaning
Nearby heat spots: NASA FIRMS satellite detections within 50 km of a place
Subscriptions: subscribe to an area with an AQI threshold
In-app notifications: get alerted when your threshold is crossed
Health info: a page explaining how air quality affects health
Auth: email + password login with JWT
No personal details are stored.
Tech Stack
Layer	Tech
Backend	Java 21, Spring Boot, Spring Data JPA, Spring Security, JJWT
Database	PostgreSQL
Frontend	React (JavaScript), Leaflet
Data	Open-Meteo Air Quality + Geocoding, NASA FIRMS (active fires), OpenStreetMap Nominatim
Hosting	AWS: S3 + CloudFront (frontend), Elastic Beanstalk on Corretto 21 (backend), RDS PostgreSQL
How It Works
Air quality data (US AQI and pollutants) is fetched from Open-Meteo and cached in memory. No air data is stored in the database.
Fire detections are fetched from NASA FIRMS on request and cached for 30 minutes. They are satellite-detected heat sources and may include burning other than crop residue. Satellites can miss fires under cloud or between passes.
Subscription thresholds are checked when the frontend fetches notifications.
The React build is hosted in a private S3 bucket behind CloudFront. CloudFront routes /api/*, /user and /user/* to the Elastic Beanstalk backend, and sends 403/404 responses to /index.html so client-side routes work on refresh.
When the app is run as a single jar (locally or in Docker), the React build is served from the Spring Boot jar's static folder, and SpaController forwards client-side routes (/about, /login, /app, ...) to index.html.
Running Locally
Prerequisites
Java 21
Node.js 20.19+ (required by Vite 8)
PostgreSQL
Backend
Create a PostgreSQL database.
Set the [environment variables](#environment-variables) below.
Run:
cd backend
./mvnw spring-boot:run
The API runs on http://localhost:8080.

Frontend
cd frontend
npm install
npm run dev
Open http://localhost:5173. The Vite dev server proxies /api and /user to the backend on port 8080.

Run with Docker
docker build -t airhealth .
docker run -p 8080:8080 \
  -e DB_URL=<jdbc-url> \
  -e DB_USERNAME=<user> \
  -e POSTGRESQL_PASSWORD_AIRHEALTH=<password> \
  -e JWT_SECRET_AIRHEALTH=<secret> \
  -e FIRMS_KEY=<nasa-firms-map-key> \
  airhealth
Then open http://localhost:8080.

Environment Variables
Variable	Description	Default
DB_URL	PostgreSQL JDBC URL	jdbc:postgresql://localhost:5432/airhealth
DB_USERNAME	Database username	airhealth
POSTGRESQL_PASSWORD_AIRHEALTH	Database password	required
JWT_SECRET_AIRHEALTH	Secret used to sign JWTs	required
FIRMS_KEY	NASA FIRMS map key (for heat spots)	required for heat spots
PORT	HTTP port (5000 on Elastic Beanstalk)	8080
NOMINATIM_USER_AGENT	User-Agent sent to Nominatim, per its usage policy	AirHealth/0.1 (hackathon project)
On AWS, set these as Elastic Beanstalk environment properties. Use ?sslmode=require on the RDS JDBC URL.

Tests
The backend has 137 tests (JUnit 5, Mockito, Spring Security Test):

Controller tests (@WebMvcTest): request/response shape, bad-request cases, and auth rules for each endpoint
Service tests: auth, JWT, users, subscriptions, notifications, air quality, places, and fires, with repositories and clients mocked
Client tests: the Open-Meteo geocoding and Nominatim reverse-geocoding clients
contextLoads: starts the full application, so it needs a reachable database and the environment variables above
cd backend
./mvnw test
Frontend lint:

cd frontend
npm run lint
The Docker build skips tests (-DskipTests) because the build container has no database or secrets, so run them locally before deploying.

Planned
Less-polluted route suggestions and route subscriptions
Scheduled alerts
Credits
Air quality: Open-Meteo Air Quality API (CC BY 4.0). Contains modified Copernicus Atmosphere Monitoring Service information.
Place search: Open-Meteo Geocoding API, based on GeoNames data.
Active fire data: NASA FIRMS (VIIRS).
Map tiles and area names: Â© OpenStreetMap contributors (ODbL), looked up with Nominatim.
Map display: Leaflet.
Fonts: Fraunces and Plus Jakarta Sans (SIL Open Font License), via Fontsource.
Built with Spring Boot, JJWT, React, and Vite.
License
[MIT](LICENSE)

Author
Atharv Chavan (@Atharv3221)
