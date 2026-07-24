# Phase 4: Catalog View & Visual States - Context

**Gathered:** 2026-07-23
**Status:** Ready for planning

<domain>
## Phase Boundary

Replace the child catalog placeholder with a category-grouped badge grid showing 4-tier visual states (locked → in progress → achieved-to-buy → gotten). Phase 4 delivers `BadgeStateCalculator` domain logic, catalog tab navigation (Simidrott / SLS), grid UI with state overlays, and tap-through to a badge detail placeholder. No interactive requirement checklist, purchase toggle, or home summary counts yet (Phase 5).

</domain>

<decisions>
## Implementation Decisions

### Dual-Catalog Navigation
- **D-01:** Tab row below top bar — "Simidrott" | "SLS"; one catalog visible at a time.
- **D-02:** Default to Svensk Simidrott tab when opening a child's catalog screen.
- **D-03:** Short Swedish tab labels: "Simidrott" | "SLS" (full i18n in Phase 6).
- **D-04:** Tab selection is session-only — reset to Simidrott default each time the child catalog screen is opened (no per-child persistence).

### Locked Badge Rules
- **D-05:** Locked = zero requirements checked for that badge.
- **D-06:** All badges always visible in the grid — locked badges shown grayscale, never hidden.
- **D-07:** First requirement checked → badge immediately leaves locked and becomes in progress (partial ring).
- **D-08:** Categories are independent — no tier gating; parent may start badges in any category without completing prior tiers.

### In-Progress Visual
- **D-09:** Circular progress ring around the pin image, filled proportionally by requirement count (achieved / total).
- **D-10:** Progress ring uses theme primary color.
- **D-11:** Pin image stays grayscale while in progress — only the ring signals partial completion.
- **D-12:** All requirements met → full-color pin, cart overlay, progress ring removed (achieved-to-buy state).

### Badge Grid Layout
- **D-13:** 3 columns on a typical phone — larger pins for swim-hall glances.
- **D-14:** Badge name shown below each pin, truncated to one line.
- **D-15:** Comfortable spacing — ~12–16dp gaps between cells for thumb-friendly taps.
- **D-16:** Square tile cells with pin image centered.

### Category Headers
- **D-17:** Always expanded — no collapsible category sections.
- **D-18:** Sticky category headers while scrolling within the active catalog tab.
- **D-19:** Swedish category names only in Phase 4 — use `nameSv` from seed (`nameEn` in Phase 6).
- **D-20:** Category order follows official affisch `sortOrder` from seed (Phase 2 D-08).

### State Overlays
- **D-21:** Achieved-to-buy: shopping cart icon overlay, bottom-right corner, on full-color pin.
- **D-22:** Gotten: checkmark icon overlay, bottom-right corner, on full-color pin.
- **D-23:** Locked: grayscale pin only — no lock icon or dim scrim.
- **D-24:** Cart and checkmark overlays use a small filled circle badge with white icon (bottom-right) for readability on pin art.

### Badge Detail Placeholder
- **D-25:** Detail screen shows large pin image, badge name, and stub text "Checklista kommer snart" — confirms tap target before Phase 5 checklist.
- **D-26:** New navigation route `BadgeDetail(kidId, badgeId)` pushed on the stack from grid tap.
- **D-27:** View only in Phase 4 — no requirement checklist toggles until Phase 5.
- **D-28:** Detail screen respects the same 4-tier visuals as the grid (grayscale, ring, cart, checkmark).

### Badge State Computation (carried forward + locked here)
- **D-29:** Derive visual state at read time via centralized `BadgeStateCalculator` — never persist enum in DB (Phase 1 D-05).
- **D-30:** State precedence: `isGotten` → GOTTEN; else all requirements achieved → ACHIEVED_TO_BUY; else any requirement achieved → IN_PROGRESS; else → LOCKED.

