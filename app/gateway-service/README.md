# Gateway Service

The single HTTP entry point. It routes requests to the auth, problem and contest services, checks bearer sessions on the way in, and serves one Swagger UI for all of them.

## Overview

- Built on Spring Cloud Gateway Server WebMVC, with no database or Kafka.
- A request with a bearer token is checked with the auth service first. An unknown or malformed token gets 401, so services only see anonymous requests or real sessions. Signup and login skip the check.
- Requests without a token pass through, and each service decides whether the endpoint is public.
- Upstream failures are returned in the same error body the services use.
- Swagger UI loads the OpenAPI documents of the three REST services through rewritten routes.

## API

| Path prefix | Forwarded to |
| --- | --- |
| `/api/*/sessions/**`, `/api/*/users/**`, `/api/*/roles/**`, `/api/*/permissions/**` | Auth Service (:8001) |
| `/api/*/questions/**`, `/api/*/categories/**`, `/api/*/tags/**`, `/api/*/submissions/**` | Problem Service (:8002) |
| `/api/*/contests/**` | Contest Service (:8003) |
| `/v3/api-docs/auth`, `/v3/api-docs/problem`, `/v3/api-docs/contest` | The matching service's `/v3/api-docs` |

Routes match any version segment; each service checks the version itself.

| Method | Path | What it does |
| --- | --- | --- |
| GET | `/swagger-ui.html` | Swagger UI with the auth, problem and contest specs |
| GET | `/gateway/v3/api-docs` | The gateway's own OpenAPI document |

| Situation | Response |
| --- | --- |
| Bearer token is not a UUID or the session does not exist | 401 `UNAUTHENTICATED` |
| Auth service cannot be reached during the session check | 503 `AUTH_UNAVAILABLE` |
| Upstream service times out | 504 `GATEWAY_TIMEOUT` |
| Upstream service cannot be reached | 503 `SERVICE_UNAVAILABLE` |
