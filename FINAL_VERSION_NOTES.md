# LIAS Lab Manager — Final public website version

This version focuses on the official public LIAS website experience and keeps the internal JEE/Jakarta EE management portal.

## What was upgraded

- Premium public homepage inspired by the official LIAS visual identity, but more editorial and modern.
- Real LIAS public content only: hero, key figures, research axes, leadership, members table, recent publications, projects, partners, events and contact information.
- Complete ICAIS'25 PDF program extracted and exposed in a searchable page: `/icais-2025`.
- Official ICAIS'25 PDF included at: `/assets/Program_ICAIS25.pdf`.
- Official LIAS logo included at: `/assets/Logo-LIAS-01.png`.
- ICISCT 2026 page included at: `/icisct-2026`, with real submission portal link to Microsoft CMT.
- Smooth scrolling navigation, live clock/date, dark/light mode, responsive layout, working UI contact form, custom 403/404/500 pages.
- Backend seed cleaned to avoid fake lab content. It now seeds real public LIAS data plus only technical local accounts needed to run the demo.
- Internal management portal completed for the PDF scope: global search, editable member profiles, membership decisions, material request decisions, annual reports, notifications, messaging, governance history, role history, affiliations, meeting PV uploads and convention document uploads.
- Deployment was hardened with Nginx `/api` and `/uploads` proxying, Docker Compose environment variables, mandatory long JWT secret validation, stricter role-based route access and security headers.

## Run from scratch

```bash
cp .env.example .env
# Edit JWT_SECRET and POSTGRES_PASSWORD before production use.
docker compose down -v
docker compose up --build
```

Open:

```text
http://localhost
```

Admin login for local testing:

```text
admin@lias.ma
Use the value of INITIAL_ADMIN_PASSWORD from your local .env file.
```

## Update an already-running old version

If you already have the previous project running and want to replace it with this final one:

1. Stop the containers:

```bash
docker compose down
```

2. Replace your project folder with this final folder, or copy these updated files into your current project:

```text
frontend/src/App.jsx
frontend/src/index.css
frontend/src/App.test.jsx
frontend/src/data/liasData.js
frontend/public/assets/Logo-LIAS-01.png
frontend/public/assets/Program_ICAIS25.pdf
backend/src/main/java/ma/lias/config/DataLoader.java
backend/uploads/public/Logo-LIAS-01.png
backend/uploads/public/Program_ICAIS25.pdf
```

3. If you want the database seed to be refreshed with the cleaned real-data seed, run:

```bash
docker compose down -v
docker compose up --build
```

If you do not want to erase the database, run only:

```bash
docker compose up --build
```

In that case, the frontend changes will appear, but the old database rows remain.

## Verification

Fresh verification for this final package:

```bash
npm test
npm run build
docker compose config
docker compose build backend
docker compose build frontend
```

The tests verify the official LIAS public content and the final management/deployment contract. Docker builds verify the backend Maven package and the frontend Nginx image.

## Data source policy

Only public/official LIAS data was included. I did not add unverified data from unrelated ICISCT/ICAIS websites because several search results refer to different conferences using similar acronyms.