### Claude's Discretion
- Empty-catalog tab behavior if a catalog has zero badges (both tabs remain visible with empty state)
- Progress ring stroke width, cap style, and whether to animate on change
- Exact grayscale `ColorFilter` / saturation approach for locked and in-progress pins
- Coil image request sizes for grid vs detail; tier-color fallback when `imageAssetPath` is null (Phase 2 D-04)
- Lazy list structure for sticky headers + per-category grids (single `LazyColumn` with sticky header items vs nested scroll)
- Hardcoded Swedish UI strings until Phase 6 i18n

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project scope & requirements
- `.planning/PROJECT.md` — 4-tier badge visual states, clean minimal parent UI, swim-hall one-handed use
- `.planning/REQUIREMENTS.md` — UI-02, UI-04, PROG-04
- `.planning/ROADMAP.md` — Phase 4 goal, success criteria, plan breakdown (04-01 through 04-03)

### Architecture & stack research
- `.planning/research/ARCHITECTURE.md` — `BadgeVisualState` enum, derived state pattern, `ChildCatalogScreen` / `BadgeDetailScreen` structure, Coil for images
- `.planning/research/PITFALLS.md` — Pitfall 3: single `BadgeStateCalculator` across screens; Pitfall 2: separate achieved vs gotten
- `.planning/research/STACK.md` — Jetpack Compose Material 3, Coil, Navigation Compose type-safe routes

### Prior phase decisions
- `.planning/phases/01-android-foundation-database/01-CONTEXT.md` — `RequirementProgress` + `BadgeProgress` tables; derive state at read time; `isGotten` separate from skill completion
- `.planning/phases/02-catalog-seeding/02-CONTEXT.md` — Bundled WebP pin images, `sortOrder` progression, tier-color fallback for missing images
- `.planning/phases/03-child-profiles-home/03-CONTEXT.md` — Replace `ChildCatalogPlaceholderScreen`; child name in top bar; no home-card progress counts yet

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `ChildCatalogPlaceholderScreen` / `ChildCatalogViewModel` — replace placeholder; ViewModel already loads child name by `kidId`
- `ChildCatalog` route in `Routes.kt` — extend NavHost with `BadgeDetail(kidId, badgeId)`
- `CatalogRepository` — `observeCatalogs()`, `observeCategories(catalogId)`, `observeBadges(categoryId)`
- `ProgressRepository` — `observeRequirementProgress(kidId)`, `observeBadgeProgress(kidId)` for state derivation (read-only in Phase 4)
- `CatalogDao` — ordered category/badge queries by `sortOrder`
- `BadgeEntity.imageAssetPath` — Coil loads bundled WebP assets from Phase 2
- `BadgeProgressEntity.isGotten`, `RequirementProgressEntity.isAchieved` — inputs to `BadgeStateCalculator`
- Material 3 theme (`BluePrimary`, etc.) — primary color for progress ring

### Established Patterns
- MVVM: Composable observes `StateFlow` via `collectAsStateWithLifecycle`
- Manual ViewModel factories (`ChildCatalogViewModelFactory`) — follow for catalog and badge detail ViewModels
- `AppContainer` exposes `catalogRepository`, `progressRepository`, `kidRepository`
- Feature packages: `ui/child/` for catalog; add `ui/badge/` for detail placeholder per ARCHITECTURE.md
- ElevatedCard / Material 3 components on home — match catalog grid polish

### Integration Points
- `SimmarkenNavHost` — swap `ChildCatalogPlaceholderScreen` for real catalog screen; add `BadgeDetail` composable destination
- Phase 5 replaces detail stub with interactive checklist + purchase toggle using same route
- Phase 5 adds home summary counts — explicitly out of scope for Phase 4 grid
- `ProgressRepository` upsert methods exist but Phase 4 is display-only (no writes from UI)

</code_context>

<specifics>
## Specific Ideas

- Swim-hall context: 3-column grid with comfortable spacing and names under pins for quick identification
- Tab row keeps Simidrott (primary path) and SLS separate without cluttering one scroll
- Cart overlay answers the core value question at catalog glance: "passed but not bought yet"
- Grayscale + ring progression gives clear locked → started → ready-to-buy → done story without extra lock icons

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.

</deferred>

---

*Phase: 4-Catalog View & Visual States*
*Context gathered: 2026-07-23*
