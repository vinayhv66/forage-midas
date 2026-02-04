# Midas Frontend (React + Vite)

Simple frontend to list users and balances.

## Dev

1. cd frontend
2. npm install
3. copy `.env.example` to `.env` and update `VITE_API_BASE_URL` if backend isn't on the same host
4. npm run dev

By default the app will fetch `${VITE_API_BASE_URL}/users`, so set `VITE_API_BASE_URL=http://localhost:8080` when running the backend locally on 8080.

## Build

npm run build

You can serve `dist/` from any static host (or copy into Spring Boot's `src/main/resources/static` for a single-jar deployment).

## Deploy to Vercel (CI)

1. Create a Vercel project and set the project root to `frontend/` (or import the monorepo and set root to `frontend`).
2. Add the following GitHub secrets to your repo: `VERCEL_TOKEN`, `VERCEL_ORG_ID`, `VERCEL_PROJECT_ID`.
3. On push to `main` GitHub Actions will run `npm ci && npm run build` and deploy the `frontend/` site to Vercel.

Tip: Set `VITE_API_BASE_URL` in Vercel Environment Variables to your backend URL (e.g., `https://api.example.com`).
