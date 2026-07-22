# Phase 1: Android Foundation & Database - Context

**Gathered:** 2026-07-22
**Status:** Ready for planning

<domain>
## Phase Boundary

Runnable Android app with Room database schema matching the relational catalog model: Catalog → Category → Badge → Requirement hierarchy, Kid profiles, and progress persistence. Phase 1 delivers the persistence layer and app skeleton — no catalog seeding, UI screens, or progress toggling yet.

</domain>

<decisions>
## Implementation Decisions

### Progress Data Model
- **D-01:** Use two progress tables — `RequirementProgress` (per kid + requirement) and `BadgeProgress` (per kid + badge). Skill completion and physical pin purchase are separate concerns.
- **D-02:** `BadgeProgress` stores `isGotten` (boolean), `achievedAt` (timestamp), and `gottenAt` (timestamp). Do not store derived visual state enums (LOCKED / IN_PROGRESS / ACHIEVED_TO_BUY / GOTTEN).
- **D-03:** Write `achievedAt` on `BadgeProgress` when the last requirement is checked — set once at achievement moment, never auto-cleared if requirements are later unchecked.
- **D-04:** `RequirementProgress` stores `isAchieved` (boolean) and `achievedAt` (timestamp). Support bidirectional toggle (check and uncheck) from day one.
- **D-05:** Derive badge visual state at read time from requirement progress + `isGotten`. Centralize computation in domain layer (BadgeStateCalculator in later phases).

### Claude's Discretion
- App shell at launch (placeholder vs minimal nav scaffold) — follow ARCHITECTURE.md recommended structure
- Migration posture, min SDK, DI approach, catalog i18n column design — follow STACK.md and ARCHITECTURE.md defaults unless research flags a conflict
- Package name `se.simmarken` per architecture research unless build tooling requires adjustment

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project scope & requirements
- `.planning/PROJECT.md` — Core value, constraints (Kotlin + Compose + Room + MVVM, offline-only)
- `.planning/REQUIREMENTS.md` — DATA-01: local persistence via Room with no network dependency
- `.planning/ROADMAP.md` — Phase 1 goal, success criteria, and plan breakdown

### Architecture & stack research
- `.planning/research/ARCHITECTURE.md` — MVVM layers, entity relationships, derived badge state pattern, project structure
- `.planning/research/STACK.md` — Kotlin 2.0+, Room 2.6+ with KSP, Compose BOM, recommended dependencies
- `.planning/research/PITFALLS.md` — Anti-patterns: never store derived badge state, separate achieved vs gotten
- `.planning/research/SUMMARY.md` — Executive summary and phase ordering rationale

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- None — greenfield project. No Android source code exists yet.

### Established Patterns
- Research prescribes MVVM + Repository with Room as single source of truth
- UI observes `Flow` from DAOs via ViewModel `StateFlow`
- Feature-based package structure under `se.simmarken/` (data / domain / ui / navigation)

### Integration Points
- Phase 2 will seed Catalog → Category → Badge → Requirement into tables defined here
- Phase 3+ will CRUD Kid entities and navigate from home
- Phase 5 will write to RequirementProgress and BadgeProgress using schema locked in this phase

</code_context>

<specifics>
## Specific Ideas

- Parent explicitly wants achievement date cached on BadgeProgress (not just derived at query time)
- Purchase timestamp (`gottenAt`) stored alongside `isGotten` for audit/history
- Bidirectional requirement toggle is non-negotiable — schema must not assume one-way completion

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope. Unselected gray areas (catalog i18n in DB, migration posture, app shell) deferred to research defaults and planner discretion.

</deferred>

---

*Phase: 1-Android Foundation & Database*
*Context gathered: 2026-07-22*
