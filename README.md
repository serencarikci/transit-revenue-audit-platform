# Transit Revenue Audit Platform

Backend API and Angular UI for transit revenue reconciliation and transaction audit.

| Folder | Docs |
|--------|------|
| [`backend/`](backend/) | [`backend/README.md`](backend/README.md) |
| [`frontend/`](frontend/) | [`frontend/README.md`](frontend/README.md) |

## Quick start (local)

```bash
cp .env.example .env
docker compose up -d db

cd backend
mvn spring-boot:run

cd frontend
npm install
npm start
```

- API: http://localhost:8080
- UI: http://localhost:4200
- Users: `admin` / `finance` / `auditor` / `ops` (password `ChangeMe123!`)

## Full stack with Docker (Nginx + API + DB)

```bash
cp .env.example .env
docker compose up --build
```

- UI (Nginx): http://localhost
- API direct: http://localhost:8080
- Swagger via Nginx: http://localhost/swagger-ui.html

Nginx serves the Angular app and proxies `/api/` to the backend.

## Backend JAR

```bash
cd backend
mvn clean package -DskipTests
java -jar Release/transit-revenue-audit-platform.jar
```
