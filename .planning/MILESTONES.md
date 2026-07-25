# Milestones

## v1.0 MVP (Shipped: 2026-07-25)

**Phases completed:** 6 phases, 21 plans, 57 tasks

**Key accomplishments:**

- Greenfield Android project at `se.simmarken` with Compose Navigation shell and compile-ready DATA-01 test stubs
- Room v1 schema with 7 entities, 4 DAOs, exported JSON, and catalog FK chain instrumented test
- Walking skeleton vertical slice: Compose Home → ViewModel → KidRepository → Room with restart persistence test
- Svensk Simidrott core progression seed (20 badges, verbatim affisch requirements) with PDF extraction tooling and golden accuracy tests
- SLS catalog seed (17 badges, shop-verified requirements) with extraction tooling and Droppen/Skräddaren golden tests
- Version-gated kotlinx-serialization seed loader with ID-preserving Room merge wired at app startup on IO dispatcher
- Official Simidrott and SLS pin WebP assets (32 files, 272 KB) with SOURCES.md licensing and tier-color fallback palette
- Room @Upsert replaces destructive REPLACE upserts so catalog merges preserve requirement IDs and all 8 seed instrumented tests pass
- FAB-driven add-child flow with initials avatars, 10-color swatch grid, Swedish validation, and Room persistence ordered by creation
- Kid-first home with full-width ElevatedCards, Swedish empty state, and type-safe navigation to a catalog placeholder showing the child's name
- Overflow-menu edit/delete on child cards with shared bottom sheet edit mode, field preservation, and destructive delete confirmation with Room cascade
- PROG-04 state derivation centralized in BadgeStateCalculator with full JVM test matrix, entity-to-UI mapper, and CatalogDao progress-indexing queries
- Coil-powered 4-tier BadgePinVisual with progress ring and cart/check overlays, plus BadgeDetail placeholder navigation reusing shared state calculation
- ViewModel progress writes with achievedAt write-once, gotten auto-clear, purchase flag API, and tracer checklist row
- Full BadgeDetailScreen with fixed pin header, scrollable checklist, zero-requirement note, and purchase toggle with köpt undo dialog
- Per-kid pågår and att köpa counts on home ChildCards, aggregated across both catalogs via KidProgressSummaryCalculator and BadgeCatalogMapper
- Versioned JSON backup with stableId identity, newer-wins merge, Settings export/import UI, and share-intent entry points.
- Bilingual string resources for all app chrome with StringsParityTest gate — catalog content stays Swedish per D-13
- Three-way language preference via DataStore and AppCompatDelegate per-app locales with Settings radio picker
- BadgeGridItem now mirrors BadgeDetailScreen — accessibility state labels resolve through string resources in both locales.

---
