# Simmärken Tracker

Local-first Android app for parents to track their children's progress through
the official Swedish swimming badge systems (Svensk Simidrott and SLS) — which
skills are done, which badges are earned, and which physical pins still need buying.

**Core value:** at the swim hall, a parent can immediately answer *"Did they pass
this badge, and did we buy the physical pin?"*

## Planning: PRDs and ADRs

- Product scope and requirements: [`docs/prd/`](docs/prd/README.md)
- Architecture decisions: [`docs/adr/`](docs/adr/README.md)

Before implementing a feature, find (or add) its requirement ID in a PRD. When a
change makes a significant technical decision — new dependency, schema or backup
format change, cross-module pattern — add an ADR in the same PR. Don't edit the
decision of an accepted ADR; supersede it with a new one. Tick PRD requirements
off when they land.

Agent skills in `.agents/skills/` (e.g. `domain-modeling`, `grill-with-docs`) also
write ADRs to `docs/adr/`; use the numbering and template described in its README.

Older code comments cite GSD decision IDs such as `D-13`; each ADR's *Origin*
section maps them. The old `.planning/` directory is only in git history (`dcf12df`).

## Stack and constraints

Kotlin, Jetpack Compose (Material 3), Room via KSP, MVVM + repositories, manual DI
(`di/AppContainer`), kotlinx-serialization, Coil, DataStore. See ADR-0002.

- No network, no backend, no analytics (ADR-0003).
- Never persist badge visual state; derive it via `BadgeStateCalculator` (ADR-0004).
- Catalog codes are permanent; bump `catalogVersion` for any seed change (ADR-0005).
- Catalog content is verbatim Swedish in every UI language (ADR-0006).
- Backup JSON is a public format — keep it backwards compatible (ADR-0008).
- Every UI string must exist in both `values/` and `values-en/` (`StringsParityTest`).
- Never commit keystores, `keystore.properties` or service-account JSON (ADR-0011, ADR-0012).

## Verify locally

```sh
./gradlew lintDebug testDebugUnitTest       # same as CI
./gradlew connectedDebugAndroidTest         # Room/seed instrumented tests (emulator; not in CI)
```

## Agent skills

### Issue tracker

Issues are tracked in GitHub Issues (flyhard/simmmarken) through the `gh` CLI. See `docs/agents/issue-tracker.md`.

### Triage labels

Default labels: `needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: `CONTEXT.md` (created only when first needed) and `docs/adr/` at the repo root. See `docs/agents/domain.md`.
