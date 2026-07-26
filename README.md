# Overlook Hotel

Welcome to **Overlook Hotel**, a Spring Boot–based hotel management application that covers everything from room booking to employee scheduling, feedback, loyalty points, events, analytics and more. This project will deepen your Java and Spring expertise while simulating a real‑world hospitality management system.

## Features

1. **Authentication & Roles**
   - Separate login pages for **Clients** and **Employee**
   - Role‐based access control (CLIENT, EMPLOYEE, RESPONSABLE and ADMIN)

2. **Room Management**
   - CRUD API for rooms (add, edit, delete, list)
   - EMPLOYEE UI for room inventory
   - Client UI for browsing and booking available rooms

3. **Reservation Management**
   - CRUD API for reservations (create, view, modify, cancel)
   - Employee view of all bookings with edit/cancel options
   - Client view: upcoming & past reservations; new booking interface

4. **Client & Employee Management**
   - CRUD API for client and employee records
   - Employee UI for personnel data

5. **Feedback & Ratings**
   - API for clients to submit comments and star–ratings
   - Client page to leave feedback
   - Employee dashboard to review and respond to feedback

6. **Loyalty Program**
   - Points‐based system rewarding repeat clients
   - Client UI to view and redeem loyalty points

7. **Events & Facilities**
   - API to create/manage hotel events and facility bookings
   - Automated notification system (email/webhook)
   - Employee UI to schedule events & configure notifications
   - Client UI to browse upcoming events and reserve facilities

8. **Analytics & Reporting**
   - Backend tools for occupancy rates, financial KPIs, and satisfaction metrics
   - Dashboards for both clients and managers

9. **Advanced Employee Management**
   - API for shift scheduling, leave requests, training records
   - Dedicated UI for HR tasks (managers and employees)

---

## Technical Stack

- Java 25.0.3
- Spring Boot 3.5
- Maven Wrapper
- PostgreSQL 17
- Redis
- Flyway
- Spring Security
- Thymeleaf
- MapStruct
- Lombok
- Docker
- GitHub Actions

---

## Prerequisites

- JDK 25.0.3
- PostgreSQL 17
- Git
- Docker, optional for container builds

The Maven Wrapper is included in `master/`, so a local Maven installation is not required.

## Configuration

The application reads its runtime configuration from Spring properties and environment variables.

For local development, create:

```text
master/src/main/resources/application-local.properties
```

Example:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/overlookhoteldb
spring.datasource.username=postgres
spring.datasource.password=change-me

spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=
spring.data.redis.timeout=2s
spring.cache.type=redis

spring.security.user.name=admin
spring.security.user.password=change-me

app.jwt.secret=change-me-with-at-least-32-characters
app.jwt.expiration-ms=86400000
```

`application-local.properties` is intended for local secrets and must not be committed.

## Database

The project uses PostgreSQL and Flyway.

Redis is used by the authentication layer to store JWT logout blacklist entries with a TTL matching the JWT lifetime. It is also used as the Spring cache backend for client reservations and employee planning/schedule reads.

Migration files are stored in:

```text
master/src/main/resources/db/migration
```

Flyway is the source of truth for the SQL schema. Hibernate automatic schema generation should not be used as the production schema management strategy.

## Run Locally

From the project root:

```bash
cd master
./mvnw spring-boot:run
```

On Windows:

```powershell
cd master
.\mvnw.cmd spring-boot:run
```

The application starts on:

```text
http://localhost:8080
```

## Build and Test

Linux/macOS:

```bash
cd master
./mvnw -B verify
```

Windows:

```powershell
cd master
.\mvnw.cmd -B verify
```

The current unit test suite still needs cleanup, but the recette profile now supports dedicated black-box integration tests from:

```text
master/src/integrationTest/java
```

## Docker

### Local Docker stack

Run the whole local stack with the application, PostgreSQL and Redis:

```bash
cp .env.docker.example .env.docker
docker compose --env-file .env.docker up --build
```

On Windows PowerShell:

```powershell
copy .env.docker.example .env.docker
docker compose --env-file .env.docker up --build
```

The application starts on:

```text
http://localhost:8080
```

Daily development commands:

```bash
docker compose --env-file .env.docker up --build
docker compose --env-file .env.docker up --build -d
docker compose --env-file .env.docker ps
docker compose --env-file .env.docker logs -f app
docker compose --env-file .env.docker restart app
docker compose --env-file .env.docker down
```

Use `up --build` when Java/resources changed and the image must be rebuilt.
Use `up --build -d` to run the stack in the background.

Reset the local Docker database and Redis data:

```bash
docker compose --env-file .env.docker down -v
docker compose --env-file .env.docker up --build
```

Check the application health endpoint:

```bash
curl http://localhost:8080/actuator/health/readiness
```

The same commands are available through the `Makefile`:

```bash
make docker-build
make docker-up
make docker-logs
make docker-down
```

### Build the application image

Build the image from the repository root:

```bash
docker build -f master/Dockerfile -t overlook-hotel:local master
```

The image uses the `prod` profile by default. For a single-container run, provide
external PostgreSQL and Redis endpoints:

```bash
docker run --rm -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/overlook_hotel \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=change-me \
  -e SPRING_DATA_REDIS_HOST=host.docker.internal \
  -e SPRING_DATA_REDIS_PORT=6379 \
  -e SPRING_DATA_REDIS_PASSWORD= \
  -e SPRING_SECURITY_USER_NAME=admin \
  -e SPRING_SECURITY_USER_PASSWORD=change-me \
  -e APP_JWT_SECRET=change-me-with-at-least-32-characters \
  overlook-hotel:local
