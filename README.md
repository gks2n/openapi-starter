# Eagle Bank API

Simple Spring Boot API.

## Requirements
- Java 21
- Docker (for local Postgres)

## Run Locally

1) Start Postgres
```bash
docker run --name eaglebank-postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:17
```

2) Create the `eaglebank` database
```bash
docker exec -it eaglebank-postgres psql -U postgres -c "CREATE DATABASE eaglebank;"
```

3) Start the app
```bash
./gradlew bootRun
```

The API listens on `http://localhost:8080`.

## Notes
- DB schema is created on startup (`create-drop`), and dropped on shutdown.
- Default password for new users is configured via `app.security.user.default-password` in `src/main/resources/application.properties`.
- OpenAPI spec lives at `src/main/resources/static/openapi.yaml`.

## Tests
```bash
./gradlew test
```

## Scenario Testing

Smoke test script (creates users and runs the main flows):
```bash
bash scripts/scenario_smoke.sh
```

Manual testing:
- Postman collection: `scripts/postman_collection.json`
- HTTP file (REST Client / IntelliJ): `scripts/scenarios.http`
