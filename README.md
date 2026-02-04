# Midas

Project repo for the JPMC Advanced Software Engineering Forage program

## Frontend (React) ✅

A minimal React + Vite frontend is included at `frontend/`.

- Dev: `cd frontend && npm install && cp .env.example .env && npm run dev` (set `VITE_API_BASE_URL` if your backend runs on a different host/port)
- Build: `npm run build` — copy `dist/` into `src/main/resources/static` if you want the Spring Boot app to serve the built UI.
- Maven: `mvn package` will run the frontend build and include the UI in the Spring Boot jar (requires network access; Node/npm will be installed automatically by the build plugin if not present).
