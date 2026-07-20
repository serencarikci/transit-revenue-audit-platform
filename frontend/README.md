# Frontend

Angular 19 + Material UI for the audit platform.

Needs the backend on `http://localhost:8080` ([backend README](../backend/README.md)).

## Run

```bash
cd frontend
npm install
npm start
```

Open http://localhost:4200

Login examples: `admin` / `ChangeMe123!`

## Build

```bash
cd frontend
npm run build
```

Production build uses `apiBaseUrl: '/api/v1'` (for Nginx proxy).

## Docker (Nginx)

From the repository root:

```bash
docker compose up --build
```

Open http://localhost  
Nginx serves the UI and proxies `/api/` to the backend.

## Test

```bash
cd frontend
npm test -- --watch=false --browsers=ChromeHeadless
```

## Screens

Login, Dashboard, Reconciliation, Variance, Transactions, Anomalies, Terminals, Depots, Reports, Audit logs.
