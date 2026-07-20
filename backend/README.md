# Backend

Spring Boot modular monolith for transit revenue reconciliation and transaction audit.

## Stack

- Java 21, Spring Boot 4.0.7 (Web, Data JPA, Security, OAuth2 Resource Server, Validation, Actuator, AspectJ)
- PostgreSQL 16, Flyway (`v1.0.0.sql` baseline)
- MapStruct, springdoc-openapi (Swagger UI)
- JUnit 5, Mockito, Testcontainers

## Modules

identity · depot · terminal · transaction · reconciliation · anomaly · reporting · notification · audit-log

## Business rules

| Rule | Description |
|------|-------------|
| **1** | Expected closing balance = opening + deposited − withdrawal (`BigDecimal` only, never float/double). |
| **2** | When expected ≠ actual closing balance, create a `ReconciliationResult` with the variance. |
| **3** | Detect transactions stamped with date `1970-01-01` (epoch / invalid PDA timestamp). |
| **4** | Duplicate transactions are rejected by fingerprint `approvalNumber + cardAlias + terminalId + amount` (unique index + import check). |
| **5** | Flag transactions whose terminal has no depot assignment covering `transactionTime`. |
| **6** | Resolve depot for a terminal by **transaction time**, not by the currently open assignment (`findDepotForTerminalAt`). |
| **7** | Flag terminals that have not synced for longer than the configured PDA delay (default 24h). |
| **8** | Flag terminals that missed the planned daily shutdown sync window (default 01:00 UTC). |
| **9** | Flag cancellations with no matching sale on the same card alias, terminal, and amount within the configured proximity window. |
| **10** | Period totals expose **sale**, **cancellation**, and **net** amounts as three separate fields. |
| **11** | Report snapshots store a `resultHash`. Regenerating with the same parameters but a different hash raises an anomaly. |
| **12** | Audit log rows are append-only: no repository `delete*` API, plus DB-level `REVOKE DELETE` on `audit_log`. |

## Build

Requires **Java 21** and **Maven 3.9+**. Run Maven from this folder (`backend/`):

```bash
cd backend
mvn clean package -DskipTests
```

Produces an executable Spring Boot fat JAR (embedded Tomcat, no external Tomcat/WAR deploy):

- `target/transit-revenue-audit-platform.jar`
- `Release/transit-revenue-audit-platform.jar` (copy on `package`)

```bash
java -jar Release/transit-revenue-audit-platform.jar
```

Default HTTP port: `8080`.

## Run (dev)

From the repository root:

```bash
cp .env.example .env
docker compose up -d db
cd backend
mvn spring-boot:run
```

- API docs: `http://localhost:8080/swagger-ui.html`
- Version: `GET http://localhost:8080/api/v1/version` (no auth)
- Default users (password `ChangeMe123!`): `admin`, `finance`, `auditor`, `ops`

## Docker Compose (API + DB)

From the repository root:

```bash
docker compose up --build
```

This starts PostgreSQL, the Spring Boot API, and the Angular UI behind Nginx.

## API hardening

- CSV import: MIME/size/content sniffing + required header + per-line validation (`CsvUploadValidator`)
- Method security: `@PreAuthorize` on all controllers (`permitAll` only for `/auth/login` and `/version`)
- Sensitive logging: Logback `%maskMsg` masks password/token/cardAlias. A static guard test scans production log calls.
- Optimistic locking: PATCH (and resolve) endpoints accept body `version` and optional `If-Match` via `IfMatchSupport`

## Tests

```bash
cd backend
mvn test
mvn verify -Pintegration-test   # needs Docker
```
