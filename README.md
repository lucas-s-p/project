# Plotline

An MVP of an interactive platform for the real estate market, where landowners
list plots of land by drawing their exact boundaries on a map, and buyers
search for available plots inside a freehand-drawn circular area.

## What it does

- **Sign up and verify** — create an account with an email and password. A
  verification link is emailed to you; the account can't log in until it's
  clicked. A "Forgot password?" flow works the same way, by email.
- **Log in** — authenticates with a JWT, required for every map action below.
- **Register land** — draw the exact polygon of a plot on the map and submit
  a short form (price, description, contact). As soon as the boundary is
  drawn, it's checked against every existing plot; an overlapping boundary is
  rejected immediately, before the form even opens.
- **Search land** — draw a circle on the map by pressing and dragging the
  mouse, watching the radius update live. Only the plots that intersect that
  circular area are shown.
- **View and delete** — clicking any plot on the map opens a popup with its
  price, description, and contact details, with an option to delete it.

## Architecture

```
project/
├── docker-compose.yml       starts db + mailpit + api + frontend together
├── land-marketplace-api/    Spring Boot backend
└── land-marketplace-front/  React + TypeScript frontend
```

### Backend (`land-marketplace-api`)

Java 17 / Spring Boot, organized by layer:

- `model` — `LandPlot` (boundary stored as a JTS `Polygon`, SRID 4326, via
  Hibernate Spatial) and `User` (also a Spring Security `UserDetails`).
- `dto` — request/response records; entities are never exposed directly.
- `repository` — `LandPlotRepository`, with native PostGIS queries:
  `ST_Intersects` to detect boundary overlap, and `ST_DWithin` (on
  `geography`) to find plots inside a search circle. `UserRepository` for
  account lookups.
- `validation` — `@NoOverlap`, a class-level constraint that runs the overlap
  query against the incoming boundary before a plot is ever persisted, so
  the rejection happens as part of standard request validation (HTTP 409)
  rather than inside the service; `@ValidContact`, accepting either an email
  or a phone number.
- `service` — `LandPlotService` (land plot orchestration), `AuthService`
  (registration, email verification, login), `EmailService` (sends the
  verification email), `UserDetailsServiceImpl`.
- `config` — `SecurityConfig` (stateless JWT security), `JwtService`
  (issues/validates tokens), `JwtAuthenticationFilter`.
- `controller` — thin REST endpoints under `/api/land-plots` and
  `/api/auth`.

All spatial computation (overlap detection, radius search) is delegated to
PostGIS itself; the application never does geometry math in Java. The API is
browsable at `http://localhost:8080/swagger-ui/index.html`.

### Frontend (`land-marketplace-front`)

React + TypeScript. All OpenLayers logic is isolated from the rest of the
UI, and the map is gated behind authentication:

- `map/` — OpenLayers styles, and formatting helpers.
- `hooks/useLandMarketplaceMap.ts` — the only module that talks to
  OpenLayers directly: creates the map, keeps the plots layer in sync,
  switches between drawing a polygon, freehand-dragging a search circle, and
  selecting a plot, depending on the current mode.
- `contexts/AuthContext.tsx` — holds the JWT (persisted to `localStorage`)
  and exposes `login`/`logout` to the rest of the app.
- `components/` — `MapCanvas`, `ModeToggle`, `LandPlotForm`, `LandPlotPopup`,
  `AuthLayout`, `ProtectedRoute`.
- `pages/` — `LoginPage`, `RegisterPage`, `VerifyEmailPage`, and
  `MapPage.tsx`, which owns map application state (plots, mode, search
  results) and wires it to the `services/landPlotService` API layer.
- `App.tsx` — routes `/login`, `/register`, `/verify`, and `/` (the map,
  wrapped in `ProtectedRoute`) via `react-router-dom`.

The search circle is drawn with OpenLayers' `Draw` interaction in freehand
mode (press, drag, release), while the live radius shown during the drag is
computed from the drawn circle's geometry in the map's projection.

### Data flow

1. A new user registers with an email and password; the backend stores the
   account as unverified and emails a verification link (via SMTP —
   Mailpit in development, see below).
