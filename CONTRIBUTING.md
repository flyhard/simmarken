# Contributing to Simmärken

Thanks for helping out. This is a small project with one maintainer, so small,
focused pull requests are the easiest to review and merge.

## Before you start

- **Find the requirement.** Every feature needs a requirement ID in a
  [PRD](docs/prd/README.md), for example `SHAR-01`. If there isn't one, open an
  issue or add a requirement to a Draft PRD in your PR. Tick the requirement off
  (`[x]`) with a link to the evidence when it lands.
- **Record significant decisions.** If your change adds a dependency, changes the
  database schema, the seed format or the backup JSON, or introduces a pattern
  used across modules, add an [ADR](docs/adr/README.md) in the same PR. Never
  edit the decision of an accepted ADR. Write a new ADR that supersedes it.
- Bigger changes: open an issue first so we can agree on the approach.

## Ground rules

These come from the accepted ADRs. PRs that break them will not be merged.

- **No network, backend or analytics** ([ADR-0003](docs/adr/0003-offline-only-no-backend.md)).
  The app must not request the `INTERNET` permission, including through a
  dependency's merged manifest.
- **Never store badge visual state.** Derive it with `BadgeStateCalculator`
  ([ADR-0004](docs/adr/0004-progress-model-and-derived-badge-state.md)).
- **Catalog codes are permanent.** Any change to the seed JSON must bump
  `catalogVersion`
  ([ADR-0005](docs/adr/0005-versioned-catalog-seed-with-code-based-merge.md)).
  Record new sources in `docs/SOURCES.md`.
- **Catalog content stays verbatim Swedish**, whatever the UI language
  ([ADR-0006](docs/adr/0006-official-catalog-content-verbatim-swedish.md)).
- **The backup JSON is a public format.** Keep it backwards compatible
  ([ADR-0008](docs/adr/0008-json-backup-format-and-merge.md)).
- **Every UI string must exist in both** `values/strings.xml` (Swedish) and
  `values-en/strings.xml` (English). `StringsParityTest` checks this.
- **Never commit secrets:** no keystores, `keystore.properties`, or Play
  service-account JSON ([ADR-0011](docs/adr/0011-secrets-scanning-with-gitleaks.md),
  [ADR-0012](docs/adr/0012-release-signing-configuration.md)). Gitleaks runs on
  every push and PR.

## Verify locally

Run the same checks as CI before you open a PR:

```sh
./gradlew lintDebug testDebugUnitTest
```

If you change Room entities, DAOs, migrations or seed loading, also run the
instrumented tests on an emulator (they are not run in CI):

```sh
./gradlew connectedDebugAndroidTest
```

Optionally, scan for secrets with [Gitleaks](https://github.com/gitleaks/gitleaks):

```sh
gitleaks detect --source . --config .gitleaks.toml
```

## Pull requests

- Branch from `main` and keep each PR to one change.
- In the PR description, name the requirement IDs (and any ADRs) the PR covers,
  and say how you tested it. Screenshots help for UI changes.
- CI (`lint-and-test`) and Gitleaks must pass.
- Add or update unit tests for domain logic, such as badge state, merge and
  validation.
- The maintainer reviews every PR (see `.github/CODEOWNERS`).

## Licensing

By contributing, you agree that your code is released under the
[MIT License](LICENSE). Don't add third-party badge images or requirement text
without documenting where they come from in `docs/SOURCES.md` and covering them
in [NOTICE](NOTICE).
