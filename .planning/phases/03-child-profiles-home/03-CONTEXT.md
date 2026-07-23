# Phase 3: Child Profiles & Home - Context

**Gathered:** 2026-07-23
**Status:** Ready for planning

<domain>
## Phase Boundary

Parent can manage children and see them on a kid-first home screen. Phase 3 delivers Kid CRUD (add, edit, delete), child cards on home, FAB to add a child, and tap-through navigation to a catalog view (placeholder acceptable until Phase 4). No badge grid visuals, progress toggling, or summary counts yet.

</domain>

<decisions>
## Implementation Decisions

### Home Card Layout
- **D-01:** Single-column vertical list — one full-width card per child for easy one-handed tapping at the swim hall.
- **D-02:** Card content shows child name plus a colored circle avatar with initials. No progress summary counts on cards (deferred to Phase 5 / PROG-05).
- **D-03:** Empty state shows a centered message (e.g., "Add your first child") with the FAB always visible.
- **D-04:** Children ordered by creation order — first added stays on top. Use existing `sortOrder` / `createdAtEpochMillis` on `KidEntity`; no manual drag-to-reorder in v1.

### Add/Edit Child Flow
- **D-05:** Add and edit use a Material bottom sheet — slides up from the FAB (add) or overflow menu (edit), dismissible without navigation.
- **D-06:** Name is required: trim whitespace, reject empty/whitespace-only, cap at ~30 characters.
- **D-07:** Visual identity = initials on a colored circle. Default color derived from name hash; parent can override via a preset color swatch grid (8–12 tappable circles). Store chosen color in `avatarColorArgb` — no avatar image field.
- **D-08:** Edit and delete accessed via overflow menu (⋮) on each child card. Edit reuses the same bottom sheet form. Delete requires a confirmation dialog before removal.

### Navigation
- **D-09:** Tapping a child card navigates to that child's catalog view route. A placeholder screen is acceptable until Phase 4 ships the badge grid.

### Claude's Discretion
- Exact preset swatch palette and hash-to-color algorithm
- Delete confirmation dialog copy and destructive-action styling
- Catalog placeholder screen content (minimal "badges coming" vs child name header only)
- Bottom sheet peek height, keyboard handling, and inline validation error text
- Hardcoded UI strings in Swedish until Phase 6 i18n (primary user locale)

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project scope & requirements
- `.planning/PROJECT.md` — Kid-first navigation, clean minimal parent UI, one-handed swim-hall use
- `.planning/REQUIREMENTS.md` — KIDS-01 through KIDS-04, UI-01
- `.planning/ROADMAP.md` — Phase 3 goal, success criteria, plan breakdown (03-01 through 03-03)

### Architecture & stack research
- `.planning/research/ARCHITECTURE.md` — HomeScreen → ChildCatalogScreen navigation, MVVM + Repository pattern, feature-based `ui/home/` and `ui/child/` packages
- `.planning/research/STACK.md` — Jetpack Compose Material 3, Navigation Compose type-safe routes
- `.planning/research/PITFALLS.md` — UI observes Room Flows; no authoritative state in Composables

### Prior phase decisions
- `.planning/phases/01-android-foundation-database/01-CONTEXT.md` — `KidEntity` schema (`name`, `avatarColorArgb`, `sortOrder`, `createdAtEpochMillis`), MVVM + KidRepository pattern
- `.planning/phases/02-catalog-seeding/02-CONTEXT.md` — Catalog data ready in Room; Phase 3 reads via repositories, no seed work

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `KidEntity` / `KidDao` / `KidRepository` — schema and observe/upsert already wired; extend with `delete` for KIDS-04
- `HomeScreen` + `HomeViewModel` — placeholder with live kid count and test insert; replace with card list + FAB
- `SimmarkenNavHost` + type-safe `Home` route — add child catalog route with `kidId` argument
- `HomeViewModelFactory` — manual DI pattern for ViewModels without Hilt
- `AppContainer.kidRepository` — DI entry point for home and kid form ViewModels

### Established Patterns
- MVVM: Composable observes `StateFlow` via `collectAsStateWithLifecycle`
- Repository interface in `domain/repository/`; writes on `Dispatchers.IO` in ViewModel
- UI depends on repository interfaces only — no DAO access from Composables
- Material 3 theme in `ui/theme/` (`BluePrimary`, etc.) — extend with kid avatar swatch palette

### Integration Points
- Replace `addTestKid()` debug button with production FAB + bottom sheet flow
- New navigation destination: child catalog (placeholder) receives `kidId` from card tap
- Phase 4 replaces catalog placeholder with category-grouped badge grid
- Phase 5 adds progress summary counts to child cards (D-02 explicitly excludes them now)

</code_context>

<specifics>
## Specific Ideas

- Swim-hall context drives layout: single column for thumb-reachable full-width cards
- Initials avatar gives quick visual identification among siblings without photo uploads
- Overflow menu keeps primary tap target (open catalog) separate from destructive/edit actions

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope. Unselected gray areas (standalone avatar/color and edit/delete discussions) were partially resolved via add/edit flow choices.

</deferred>

---

*Phase: 3-Child Profiles & Home*
*Context gathered: 2026-07-23*