2. Clicking that link calls `GET /api/auth/verify`, marking the account
   verified. Logging in then calls `POST /api/auth/login`, returning a JWT
   that the frontend stores and attaches as `Authorization: Bearer <token>`
   to every subsequent request.
3. The frontend fetches all registered plots on load and renders them as
   polygons on the map.
4. Drawing a polygon and submitting the form sends the boundary as a list of
   `{ lng, lat }` points to `POST /api/land-plots`. If it overlaps an
   existing plot, the API returns `409` and the form shows an inline error;
   other validation failures (price, description, contact) return `400`
   with a specific message.
5. Dragging a search circle calls `GET /api/land-plots/search` with the
   center and radius; the map then shows only the returned plots.
6. Clicking a plot polygon opens a popup with that plot's details.

## Running with Docker

Requires Docker and Docker Compose. From the repository root:

```bash
docker compose up --build
```

This starts four services:

- `db` — PostgreSQL with PostGIS, on port `5432`.
- `mailpit` — a local SMTP server that catches verification emails instead
  of sending them anywhere real; its web inbox is at
  `http://localhost:8025`.
- `api` — the Spring Boot backend, on `http://localhost:8080` (interactive
  API docs at `/swagger-ui/index.html`).
- `front` — the React app served by Nginx, on `http://localhost:5173`
  (Nginx proxies `/api/*` to the `api` service internally).

Open `http://localhost:5173`, sign up, then open `http://localhost:8025` to
find the verification email and click its link. No further manual setup is
required.

## Running without Docker

### Database

Start a local PostGIS instance, for example:

```bash
docker run -d --name land-marketplace-db \
  -e POSTGRES_DB=land_marketplace \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgis/postgis:16-3.4
```

### Mail (for the sign-up verification email)

Start a local Mailpit instance:

```bash
docker run -d --name land-marketplace-mail \
  -p 1025:1025 -p 8025:8025 \
  axllent/mailpit:latest
```

Verification emails sent by the backend can then be read at
`http://localhost:8025`.

### Backend

```bash
cd land-marketplace-api
./gradlew bootRun
```

The API starts on `http://localhost:8080`, reading
`src/main/resources/application.properties`, which already points at
`jdbc:postgresql://localhost:5432/land_marketplace` and `localhost:1025` for
mail. The JWT signing secret (`app.jwt.secret`) defaults to a dev-only value
in that same file — override it via an environment variable for anything
beyond local development.

### Frontend

```bash
cd land-marketplace-front
nvm use
npm install
npm run dev
```

The app starts on `http://localhost:5173`. In development, Vite proxies
`/api/*` requests to `http://localhost:8080` (see `vite.config.ts`), so no
extra configuration is needed.

## Tests and coverage

Backend tests (unit tests, validator tests, `@WebMvcTest` controller tests,
and full `@SpringBootTest` + `MockMvc` integration tests for both land plots
and auth, running against a real PostGIS instance via Testcontainers) run
with:

```bash
cd land-marketplace-api
./gradlew test
```

Coverage is measured with JaCoCo and enforced at a minimum of 80%:

```bash
./gradlew jacocoTestCoverageVerification
```

The HTML report is generated at every `test` run and can be viewed at
`land-marketplace-api/build/reports/jacoco/test/html/index.html`.

Frontend tests (Jest + React Testing Library, Istanbul coverage) run with:

```bash
cd land-marketplace-front
nvm use
npm run test              # runs the suite once
npm run test:coverage     # same, plus a coverage summary and HTML report
```

The HTML report is generated at every `test:coverage` run, at
`land-marketplace-front/coverage/lcov-report/index.html`. Coverage currently
excludes `hooks/useLandMarketplaceMap.ts`, `components/MapCanvas.tsx`, and
`pages/MapPage.tsx`: they wire up real OpenLayers `Map`/`Draw`/`Select`
instances against Canvas APIs that jsdom doesn't implement, so that layer is
verified through manual and Playwright browser testing instead of jsdom-based
unit tests. Everything else — services, contexts, forms, and pages — is held
to the same 80% bar as the backend.
