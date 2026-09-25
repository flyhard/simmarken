# ADR-0002: Native Android: Kotlin, Compose, Room, MVVM

- **Status:** Accepted
- **Date:** 2026-07-22 (recorded retroactively 2026-09-25)
- **Related:** PRD-0001

## Context

The app targets parents on Android phones, used one-handed at the swim hall. The
UI is state-driven (badge state changes as skills are ticked) and all data is
relational (Catalog → Category → Badge → Requirement, plus Kid and progress).

## Decision

- **Platform:** native Android only, Kotlin, package/application ID `se.simmarken`,
  minSdk 26.
- **UI:** Jetpack Compose with Material 3; Navigation Compose with type-safe
  routes (Home → ChildCatalog → BadgeDetail, Home → Settings).
- **Persistence:** Room (SQLite) via KSP — not kapt. Schemas are exported to
  `app/schemas/` from day one and every schema change ships with a migration
  and a migration test.
- **Architecture:** MVVM + repository. Room is the single source of truth; DAOs
  expose `Flow`, ViewModels expose `StateFlow`, UI collects with
  `collectAsStateWithLifecycle`. Writes run on `Dispatchers.IO`; ViewModels take
  an injectable dispatcher so they can be unit-tested on the JVM.
- **Package layout:** `data/` (Room, seed, export, prefs), `domain/`
  (calculators, repositories, validation), `ui/` (feature screens), `navigation/`, `di/`.
- **Libraries:** kotlinx-serialization (seed and backup JSON), Coil (pin images),
  DataStore Preferences (settings).

## Alternatives considered

- **Kotlin Multiplatform / SQLDelight** — no iOS requirement.
- **XML Views** — greenfield project; Compose fits state-driven UI better.
- **LiveData** — Flow/StateFlow integrates better with Compose.

## Consequences

- iOS would require a separate effort.
- Instrumented Room tests need an emulator (not run in CI, see ADR-0013).

## Origin

GSD `PROJECT.md` Key Decisions; `research/STACK.md`, `research/ARCHITECTURE.md`; Phase 1 context.
