# Smart Irrigation Manager

[![CI](https://github.com/LittleNemo2/Smart-Irrigation-Manager/actions/workflows/ci.yml/badge.svg)](https://github.com/LittleNemo2/Smart-Irrigation-Manager/actions/workflows/ci.yml)
![Coverage](https://raw.githubusercontent.com/LittleNemo2/Smart-Irrigation-Manager/badges/jacoco.svg)

A full-stack Spring Boot web app that decides when to water a landscape. It manages properties, irrigation zones, and schedules for a landscaping business and its customers. Before each scheduled cycle, a rules-based decision engine combines soil moisture, the rain forecast, and evapotranspiration to decide whether a zone actually needs water, then reports how much water was saved.

**Live demo:** https://smart-irrigation.bluemushroom-a80bb0a0.westus.azurecontainerapps.io/
Sign in as `homeowner`, `landscaper`, or `admin`. The password for all three is `Irrigate#2026`.

## Features

- **Properties, zones, and schedules:** full create, edit, and delete with ownership checks, so users only see and change their own data.
- **Irrigation decision engine:** soil moisture, rain forecast, and evapotranspiration rules each adjust a watering probability, and the result decides whether a zone runs.
- **Live weather:** 7-day forecasts from the Open-Meteo API, with a 30-minute cache and a simulated fallback if the API is unreachable.
- **Scheduler:** checks every minute for upcoming watering cycles and runs a simulated valve. There's also an on-demand "run check now".
- **History search:** filter past watering events by property, zone, outcome, and date range, with totals.
- **Reports:** water usage by zone and date range, and water savings against a goal, with a CSS bar chart. Both export to CSV with protection against CSV injection.
- **Security:** Spring Security login, BCrypt password hashing, three roles (homeowner, landscaper, admin), an admin-only feedback inbox, and custom 403 and 404 pages.
- **Validation:** Bean Validation on every form, including cross-field rules (the dry threshold must be below the target moisture) and day-of-week checks.

## Architecture

```
web/         Spring MVC controllers + validated form objects (Thymeleaf views)
service/     business logic, ownership checks, scheduler
engine/      IrrigationDecisionEngine + DecisionRule implementations
weather/     WeatherClient interface: OpenMeteoWeatherClient, SimulatedWeatherClient
report/      abstract Report base with shared CSV export: UsageReport, SavingsReport
model/       JPA entities extending an abstract BaseEntity
repository/  Spring Data JPA repositories with search queries
security/    Spring Security configuration and user details service
```

New behavior plugs in without changing existing classes:

- **Decision rules:** each rule implements `DecisionRule` and extends `AbstractDecisionRule`. That base class uses the template method pattern to cap each rule's adjustment at ±50 points, and the engine runs all rules polymorphically.
- **Weather sources:** the weather service works with any `WeatherClient`, so a new source is just a new class.
- **Reports:** new report types extend the abstract report base and reuse its CSV export.

## Tech stack

Java 21, Spring Boot 4, Spring MVC, Spring Data JPA, Spring Security, Thymeleaf, H2 (development), PostgreSQL (production), JUnit 5, AssertJ, Testcontainers, JaCoCo, Spring Boot Actuator, Micrometer, Prometheus, Grafana, Docker, GitHub Actions, Azure

## Running locally

```bash
./mvnw spring-boot:run
```

Open http://localhost:8080. Development uses an in-memory H2 database with demo data.

To run against PostgreSQL in Docker, with data that persists between restarts:

```bash
docker compose up --build
```

## Tests

```bash
./mvnw verify
```

Every push and pull request runs the full suite in GitHub Actions and publishes a JaCoCo coverage report.

- **Unit tests** cover the decision engine (dry soil, wet soil, heavy rain, light drizzle, offline zones, and paused schedules), zone form validation, CSV export escaping and injection protection, and the custom metrics.
- **Integration tests** start the whole application against a real PostgreSQL database in Docker (Testcontainers), the same database used in production. They check data seeding, the history search filters, per-owner data scoping, the ownership checks, and that report totals match the history. They're skipped automatically when Docker isn't running.

A failing drizzle test led to a fix: the rain rule now weights its penalty by the expected rainfall instead of only the chance of rain.

## Observability

The app exposes Spring Boot Actuator health checks and Prometheus metrics on a separate management port (8081) that isn't exposed to the public internet. Alongside the standard JVM and HTTP metrics, it publishes custom Micrometer metrics:

| Metric | What it tracks |
|---|---|
| `irrigation.decisions` | Watering decisions, tagged by outcome (watered/skipped) and trigger (scheduled/manual) |
| `irrigation.water.used`, `irrigation.water.saved` | Gallons used and saved per decision |
| `irrigation.zones.online` | Zones whose controllers are online |
| `weather.fetch` | Open-Meteo API latency, tagged success or fallback |
| `weather.cache.requests` | Forecast cache hits and misses |

To run the app with Prometheus and a pre-built Grafana dashboard:

```bash
docker compose up --build
```

- App: http://localhost:8080 (use **Run check now** on a property to generate decisions)
- Grafana dashboard: http://localhost:3000
- Prometheus: http://localhost:9090
- Raw metrics: http://localhost:8081/actuator/prometheus

## Deployment

The app runs on **Azure Container Apps**, pulling its image from **Azure Container Registry**, with **Azure Database for PostgreSQL Flexible Server** as the database. Secrets are passed in as environment variables, so none are stored in the repo.

- `deploy/provision-azure.ps1` creates the resource group, the database, the registry, and the container app in one run.
- `deploy/deploy-azure.ps1` handles each new release. It runs the tests, builds and pushes a versioned image, updates the app, checks that the live site responds, and rolls back to the previous version if that check fails.

The `Dockerfile` is multi-stage: a Maven build stage, then a slim Java 21 runtime that runs the app as a non-root user.
