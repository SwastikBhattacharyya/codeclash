# Web

The CodeClash frontend: an Angular app with server-side rendering, served by Express. It is not part of the Gradle build.

## Overview

- `src/app` is the Angular app: `core` holds the API clients and shared state, `features` the pages, `layouts` and `ui` the shared components.
- `src/server` is the Express backend for the frontend. It keeps the session in a cookie, rejects cross-site writes, and proxies API calls to the gateway.
- The browser never talks to the gateway directly. Every API call goes through this server.

## Running locally

Needs Node 26 and npm 12. The gateway must be running (port 8000 in the `dev` profile).

```sh
cp .env.example .env
npm install
npm start              # dev server
```

| Script | What it does |
| --- | --- |
| `npm start` | dev server with live reload |
| `npm run build` | production build into `dist/web` |
| `npm run serve:ssr:web` | run the production build |
| `npm run check` | format and lint with Biome, format templates with Prettier |
| `npm run lint` | lint only |

## Configuration

Read from the environment, or from `app/web/.env` when it exists:

| Variable | Purpose |
| --- | --- |
| `API_URL` | gateway URL, for example `http://localhost:8000` |
| `NG_ALLOWED_HOSTS` | Host headers the server renders for |
| `NG_TRUST_PROXY_HEADERS` | forwarded headers to accept from a proxy |
| `TRUST_PROXY` | Express `trust proxy` setting, needed for secure cookies and the cross-site check |

Behind a proxy or tunnel that terminates TLS, the proxy's address must be covered by `TRUST_PROXY`. Otherwise every write fails with 403 and the session cookie loses `Secure`.

## Image

```sh
docker buildx build --load -t codeclash/web:dev app/web
```

The image serves the app on port 8080. `./codeclash build` builds it for you.
