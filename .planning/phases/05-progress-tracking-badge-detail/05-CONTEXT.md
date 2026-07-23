# Phase 5: Progress Tracking & Badge Detail - Context

**Gathered:** 2026-07-23
**Status:** Ready for planning

<domain>
## Phase Boundary

Replace the badge detail placeholder with an interactive requirement checklist and purchase toggle. Parents check/uncheck individual skills per child, badge visual state updates automatically via `BadgeStateCalculator`, and home child cards show summary counts of badges in progress and badges to buy. Phase 5 delivers `ProgressRepository` write paths from UI, full `BadgeDetailScreen`, and home-card aggregates (PROG-01 through PROG-05, UI-03). No JSON export/import or full i18n yet (Phase 6).

</domain>

<decisions>
## Implementation Decisions

### Requirement Checklist (Detail Screen)
- **D-01:** Pin fixed at top — large `BadgePinVisual` stays visible; requirement checklist scrolls below.
- **D-02:** Each requirement row = Material `Checkbox` + full `textSv` requirement text; tap anywhere on the row toggles the checkbox.
- **D-03:** Show `"X av Y klara"` progress subtitle below the badge name (above the checklist).
- **D-04:** Zero-requirement badges (e.g. Droppen): no checklist — show short note (e.g. seed `textSv` / "Inga kunskapskrav") and purchase toggle only; badge is already `ACHIEVED_TO_BUY` per `BadgeStateCalculator`.

### Purchase Toggle
- **D-05:** Purchase control below the checklist — labeled row `"Fysiskt märke köpt"` with Material `Switch`.
- **D-06:** Switch disabled until all requirements are achieved (`ACHIEVED_TO_BUY` state). Zero-requirement badges: toggle is the only interactive control and is enabled immediately.
- **D-07:** Marking köpt is reversible, but un-marking köpt requires a confirmation dialog before clearing `isGotten`.
- **D-08:** On mark köpt: set `BadgeProgressEntity.isGotten = true` and `gottenAtEpochMillis`. On un-mark (after confirm): clear both.

### Unchecking Rules
- **D-09:** Unchecking a skill when badge was `ACHIEVED_TO_BUY` reverts visual state to `IN_PROGRESS`; `achievedAtEpochMillis` on `BadgeProgressEntity` is **not** cleared (Phase 1 D-03 write-once).
- **D-10:** Unchecking any skill while badge is `GOTTEN` auto-clears `isGotten` (and `gottenAtEpochMillis`) without an extra dialog — distinct from explicit köpt toggle undo (D-07).
- **D-11:** Unchecking the last remaining skill returns badge to `LOCKED` (0 achieved → grayscale, no ring) per Phase 4 D-05/D-07.
- **D-12:** Requirement uncheck/check is instant — no confirmation dialog on individual skill toggles.

### Home Card Summaries (PROG-05)
- **D-13:** Two subtitle lines under child name on `ChildCard` — e.g. `"2 pågår"` and `"1 att köpa"` (separate lines, not inline).
- **D-14:** Hide summary lines when count is zero — only show lines with count > 0.
- **D-15:** Counts aggregate across **both** catalogs (Simidrott + SLS combined) per child.
- **D-16:** Do not show köpta/gotten count on home cards — only `IN_PROGRESS` and `ACHIEVED_TO_BUY` counts.

### Progress Writes & State (carried forward + locked here)
- **D-17:** All visual state derived via `BadgeStateCalculator.compute` — never persist enum in DB (Phase 4 D-29).
- **D-18:** Set `BadgeProgressEntity.achievedAtEpochMillis` when the **last** requirement transitions to achieved (write-once per Phase 1 D-03); do not clear on later uncheck.
- **D-19:** Requirement toggles write `RequirementProgressEntity` via `ProgressRepository.upsertRequirementProgress` on `Dispatchers.IO` from ViewModel.
- **D-20:** Home and catalog grid must use the same `BadgeStateCalculator` path as detail — no duplicate state logic (Pitfall 3).

