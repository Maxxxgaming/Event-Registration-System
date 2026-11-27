# Event Registration System — Frontend

The frontend is a React, TypeScript, and Vite application styled with Tailwind CSS.

## Prerequisites

- Node.js 20.19+ or 22.12+
- npm
- The backend running at `http://localhost:8080`

## Setup

From the `frontend/` directory:

```sh
npm install
```

The local development API URL is configured in `.env.development`. To use another backend, set `VITE_API_BASE_URL` to its `/api` base URL. `.env.example` shows the expected variable.

## Run and validate

```sh
npm run dev
npm run lint
npm run build
npm run preview
```

Vite prints the local development URL after startup. The initial app includes account login/registration, shared layout and UI components, and placeholder routing for future event and registration screens.
