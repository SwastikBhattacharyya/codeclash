# Auth Contracts

What other services need from the auth service: its permission names, its user change event, and bearer session authentication.

## Overview

- `AuthPermissions` lists the auth service's permissions. Auth syncs them into its database at startup.
- `UserCdcEventData` is the payload of `auth.user.cdc`: id, name, username, email, verification state, role and timestamps.
- **Sessions:** `@EnableSessionAuthentication` adds a filter that reads the bearer token and resolves it through a `SessionResolver`. The `@Authenticated` and `@Authorized` annotations and `CurrentSession` use the result.
  - The auth service resolves sessions locally.
  - Other services use `RemoteSessionResolver`, which calls the auth service over HTTP.
- The shared 401, 403 and 503 errors live here too.
