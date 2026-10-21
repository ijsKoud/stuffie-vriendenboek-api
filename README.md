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
