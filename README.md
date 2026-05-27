# Overlook Hotel

Welcome to **Overlook Hotel**, a Spring Boot–based hotel management application that covers everything from room booking to employee scheduling, feedback, loyalty points, events, analytics and more. This project will deepen your Java and Spring expertise while simulating a real‑world hospitality management system.

## Features

1. **Authentication & Roles**
   - Separate login pages for **Clients** and **Employee**
   - Role‐based access control (CLIENT vs. ADMIN vs. EMPLOYEE)

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

spring.security.user.name=admin
spring.security.user.password=change-me

app.jwt.secret=change-me-with-at-least-32-characters
app.jwt.expiration-ms=86400000
```

`application-local.properties` is intended for local secrets and must not be committed.

## Database

The project uses PostgreSQL and Flyway.

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

The current test suite is limited and should be expanded with service, security, integration and end-to-end tests.

## Docker

Build the image from the repository root:

```bash
docker build -f master/Dockerfile -t overlook-hotel:local master
```

Run the container with the required environment variables:

```bash
docker run --rm -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/overlookhoteldb \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=change-me \
  -e SPRING_SECURITY_USER_NAME=admin \
  -e SPRING_SECURITY_USER_PASSWORD=change-me \
  -e APP_JWT_SECRET=change-me-with-at-least-32-characters \
  overlook-hotel:local
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

Some quality, integration and end-to-end steps are planned and documented, but must be introduced through dedicated pull requests.

## Project Structure

```text
overlook_hotel/
├── .github/workflows/          GitHub Actions workflows
├── docs/                       Project documentation
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
