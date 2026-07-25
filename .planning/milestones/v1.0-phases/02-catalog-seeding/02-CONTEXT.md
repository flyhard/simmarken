# Phase 2: Catalog Seeding - Context

**Gathered:** 2026-07-22
**Status:** Ready for planning

<domain>
## Phase Boundary

Both Svensk Simidrott and SLS badge catalogs pre-loaded in Room on first run — categories, badges, official requirements, and visual identifiers. Phase 2 delivers seed data extraction, JSON asset format, seed loader, and image bundling strategy. No UI screens, child profiles, or progress tracking yet.

</domain>

<decisions>
## Implementation Decisions

### Badge Visuals
- **D-01:** Bundle official pin images as the default badge visual — not color-coded placeholders.
- **D-02:** Source artwork per catalog from best available official material — affisch PDFs for Svensk Simidrott, whatever SLS publishes publicly.
- **D-03:** Document image licensing and sources in repo docs (`LICENSE` / `SOURCES.md`); do not add schema fields for provenance.
- **D-04:** Missing-image fallback: tier/category color background with badge name or short code (Claude discretion on exact rendering).

### Catalog Coverage
- **D-05:** Seed core progression coverage — Vattenvana through Guld for Simidrott plus main SLS badges (not full Magister-tier affisch in v1).
- **D-06:** Simidrott deeper than SLS — full core Simidrott tiers; thinner SLS coverage acceptable when sources are sparse.
- **D-07:** Categories mirror official affisch tier names exactly (Vattenvana, Nybörjare, Järn, Brons, Silver, Guld, etc.) with `nameSv`/`nameEn` on `CategoryEntity`.
- **D-08:** `sortOrder` within each category follows official affisch progression sequence (e.g., Baddaren before Hajen).

### Requirement Wording
- **D-09:** Swedish requirement text (`textSv`) is verbatim from official PDF protocols — no parent-friendly summarization.
- **D-10:** English text (`textEn`) via machine-assisted translation with human review on sampled badges.
- **D-11:** Requirement granularity matches affisch layout — same bullet count and structure as official material for verifiability.
- **D-12:** All seeded badges manually verified against official sources before ship (not just Baddaren/Hajen/SLS basics samples).

### Seed Update Behavior
- **D-13:** Merge strategy on catalog update — add new badges, update changed requirements, preserve kid progress (not wipe-and-reseed).
- **D-14:** Trigger merge when bundled `catalogVersion` > stored version for that catalog (explicit version check on app launch).
- **D-15:** Progress preservation via stable requirement `code` field — remap progress across text changes; match by code, not by row id.
- **D-16:** Source provenance (URLs, extraction date) documented in repo docs only; `catalogVersion` on `CatalogEntity` is the runtime version marker.

### Claude's Discretion
- Image format and resolution — WebP in `assets/` at sizes suitable for grid and detail views (per ARCHITECTURE.md guidance)
- Exact merge algorithm for catalog updates (upsert by stable `code` keys across catalog/category/badge/requirement)
- SLS badge subset selection within "main SLS badges" when source documentation is incomplete
- Fallback placeholder visual design (tier color + badge name/code)

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project scope & requirements
- `.planning/PROJECT.md` — Both catalogs in v1, official public sources, clean minimal UI
- `.planning/REQUIREMENTS.md` — CATA-01 through CATA-05 (pre-loaded catalogs, categories, requirements, images)
- `.planning/ROADMAP.md` — Phase 2 goal, success criteria, plan breakdown (02-01 through 02-04)

### Architecture & stack research
- `.planning/research/ARCHITECTURE.md` — JSON seed → Room on first launch, `data/seed/` package, WebP image guidance
- `.planning/research/STACK.md` — Coil for image loading, kotlinx-serialization for JSON
- `.planning/research/PITFALLS.md` — Official PDF sources, catalogVersion metadata, image copyright, requirement accuracy
- `.planning/research/FEATURES.md` — Catalog seeding as P1 high-complexity feature
- `.planning/research/SUMMARY.md` — Phase 2 risks and research priorities

### Prior phase decisions
- `.planning/phases/01-android-foundation-database/01-CONTEXT.md` — Entity schema (CatalogEntity.catalogVersion, BadgeEntity.imageAssetPath, bilingual name/text fields)

### Official data sources (for extraction plans)
- [Svensk Simidrott simmärken](https://svensksimidrott.se/simkunnighet/simmarken) — catalog structure
- [Simmärkesmaterial PDFs](https://svensksimidrott.se/simkunnighet/simmarken/simmarkesmaterial) — requirement protocols and affisch artwork

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `CatalogEntity`, `CategoryEntity`, `BadgeEntity`, `RequirementEntity` — schema ready with `code`, `nameSv`/`nameEn`, `catalogVersion`, `imageAssetPath`, `sortOrder`
- `CatalogRepository` / `CatalogRepositoryImpl` — upsert methods for all catalog entities
- `CatalogDao` — query and insert infrastructure for catalog hierarchy
- `AppDatabase` — Room database with all catalog tables

### Established Patterns
- MVVM + Repository with Room as single source of truth
- Feature-based package structure under `se.simmarken/` — seed loader belongs in `data/seed/`
- Bilingual fields on entities (`nameSv`/`nameEn`, `textSv`/`textEn`) — seed JSON must populate both

### Integration Points
- Seed loader runs at app startup (via `AppContainer` or `Application` class) before UI needs catalog data
- Phase 3+ reads catalog via `CatalogRepository` observe methods — no UI in this phase
- `imageAssetPath` on `BadgeEntity` points to bundled asset paths for Coil loading in later phases

</code_context>

<specifics>
## Specific Ideas

- User wants official pin images despite licensing risk — document sources thoroughly
- Simidrott affisch is the authority for category names, badge order, and requirement bullets
- All badges verified, not just roadmap sample badges (Baddaren, Hajen, SLS basics) — samples remain as acceptance test cases
- Merge-on-update protects kid progress when affisch changes in future app versions

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.

</deferred>

---

*Phase: 2-Catalog Seeding*
*Context gathered: 2026-07-22*