### Claude's Discretion
- Exact Swedish copy for zero-requirement note, köpt confirmation dialog, and summary subtitle strings (hardcoded until Phase 6 i18n)
- Scroll container structure (`Column` + `verticalScroll` vs `LazyColumn` for checklist)
- Whether to animate checkbox/state transitions on toggle
- `ProgressRepository` helper methods vs inline upsert logic in ViewModel
- Home summary computation location (HomeViewModel combine vs dedicated domain aggregator)
- Disabled purchase switch styling (greyed label vs hidden until enabled)

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project scope & requirements
- `.planning/PROJECT.md` — Core value: passed vs bought pin; swim-hall one-handed use
- `.planning/REQUIREMENTS.md` — UI-03, PROG-01, PROG-02, PROG-03, PROG-05
- `.planning/ROADMAP.md` — Phase 5 goal, success criteria, plan breakdown (05-01 through 05-03)

### Architecture & pitfalls
- `.planning/research/ARCHITECTURE.md` — Requirement check-off data flow, `ProgressRepository`, derived badge state
- `.planning/research/PITFALLS.md` — Pitfall 2: separate achieved vs gotten; Pitfall 3: single `BadgeStateCalculator`
- `.planning/research/STACK.md` — Jetpack Compose Material 3, Room Flow, MVVM

### Prior phase decisions
- `.planning/phases/01-android-foundation-database/01-CONTEXT.md` — `RequirementProgress` + `BadgeProgress` tables; `achievedAt` write-once; bidirectional toggle
- `.planning/phases/03-child-profiles-home/03-CONTEXT.md` — `ChildCard` layout; no summary counts until Phase 5
- `.planning/phases/04-catalog-view-visual-states/04-CONTEXT.md` — `BadgeStateCalculator` D-29/D-30; detail route; `BadgePinVisual`; locked/in-progress rules D-05–D-08
- `.planning/phases/04-catalog-view-visual-states/04-UI-SPEC.md` — Spacing, typography, pin sizes for grid vs detail (extend for checklist rows in Phase 5 UI-SPEC)

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `BadgeDetailPlaceholderScreen` / `BadgeDetailViewModel` — replace placeholder; ViewModel already combines catalog + progress flows and uses `BadgeCatalogMapper`
- `BadgeDetailUiState` — extend with `requirements: List<RequirementRowUiModel>`, `isGotten`, purchase toggle enabled flag
- `ProgressRepository` — `observeRequirementProgress`, `observeBadgeProgress`, `upsertRequirementProgress`, `upsertBadgeProgress` (writes stubbed for Phase 5)
- `BadgeStateCalculator` — single source for visual state + `progressFraction`
- `BadgePinVisual` + `BadgePinSize.Detail` — pin at top of detail screen
- `ChildCard` — add optional subtitle lines for summary counts
- `HomeViewModel` / `HomeScreen` — wire per-kid aggregate counts
- `RequirementEntity.textSv` / `textEn` — checklist label source (Swedish hardcoded in Phase 5)

### Established Patterns
- MVVM: Composable observes `StateFlow` via `collectAsStateWithLifecycle`
- Room as single source of truth — UI recomposes on Flow emission after upsert
- Manual ViewModel factories (`BadgeDetailViewModelFactory`) — follow for extended ViewModels
- `BadgeCatalogMapper.toBadgeCellUiModel` — reuse mapping logic for consistent state across grid and detail
- Writes on `viewModelScope` + `Dispatchers.IO` via repository

### Integration Points
- `SimmarkenNavHost` — swap `BadgeDetailPlaceholderScreen` for full `BadgeDetailScreen` (same route)
- `ChildCatalogScreen` grid auto-updates when returning from detail (shared Room Flows)
- Phase 6 adds i18n for checklist labels, toggle copy, and summary strings
- Phase 6 export/import serializes `RequirementProgress` + `BadgeProgress` rows written here

</code_context>

<specifics>
## Specific Ideas

- Swim-hall speed: instant requirement toggle, no confirmation except köpt undo
- Purchase toggle gated until skills complete — matches real-world "passed then buy pin" flow
- Home cards answer "what needs attention?" — pågår + att köpa only, hide zeros for clean new-kid cards
- Zero-requirement badges (Droppen): purchase-only detail, no empty checklist

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.

</deferred>

---

*Phase: 5-Progress Tracking & Badge Detail*
*Context gathered: 2026-07-23*
