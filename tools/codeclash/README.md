# codeclash

`./codeclash` builds, deploys, resets and seeds CodeClash. It works locally (docker compose or kind) and on a server (k3s or any other cluster). Run it without arguments for a menu, or with a command (`./codeclash --help`).

The wrapper script compiles this Go module into `~/.cache/codeclash/bin` and runs it. Nothing is built inside the repository.

## Requirements

- Go 1.26+ and Docker
- For k8s profiles: `kubectl`, `docker-buildx`, and `kind` when using kind
- No host compiler: seed tests are generated inside the runner image

## Quick start

**Compose** (infrastructure in Docker, services in the IDE):

```sh
./codeclash configure        # first run creates a profile; pick the compose target
./codeclash up               # Postgres, Redpanda, Mailpit, Grafana, pgAdmin, Kafka UI
# start the six services from the IDE with the `dev` Spring profile
./codeclash pipeline admin questions contests
```

Then start the frontend from `app/web` (see [app/web/README.md](../../app/web/README.md)).

**kind** (everything in a local cluster):

```sh
./codeclash configure        # pick the k8s target and the kind cluster
./codeclash pipeline         # build, deploy, seed the admin and the questions
./codeclash seed-contests
```

The app is then at `http://localhost:8080`.

## Commands

| Command | What it does | Target |
| --- | --- | --- |
| `configure`, `new`, `profiles`, `use NAME` | edit, create, list or switch profiles | both |
| `up`, `down` | start or stop the compose infrastructure | compose |
| `build` | build every image, tagged `<version>-<git sha>[-dirty]` | k8s |
| `deploy` | load the images and deploy to the cluster | k8s |
| `reset` | wipe the databases, Kafka topics and consumer groups, and Mailpit | both |
| `seed-admin` | create or reset the superadmin from the profile | both |
| `seed-questions [--verify]` | upload the question catalog in `seed/` | both |
| `seed-contests` | seed the contests, contestants and submissions in `seed/contests/` | both |
| `pipeline [STEPS...]` | run several steps in order | both |
| `status` | show the profile, images and pods or containers | both |
| `teardown` | delete the kind cluster, or the namespace of an existing cluster | k8s |

Flags: `--profile NAME` uses another profile for one run, and `--yes` answers yes to confirmations. External databases are still skipped with `--yes`.

### Pipeline

| Target | Step order |
| --- | --- |
| k8s | reset → build → deploy → admin → questions → verify → contests |
| compose | up → reset → admin → questions → verify → contests |

With no steps given, everything runs except `reset`, `verify` and `contests`.

## Profiles

Profiles live outside the repository, in `~/.config/codeclash` (or `$XDG_CONFIG_HOME/codeclash`):

```
profiles/<name>.toml   one profile, mode 0600
active                 name of the active profile
```

Every profile holds the superadmin login and the shared password of the seeded contestants. New profiles get generated passwords.

A profile has one of two targets:
- **compose:** manages `docker-compose.yaml`. Ports and DB credentials come from `.env`. pgAdmin and Kafka UI need no login.
- **k8s:** a kind cluster the tool creates, or any existing cluster in your kubeconfig.

Main k8s settings (`configure` asks for all of them):

| Setting | Default |
| --- | --- |
| Cluster | kind `codeclash`, or an existing kubeconfig context |
| Host port (kind) | `8080` |
| Namespace | `codeclash` |
| Image registry | none (needed unless the cluster is kind or a k3s on this machine) |
| Databases | in-cluster Postgres, or an external JDBC URL per service |
| Mail | in-cluster Mailpit, or an external SMTP server |
| Domain | `localhost` |
| Published tools | Grafana, pgAdmin, Kafka UI, Mailpit, behind one shared ops login |
| Trusted proxy hops | `1` on kind or behind a tunnel, `0` otherwise |
| TLS (existing clusters with a real domain) | on, Let's Encrypt staging |

Memory sizes, the warm sandbox count, concurrent submissions and the Postgres volume size are also asked for. The runner's Docker sidecar needs enough memory for every warm and running sandbox at the largest question memory limit, plus about 300 MB.

## Build and deploy

`build` builds the six services, the frontend, the sandbox image `codeclash-runner:local` and a sandbox bundle for the runner's Docker sidecar. The version comes from each `build.gradle.kts` or from `app/web/package.json`.

`deploy` then:
1. creates or reuses the cluster (kind) or checks that it is reachable
2. loads the images:

   | Cluster | How |
   | --- | --- |
   | kind | `kind load image-archive` |
   | k3s on this machine | `k3s ctr images import` (asks for sudo) |
   | anything else | `docker push` to the profile's registry |

3. creates the namespace and Secrets (never written to disk)
4. applies the infrastructure in `k8s/infra`
5. runs the Flyway migrations as Jobs (`k8s/migrations`); services never migrate on startup
6. applies the services in `k8s/services` and restarts their pods
7. installs Envoy Gateway (and cert-manager when TLS is on) and applies the routes

### Public access

| Host | Goes to |
| --- | --- |
| `<domain>` | the frontend |
| `grafana.<domain>` | Grafana |
| `pgadmin.<domain>` | pgAdmin |
| `kafka.<domain>` | Kafka UI |
| `mail.<domain>` | Mailpit |

Locally these are `http://localhost:8080`, `http://grafana.localhost:8080` and so on. Every tool uses the profile's ops email and password. The gateway service (Swagger UI) stays internal, and `deploy` prints the port-forward command for it.

### Running on a server (k3s)

1. **Disable Traefik.** Add the following to `/etc/rancher/k3s/config.yaml`, then run `sudo systemctl restart k3s`:

   ```yaml
   disable:
     - traefik
   ```
2. **Point DNS at the server.** Create A records for `<domain>` and `*.<domain>`, and open ports 80 and 443.
3. **Deploy with staging TLS.** In `configure`, pick the k3s context, your domain and the staging issuer, then deploy.
4. **Switch to production.** Once the staging certificates are issued, change the issuer to production and deploy again.

Let's Encrypt needs the HTTP port to stay 80. One certificate is issued per host. If certificates aren't ready within five minutes, deploy only warns, and the next deploy picks them up.

## Reset

`reset` wipes and re-migrates every service database, deletes all non-internal Kafka topics and consumer groups, and empties Mailpit. The services recreate their topics on start.

- **k8s:** scales the services to 0, resets, then scales them back up. External databases must be confirmed by typing their name.
- **compose:** stop the services in the IDE first; reset refuses while anything is connected.

## Seeding

- **`seed-admin`:** writes the profile's superadmin straight into the auth, problem and contest databases, because the API can't create the first admin. It waits for auth-service to create the `superadmin` role, so start auth-service once after a reset. Reruns reset the account to the profile's values.
- **`seed-questions` and `seed-contests`:** see [seed/README.md](../../seed/README.md).

## Troubleshooting

- When a step fails, the last 40 lines are printed and the full output is saved to `~/.cache/codeclash/last-error.log`.
- **Behind a tunnel or TLS proxy:** set trusted proxy hops to `1`, and make sure the proxy's address is inside the frontend's `TRUST_PROXY` (see [app/web/README.md](../../app/web/README.md)). Otherwise writes fail with 403 and the session cookie loses `Secure`.

## Teardown

- **kind:** deletes the cluster.
- **Existing clusters:** deletes the namespace, including the database volumes, after you type its name.
