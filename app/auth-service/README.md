# Auth Service

Owns users, roles, permissions and sessions. Every other service asks it who a bearer token belongs to and what that user may do.

## Overview

- **Users** sign up with a name, a unique case-sensitive username, an email and a password (hashed with Argon2). The notification service mails them a verification token.
- **Sessions:** logging in with email or username returns a session id, used as the bearer token. Sessions last until they are deleted.
- **Permissions** are declared in each service's contracts library and synced into the database at startup, along with the default `superadmin`, `admin` and `user` roles.
- **Field permissions** control who can read emails and roles, and who can change usernames, verification and roles.
- User changes are published to `auth.user.cdc` through the outbox.
- Data lives in the `auth` database.

## API

Request and response shapes are in Swagger UI on the gateway. A permission ending in `_own` applies when the target is the caller; the other variant applies to everyone else.

### Sessions

| Method | Path | What it does | Permission |
| --- | --- | --- | --- |
| POST | `/api/v1/sessions` | Log in with email or username plus password; returns the session id | — |
| GET | `/api/v1/sessions` | List sessions | `auth:session:read`, or `auth:session:read_own` when filtering by your own user id |
| GET | `/api/v1/sessions/{ID}` | Get a session with its user's role and permissions | `auth:session:read` / `auth:session:read_own` |
| DELETE | `/api/v1/sessions/{ID}` | Log out | `auth:session:write` / `auth:session:write_own` |

### Users

| Method | Path | What it does | Permission |
| --- | --- | --- | --- |
| POST | `/api/v1/users` | Sign up | — (setting `verified` or `roleId` needs `auth:user:write_is_verified` / `auth:user:write_role`) |
| GET | `/api/v1/users` | List users, filter by name, username, email, role, verified, dates | — (email and roleId are hidden without `auth:user:read_email` / `auth:user:read_role`) |
| GET | `/api/v1/users/{ID}` | Get a user | — (same masking as the list) |
| PUT | `/api/v1/users/{ID}` | Replace a user, or create it under that id | `auth:user:write` / `auth:user:write_own`, plus field permissions |
| PATCH | `/api/v1/users/{ID}` | Update some fields; also verifies an email anonymously with `X-EMAIL-VERIFICATION-TOKEN` | `auth:user:write` / `auth:user:write_own`, plus field permissions |
| DELETE | `/api/v1/users/{ID}` | Delete a user and their sessions | `auth:user:write` / `auth:user:write_own` |

Field permissions on PUT and PATCH: changing `verified` needs `auth:user:write_is_verified`, changing `username` needs `auth:user:write_username`, and changing `roleId` needs `auth:user:write_role` plus every permission of the new role (each with an `_own` variant). Writing another user also needs every permission of that user's current role.

### Roles

| Method | Path | What it does | Permission |
| --- | --- | --- | --- |
| GET | `/api/v1/roles` | List roles | `auth:role:read` |
| GET | `/api/v1/roles/{ID}` | Get a role with its permissions | `auth:role:read` |
| POST | `/api/v1/roles` | Create a role | `auth:role:write` |
| PUT | `/api/v1/roles/{ID}` | Replace a role, or create it under that id | `auth:role:write` |
| PATCH | `/api/v1/roles/{ID}` | Update some fields | `auth:role:write` |
| DELETE | `/api/v1/roles/{ID}` | Delete a role; its users move to the default role | `auth:role:write` |

The default roles cannot be changed or deleted.

### Permissions

| Method | Path | What it does | Permission |
| --- | --- | --- | --- |
| GET | `/api/v1/permissions` | List permissions | `auth:permission:read` |
| GET | `/api/v1/permissions/{ID}` | Get a permission | `auth:permission:read` |
