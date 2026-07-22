# Walking Skeleton — Simmärken Tracker

**Phase:** 1
**Generated:** 2026-07-22

## Capability Proven End-to-End

A developer can run the app locally, tap **Add test kid** on the Home screen, and see the kid count increase — data persists in the on-device Room database across process restart (verified by `DatabasePersistenceTest` and manual force-stop).

## Architectural Decisions

| Decision | Choice | Rationale |
|---|---|---|
| Framework | Kotlin 2.1.21 + Jetpack Compose (BOM 2025.12.01) + AGP 8.7.2 | Official Android stack; state-driven UI for progress tracking |
| Data layer | Room 2.8.4 + KSP 2.1.21-2.0.2 on SQLite (`simmarken.db`) | DATA-01 offline persistence; Flow-based reactive reads |
| Auth | None | 100% offline local app; single-user device storage |
| DI | Manual `AppContainer` (no Hilt in Phase 1) | Minimal walking skeleton; defer Hilt until ViewModel count grows |
| Navigation | Navigation Compose 2.9.8 type-safe routes (`@Serializable object Home`) | Compile-time route safety; extends to child/badge screens |
| Deployment target | Local debug APK on emulator/device | No backend; documented Gradle commands substitute for cloud deploy |
| Directory layout | `se.simmarken/{data,domain,ui,navigation,di}` feature-aligned packages | Matches ARCHITECTURE.md; isolates persistence from UI |
| Progress model | Two tables: `RequirementProgress` + `BadgeProgress` (D-01) | Separates skill achievement from physical pin purchase |
| Badge visual state | Derived at read time (D-05) — never stored in DB (D-02) | Prevents stale state per PITFALLS.md |
| i18n data shape | Bilingual DB columns (`nameSv`/`nameEn`, `textSv`/`textEn`) | Supports SV+EN catalog text without translation table in v1 |
| Migrations | Room schema export v1 (`app/schemas/.../1.json`); no runtime migrations yet | Safe future schema bumps |

## Stack Touched in Phase 1

- [x] Project scaffold (Gradle KTS, version catalog, KSP, Room plugin, Compose, lint via assemble)
- [x] Routing — type-safe `Home` route via `SimmarkenNavHost`
- [x] Database — disk-backed Room with 7 entities; DAO insert + observe; persistence test across close/reopen
- [x] UI — **Add test kid** button wired Home → ViewModel → KidRepository → KidDao → SQLite
- [x] Local full-stack run — documented commands below (no cloud deploy)

## Local Run Commands

```bash
# Build debug APK
./gradlew :app:assembleDebug

# Install on connected emulator/device
./gradlew :app:installDebug

# Instrumented persistence suite (requires emulator/device running)
./gradlew :app:connectedDebugAndroidTest

# Targeted DATA-01 proofs
./gradlew :app:connectedDebugAndroidTest --tests "se.simmarken.data.local.DatabasePersistenceTest"
./gradlew :app:connectedDebugAndroidTest --tests "se.simmarken.data.local.DaoInstrumentedTest"
```

**Manual smoke:** Launch app → tap **Add test kid** → note count → force-stop app → relaunch → count unchanged.

## Out of Scope (Deferred to Later Slices)

- Catalog seeding (Svensk Simidrott + SLS) — Phase 2
- Production kid CRUD UI (FAB, avatar picker) — Phase 3
- `BadgeStateCalculator` and 4-tier visuals — Phase 4
- Requirement toggle + D-03 achievement write-once logic in repository — Phase 5
- JSON export/import — Phase 6
- Swedish/English UI strings (`strings.xml`) — Phase 6 (skeleton uses minimal hardcoded debug strings)
- Hilt dependency injection
- Room 3.0 (`androidx.room3`) migration
- Network permissions, cloud sync, accounts

## Entity Contract (Locked for Downstream Phases)

```
Catalog 1──* Category 1──* Badge 1──* Requirement
Kid 1──* RequirementProgress (kidId + requirementId → isAchieved, achievedAt)
Kid 1──* BadgeProgress (kidId + badgeId → isGotten, achievedAt [write-once], gottenAt)
```

## Subsequent Slice Plan

Each later phase adds one vertical slice on top of this skeleton without altering its architectural decisions:

- **Phase 2:** Seed both catalogs into empty Room tables from bundled JSON assets
- **Phase 3:** Real child profile CRUD and kid-first home navigation
- **Phase 4:** Category-grouped badge grid with derived visual states
- **Phase 5:** Requirement checklist toggle + purchase flag + D-03 achievement rules in ProgressRepository
- **Phase 6:** Versioned JSON backup and bilingual UI strings