```

### Recette stack

Run the production-like recette stack locally:

```bash
cp .env.recette.example .env.recette
docker compose --env-file .env.recette -f compose.recette.yml up --build
```

The recette stack uses PostgreSQL 17, Redis 7.4 and the `recette` Spring profile.

### Production compose

Production compose expects an already built/published image and external
PostgreSQL/Redis services:

```bash
cp .env.prod.example .env.prod
docker compose --env-file .env.prod -f compose.prod.yml up -d
```

The application image includes a Docker healthcheck against:

```text
/actuator/health/readiness
```

## CI/CD

GitHub Actions workflows are defined in:

```text
.github/workflows
```

The CI/CD strategy is documented in:

```text
docs/ci-cd.md
```

Branch strategy:

| Branch | Workflow | Purpose |
| --- | --- | --- |
| `dev` | `CI - Dev` | Fast validation, unit tests, code quality and formatting checks. |
| `recette` | `CD - Recette` | Build the JAR and Docker image, then run integration and end-to-end validation. |
| `main` / `v*.*.*` tags | `CD - Production` | Build the production candidate and publish the Docker image. |

Current automation:

- `dev` runs unit tests, packaging, formatting, coverage, PMD and SpotBugs/FindSecBugs.
- `recette` runs the same quality gate, then starts a Docker Compose stack with PostgreSQL 17 and the application image.
- `recette` checks `/actuator/health` and runs the Maven `integration` profile for dedicated `*IT.java` tests.
- `prod` validates the candidate and publishes the Docker image to GHCR outside pull requests.

Run the recette stack locally:

```bash
cp .env.recette.example .env.recette
docker compose --env-file .env.recette -f compose.recette.yml up --build
```

Run integration tests against the local recette stack:

```bash
cd master
./mvnw -B -Pintegration -Drecette.base-url=http://localhost:8080 verify
```

Current recette integration coverage includes authentication and logout, room authorization and browsing, client reservations, fidelity, employee planning and leave request flows. The tests require the dedicated recette PostgreSQL and Redis services.

Stop the recette stack:

```bash
docker compose --env-file .env.recette -f compose.recette.yml down --volumes
```

Recommended collaborative commands:

```bash
make dev-test
make quality
make recette-up
make recette-it
make recette-down
```

Collaboration rules:

- keep `dev` fast and Docker-free for everyday development;
- open pull requests into `dev`, `recette` and `main` instead of pushing directly;
- require green GitHub checks before merge;
- keep `*Test.java` for unit and MVC tests;
- keep `*IT.java` for Docker-backed recette integration tests;
- keep future E2E tests in recette only, not in the fast dev workflow;
- publish production images only after the validation workflow is green.


## Swagger

http://localhost:8080/swagger-ui/index.html

## Project Structure

```text
overlook_hotel/
├── .github/workflows/          GitHub Actions workflows
├── compose.recette.yml          Production-like recette Docker stack
├── docs/                       Project documentation
├── Makefile                    Shared local commands
├── README.md
└── master/
    ├── Dockerfile
    ├── mvnw
    ├── mvnw.cmd
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/master/master/
        │   │   ├── config/        Spring configuration
        │   │   ├── domain/        JPA entities
        │   │   ├── mapper/        MapStruct mappers
        │   │   ├── repository/    Spring Data repositories
        │   │   ├── security/      JWT and security helpers
        │   │   ├── service/       Business logic
        │   │   └── web/           MVC and REST controllers
        │   └── resources/
        │       ├── db/migration/  Flyway migrations
        │       ├── static/        CSS, JavaScript and images
        │       └── templates/     Thymeleaf pages
        └── test/                 Automated tests
```

## Professional Project Notes

For the CDA assessment, the delivery should demonstrate:

- a clear branch and environment strategy;
- reproducible builds;
- database migrations through Flyway;
- CI evidence through test and build reports;
- a deployable Docker artifact;
- documented limitations and improvement plan.
