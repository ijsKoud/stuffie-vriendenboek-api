# stuffie-vriendenboek-api

The back-end of the [Stuffie Vriendenboek](https://github.com/ijsKoud/stuffie-vriendenboek-web) (a friendship book for your stuffed animals). Spring Boot 4 / Kotlin, built from the kotlin-monorepo-template described further down, and structured like the services in [appiecal](https://github.com/ijskoud/appiecal).

```
apps/stuffie              the service: api/{controller,facade,service,models}, datasource/{models,repositories}, storage, helpers
packages/common          ApiException + ErrorResponse (application/problem+json)
packages/spring          application annotation, global error handling, swagger setup
packages/security-idp    validates Authentik access tokens (JWKS) and exposes the user's `sub`
```

## API

All routes are under `/v1`, authenticated with `X-Authorization: Bearer <access token>` (the Authentik token the web app forwards), except `/v1/shared/**`. Swagger UI is at `/`. Every user only ever sees their own data, keyed by the token's `sub`.

| Route | |
|---|---|
| `GET /v1/stuffies?status=ACTIVE\|RAINBOW_BRIDGE` | list |
| `POST /v1/stuffies` | add |
| `GET /v1/stuffies/{id}` | read |
| `PUT /v1/stuffies/{id}` | replace details (`photoId` attaches, `removePhoto` removes, omit to keep) |
| `POST /v1/stuffies/{id}/rainbow-bridge` | say goodbye (`leftAt`, optional `farewellMessage`); 409 if already there |
| `POST /v1/stuffies/{id}/restore` | bring back (note cleared, visit kept in `visits`) |
| `PUT /v1/stuffies/{id}/sharing` | `{"enabled": true\|false}`, creates/revokes the public token |
| `GET /v1/shared/{token}` · `GET /v1/shared/{token}/photo` | public read-only page and its photo |
| `POST /v1/photos` (multipart `file`) · `GET /v1/photos/{id}` | upload (max 5 MB, re-encoded to a JPEG of at most 1200px, metadata stripped) / download your own photo |
| `DELETE /v1/account` | GDPR erasure of the signed-in user's data |

There is deliberately no permanent delete for a single stuffie. Differences from what the web app's temporary API returned: enum values are `ACTIVE`/`RAINBOW_BRIDGE`, a stuffie has `photoId` (not `photoUrl`), shared pages have `hasPhoto` and load the photo from `/v1/shared/{token}/photo`, and photos are removed with `removePhoto: true`. The GQL layer maps these.

**Erasure.** `DELETE /v1/account` removes stuffies, visits, share links, photos and writes one hashed tombstone (`sha256("<salt>:<sub>")`). The caller still owns the extra confirmation, the fresh-login check and deleting its own login records. At every startup the service replays the tombstones, so restoring an old backup and restarting re-erases deleted accounts. Keep backups for at most 30 days.

## Run it

```bash
docker compose -f local/docker-compose.yml up -d     # Postgres
./gradlew :apps:stuffie:bootRun                      # profile `local`: port 8082, photos on disk, tokens from the mock IdP of the web repo (:4010)
./gradlew build                                      # tests run on H2, no Docker needed
```

Configuration (environment, see `apps/stuffie/src/main/resources/application.yml`):

| Variable | |
|---|---|
| `JDBC_URL` | Postgres JDBC url (schema is managed by Hibernate `ddl-auto: update`; move to Flyway before the first breaking change) |
| `OIDC_ISSUER`, `OIDC_CLIENT_ID`, `JWKS_URL` | tokens must carry this exact `iss` and `aud` and be signed (RS256) with a key from the JWKS |
| `SUB_HASH_SALT` | random, **never change it** (photo folders and tombstones depend on it) |
| `STORAGE_DRIVER` | `s3` (default) or `local` with `STORAGE_LOCAL_DIR` |
| `S3_ENDPOINT`, `S3_REGION`, `S3_BUCKET`, `S3_ACCESS_KEY`, `S3_SECRET_KEY`, `S3_FORCE_PATH_STYLE` | Garage on the NAS, see the web repo's `deployment/garage` |

---

# kotlin-monorepo-template

Kotlin/Gradle monorepo template for Spring Boot microservices that are released on every merge and deployed to the homelab k3s cluster through [homelab-gitops](https://github.com/ijskoud/homelab-gitops) after a manual approval.

```
apps/<service>      deployable Spring Boot services (one image each)
packages/<name>     shared libraries, used as plain Gradle project dependencies
build-logic         convention plugins (buildlogic.kotlin-library, buildlogic.spring-service)
tools/release       the release tool: bump detection, affected projects, PR checks (Kotlin, tested)
```

A new `apps/*` or `packages/*` directory with a `build.gradle.kts` is picked up automatically. Use `./gradlew build` locally.

## How releases work

1. **Pull request**: the title must be a [Conventional Commit](https://www.conventionalcommits.org) because PRs are squash-merged and the title becomes the commit on `main`. CI builds and tests only the affected projects and comments which versions the PR would release.
2. **Merge to main**: every affected service gets a new version, an image `ghcr.io/<owner>/<repo>-<service>:<version>`, a git tag `<service>/v<version>` and a GitHub Release. Nothing is deployed yet.
3. **Deploy**: a `Deploy` run is started per release and waits for approval on the `production` environment. After approval it sets the image in `homelab-gitops/apps/<repo>/<service>/`, syncs ArgoCD and waits until the application is healthy. It is skipped if gitops already holds a newer version.
4. **Rollback**: run the `Deploy` workflow by hand with an existing `service`, `version` and `rollback` ticked.

Which bump a service gets (paths decide *which* services, the commit decides *how much*):

| Change | Own commit type | Service bump |
|---|---|---|
| In `apps/<service>` | `feat` | minor |
| | `fix`, `chore`, `docs`, anything else | patch |
| | `type!:` or `BREAKING CHANGE:` footer | major |
| In a package the service depends on (transitively) | any | patch |
| | breaking | minor |
| In `build-logic/`, `gradle/`, `Dockerfile`, root build files | any | patch for every service |
| Only `README`, `.github`, `tools`, `docs` | | nothing is released |

A service's first release is `0.1.0`. Versions come from git tags only; Gradle receives `-PreleaseVersion`, local builds are `0.0.0-SNAPSHOT`.

## Adding a service

1. Create `apps/<name>/build.gradle.kts` with `plugins { id("buildlogic.spring-service") }`.
2. In `homelab-gitops`, create `apps/<repo>/<name>/` with the manifests. The image line must start with `ghcr.io/<owner>/<repo>-<name>` (any tag); the pipeline rewrites it. Use the `/actuator/health/liveness` and `/readiness` probes on port 8080, which the deploy's health wait relies on. Add `apps/<repo>/project-values.yaml` once per repo.
3. The release workflow fails the deploy with a clear message while those manifests are missing.

## One-time setup for a repository created from this template

- [ ] Create the GitHub repository from this template (so template sync can link the history) and name it as you want the gitops project and namespace to be named.
- [ ] Settings → Actions → General: allow GitHub Actions to create pull requests; default workflow permissions: read and write.
- [ ] Settings → Environments: create `production` with yourself as required reviewer.
- [ ] Secrets available to the repo (sync them with Phase): `APP_CLIENT_ID`, `APP_PRIVATE_KEY` (GitHub App with contents write on `homelab-gitops`, plus `workflows` and pull requests write on this repo for template sync), `ARGOCD_SERVER_URL`, `ARGOCD_TOKEN`, `TAILSCALE_CLIENT_ID`, `TAILSCALE_CLIENT_SECRET`.
- [ ] Branch protection on `main`: require the `Pull request / check` job, squash merging only, "Pull request title" as the squash commit message.
- [ ] After the first release of each service: make its GHCR package public (Package settings → Danger zone → visibility).

## Template sync

`template-sync.yaml` runs weekly in derived repos and opens a PR with changes to the shared pipeline (workflows, scripts, `build-logic`, `tools`, Dockerfile). Files in `.templatesyncignore` (apps, packages, README, the version catalog) are never touched. Edit the pipeline in the template, not in the derived repos.
