# Phase 2: Catalog Seeding - Research

**Researched:** 2026-07-22
**Domain:** Android offline catalog seeding (JSON assets → Room), Swedish swimming badge data extraction
**Confidence:** MEDIUM-HIGH (architecture/stack HIGH; SLS requirement sourcing MEDIUM)

## Summary

Phase 2 delivers two pre-loaded badge catalogs (Svensk Simidrott + SLS) into Room from bundled JSON assets and WebP images, with a version-aware merge loader that runs at app startup. Phase 1 already provides the entity schema (`code`, bilingual fields, `catalogVersion`, `imageAssetPath`, `sortOrder`) and `CatalogRepository` upsert pass-through — but **the current `CatalogDao` `@Insert(REPLACE)` only conflicts on auto-generated primary keys**, so merge-by-`code` requires new lookup queries and ID-preserving upserts before progress FKs can survive catalog updates.

Svensk Simidrott has authoritative, downloadable sources: the simmärkesaffisch PDF (structure + artwork + back-page requirements) and per-group protocol PDFs (Baddaren, Hajen, etc.) on the simmärkesmaterial page, last updated **2026-03-02** [CITED: svensksimidrott.se/simmarkesmaterial]. SLS has thinner official requirement documentation — product names and progression are visible on the public shop, but verbatim requirement bullets are harder to source than Simidrott's protocol PDFs [CITED: shop listing via web search; official simmarken page fetch timed out]. Plan Simidrott depth per D-05/D-06; treat SLS as a thinner subset with tier-color fallbacks where official text is missing.

**Primary recommendation:** Build `data/seed/` with kotlinx-serialization DTOs + `CatalogSeedLoader` (IO dispatcher, transactional merge by stable `code` keys preserving row IDs), extract Simidrott from affisch/protocol PDFs into `assets/seed/simidrott.json` + `assets/badges/simidrott/*.webp`, seed a curated SLS progression into `assets/seed/sls.json`, and gate updates on `catalogVersion` string comparison with provenance in `docs/SOURCES.md` (not DB fields).

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

#### Badge Visuals
- **D-01:** Bundle official pin images as the default badge visual — not color-coded placeholders.
- **D-02:** Source artwork per catalog from best available official material — affisch PDFs for Svensk Simidrott, whatever SLS publishes publicly.
- **D-03:** Document image licensing and sources in repo docs (`LICENSE` / `SOURCES.md`); do not add schema fields for provenance.
- **D-04:** Missing-image fallback: tier/category color background with badge name or short code (Claude discretion on exact rendering).

#### Catalog Coverage
- **D-05:** Seed core progression coverage — Vattenvana through Guld for Simidrott plus main SLS badges (not full Magister-tier affisch in v1).
- **D-06:** Simidrott deeper than SLS — full core Simidrott tiers; thinner SLS coverage acceptable when sources are sparse.
- **D-07:** Categories mirror official affisch tier names exactly (Vattenvana, Nybörjare, Järn, Brons, Silver, Guld, etc.) with `nameSv`/`nameEn` on `CategoryEntity`.
- **D-08:** `sortOrder` within each category follows official affisch progression sequence (e.g., Baddaren before Hajen).

#### Requirement Wording
- **D-09:** Swedish requirement text (`textSv`) is verbatim from official PDF protocols — no parent-friendly summarization.
- **D-10:** English text (`textEn`) via machine-assisted translation with human review on sampled badges.
- **D-11:** Requirement granularity matches affisch layout — same bullet count and structure as official material for verifiability.
- **D-12:** All seeded badges manually verified against official sources before ship (not just Baddaren/Hajen/SLS basics samples).

#### Seed Update Behavior
- **D-13:** Merge strategy on catalog update — add new badges, update changed requirements, preserve kid progress (not wipe-and-reseed).
- **D-14:** Trigger merge when bundled `catalogVersion` > stored version for that catalog (explicit version check on app launch).
- **D-15:** Progress preservation via stable requirement `code` field — remap progress across text changes; match by code, not by row id.
- **D-16:** Source provenance (URLs, extraction date) documented in repo docs only; `catalogVersion` on `CatalogEntity` is the runtime version marker.

### Claude's Discretion

- Image format and resolution — WebP in `assets/` at sizes suitable for grid and detail views (per ARCHITECTURE.md guidance)
- Exact merge algorithm for catalog updates (upsert by stable `code` keys across catalog/category/badge/requirement)
- SLS badge subset selection within "main SLS badges" when source documentation is incomplete
- Fallback placeholder visual design (tier color + badge name/code)

### Deferred Ideas (OUT OF SCOPE)

None — discussion stayed within phase scope.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| CATA-01 | App ships with Svensk Simidrott badge catalog pre-loaded (categories, badges, requirements) | Simidrott affisch + protocol PDFs on simmärkesmaterial page; JSON `assets/seed/simidrott.json`; merge loader populates Room on first launch |
| CATA-02 | App ships with SLS badge catalog pre-loaded (categories, badges, requirements) | Curated SLS progression from shop/public listings; thinner coverage per D-06; `assets/seed/sls.json` |
| CATA-03 | Badges displayed grouped by category within each catalog | `CategoryEntity.sortOrder` + `BadgeEntity.sortOrder`; categories mirror affisch tier names (D-07); verified via DAO ordering queries in tests |
| CATA-04 | Each badge shows official skill requirements as checklist | `RequirementEntity` rows with verbatim `textSv` (D-09), `textEn` translation (D-10), affisch-matched bullets (D-11) |
| CATA-05 | Each badge displays an image or visual identifier | `imageAssetPath` → bundled WebP in `assets/badges/` (D-01); null path + tier-color fallback for gaps (D-04) — fallback UI in Phase 4, path population in Phase 2 |
</phase_requirements>

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Official badge/requirement data | Data extraction (build-time / maintainer) | — | Human-verified from PDFs; not runtime network |
| JSON seed files + WebP images | Database / Storage (assets) | — | Bundled in APK `assets/`; offline by design |
| Seed parse + version check | Data layer (`data/seed/`) | Application startup | kotlinx-serialization reads assets; compares `catalogVersion` |
| Merge into Room | Data layer (`CatalogSeedLoader` + DAO) | Domain (`CatalogRepository`) | IO + transactions; repository stays thin |
| Progress preservation on update | Data layer (ID-preserving upsert) | — | FK `requirementId`/`badgeId` must keep stable row IDs |
| Badge image display | Client UI (Phase 4) | Data (`imageAssetPath`) | Phase 2 sets paths; Coil loads in later phase |
| Licensing / provenance docs | Repo docs (`docs/SOURCES.md`) | — | D-16: not in DB schema |

## Standard Stack

### Core

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| kotlinx-serialization-json | 1.7.3 (in project) | Parse seed JSON from assets | Already in `app/build.gradle.kts`; official Kotlin JSON format [CITED: kotlinlang.org/docs/serialization.html] |
| Room | 2.8.4 (in project) | Persist catalog hierarchy | Phase 1 schema; transactional writes |
| Kotlin Coroutines | 1.9.0 (in project) | IO dispatcher for seed loader | Matches Phase 1 pattern (`Dispatchers.IO`) |
| Android AssetManager | platform | Read `assets/seed/*.json` and badge files | Standard offline asset bundling [ASSUMED: `context.assets.open()` — Android platform API] |

### Supporting

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| Coil Compose | 2.7+ [ASSUMED] | Load `imageAssetPath` in UI | **Phase 4 only** — not required for Phase 2 seeding |
| WebP encoder (cwebp / Android Studio) | — | Convert affisch crops to WebP | Build-time image pipeline in plans 02-01/02-04 |

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| JSON in assets | Hardcoded Kotlin objects | Violates ARCHITECTURE.md; blocks catalog updates without refactor |
| JSON in assets | SQLite prebuilt DB | Harder to diff/review; JSON matches extraction workflow |
| `@Insert(REPLACE)` on DAO | Lookup-by-code + ID-preserving upsert | REPLACE on PK only breaks merge; REPLACE deletes row breaking FKs [CITED: developer.android.com/reference/androidx/room/Upsert] |
| Room `@Upsert` alone | Lookup existing ID by `code` then `@Upsert` | Room `@Upsert` conflicts on **primary key** only [CITED: developer.android.com/reference/androidx/room/Upsert] — must set `id` from code lookup first |

**Installation:** No new runtime Gradle dependencies required for Phase 2 core. kotlinx-serialization already present.

**Version verification:** `kotlinx-serialization-json` 1.7.3 pinned in `gradle/libs.versions.toml` (project file, 2026-07-22).

## Package Legitimacy Audit

> slopcheck unavailable at research time — packages tagged `[ASSUMED]` where not already in project.

| Package | Registry | Age | Downloads | Source Repo | slopcheck | Disposition |
|---------|----------|-----|-----------|-------------|-----------|-------------|
| kotlinx-serialization-json | Maven Central | mature | very high | github.com/Kotlin/kotlinx.serialization | n/a | **Already in project** — approved |
| coil-compose | Maven Central | mature | very high | github.com/coil-kt/coil | n/a | **Deferred to Phase 4** — not installed in Phase 2 |

**Packages removed due to slopcheck [SLOP] verdict:** none (slopcheck not run)
**Packages flagged as suspicious [SUS]:** none

*slopcheck was unavailable; no new packages proposed for Phase 2 install.*

## Project Constraints (from .cursor/rules/)

- **Platform:** Native Android only — Kotlin + Jetpack Compose
- **Architecture:** MVVM + Room; offline-only, no network stack
- **Storage:** Local device; catalog via bundled assets, not remote fetch
- **i18n:** Bilingual `nameSv`/`nameEn` and `textSv`/`textEn` on entities
- **GSD workflow:** Phase work should flow through GSD commands (informational for planner)

## Architecture Patterns

### System Architecture Diagram

```
App launch (SimmarkenApplication.onCreate)
    │
    ▼
AppContainer ──► CatalogSeedLoader.seedIfNeeded()  [Dispatchers.IO]
    │                    │
    │                    ├─► Read assets/seed/{simidrott,sls}.json
    │                    ├─► Json.decodeFromString<CatalogSeedDto>()
    │                    ├─► For each catalog: compare catalogVersion
    │                    │       bundled > stored? ──no──► skip catalog
    │                    │              │
    │                    │             yes
    │                    │              ▼
    │                    └─► Transaction: merge by code (preserve IDs)
    │                              │
    │                              ├─► upsert Catalog (by code)
    │                              ├─► upsert Categories (catalogId + code)
    │                              ├─► upsert Badges (categoryId + code)
    │                              ├─► upsert Requirements (badgeId + code)
    │                              └─► prune removed badges/requirements (optional)
    │
    ▼
CatalogRepository.observe*() ◄── Room (source of truth)
    │
    └──► Phase 3+ UI reads catalog (not built in Phase 2)
```

### Recommended Project Structure

```
app/src/main/
├── assets/
│   ├── seed/
│   │   ├── simidrott.json
│   │   └── sls.json
│   └── badges/
│       ├── simidrott/          # e.g. baddaren_gron.webp
│       └── sls/                # e.g. droppen.webp
├── java/se/simmarken/data/seed/
│   ├── CatalogSeedDto.kt       # @Serializable JSON models
│   ├── CatalogSeedParser.kt    # assets → DTO
│   ├── CatalogSeedLoader.kt    # version check + merge orchestration
│   └── CatalogVersion.kt       # comparable version strings
docs/
├── SOURCES.md                  # URLs, extraction dates, image licensing notes
└── index.md                    # TechDocs update if substantive (TeamMoveIt N/A — not Java/Maven)
```

### Pattern 1: JSON Seed DTO → Entity Mapping

**What:** Serializable DTOs mirror JSON; loader maps to Room entities with parent IDs resolved after upsert.
**When to use:** All catalog data entry.
**Example:**

```kotlin
// Source: kotlinlang.org/docs/serialization.html
@Serializable
data class CatalogSeedDto(
    val code: String,
    val nameSv: String,
    val nameEn: String,
    val catalogVersion: String,
    val sortOrder: Int = 0,
    val categories: List<CategorySeedDto>,
)

@Serializable
data class CategorySeedDto(
    val code: String,
    val nameSv: String,
    val nameEn: String,
    val sortOrder: Int,
    val badges: List<BadgeSeedDto>,
)

@Serializable
data class BadgeSeedDto(
    val code: String,
    val nameSv: String,
    val nameEn: String,
    val sortOrder: Int,
    val imageAssetPath: String? = null, // e.g. "badges/simidrott/baddaren_gron.webp"
    val requirements: List<RequirementSeedDto>,
)

@Serializable
data class RequirementSeedDto(
    val code: String,
    val textSv: String,
    val textEn: String,
    val sortOrder: Int,
)
```

### Pattern 2: ID-Preserving Merge by Stable `code`

**What:** Before upsert, query existing row by natural key; copy existing `id` into entity so Room updates in place and progress FKs remain valid.
**When to use:** Every seed run when `catalogVersion` increased (D-13, D-15).
**Why not raw `@Insert(REPLACE)`:** SQLite REPLACE deletes the old row — cascades delete `RequirementProgress` / `BadgeProgress` [CITED: Room upsert guidance via developer.android.com/reference/androidx/room/Upsert].

```kotlin
// Pseudocode — planner implements in CatalogDao + CatalogSeedLoader
suspend fun mergeCategory(catalogId: Long, seed: CategorySeedDto) {
    val existing = catalogDao.findCategoryByCatalogAndCode(catalogId, seed.code)
    val entity = seed.toEntity(
        id = existing?.id ?: 0,
        catalogId = catalogId,
    )
    catalogDao.upsertCategory(entity) // @Upsert or @Insert with explicit id
    val categoryId = existing?.id ?: catalogDao.lastInsertId()
    seed.badges.forEach { mergeBadge(categoryId, it) }
}
```

**Progress preservation (D-15):** Progress rows key off `requirementId` (surrogate). Preserving `RequirementEntity.id` when `code` unchanged keeps progress attached even if `textSv`/`textEn` change. New requirements get new codes; removed codes → delete requirement rows (CASCADE prunes progress) — acceptable for deprecated skills.

### Pattern 3: Startup Integration

**What:** Fire-and-forget coroutine from `AppContainer` or `SimmarkenApplication` after DB build.
**When to use:** Every app launch (cheap no-op when versions match).

```kotlin
// Source: Phase 1 AppContainer pattern
class AppContainer(context: Context) {
    private val database = Room.databaseBuilder(/* ... */).build()
    private val catalogDao = database.catalogDao()

    init {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            CatalogSeedLoader(context, catalogDao).seedIfNeeded()
        }
    }
}
```

Phase 3+ should expose `seedState: Flow<SeedState>` if UI must wait; Phase 2 tests can `runBlocking { loader.seedIfNeeded() }` before assertions.

### Pattern 4: Stable `code` Naming Convention

| Level | Convention | Examples |
|-------|------------|----------|
| Catalog | lowercase org slug | `simidrott`, `sls` |
| Category | affisch tier slug | `vattenvana`, `nyborjare`, `jarn-brons-silver-guld` |
| Badge | kebab-case Swedish name | `baddaren-gron`, `hajen-silver`, `droppen` |
| Requirement | `{badge-code}-{nn}` | `baddaren-gron-01`, `hajen-silver-03` |

Use ASCII slugs (å→a) for codes; display names stay in `nameSv`/`nameEn`.

### Anti-Patterns to Avoid

- **Wipe-and-reseed on version bump:** Violates D-13; destroys kid progress in later phases.
- **Match progress by requirement text:** Text changes break linkage; use `code` only (D-15).
- **Trust secondary journalism for requirements:** GP article lists SLS skills but is not authoritative for D-09 verbatim wording.
- **Store provenance in Room:** Violates D-16; use `docs/SOURCES.md`.
- **Blocking main thread for seed:** ANR risk; always IO dispatcher.

## Official Data Sources

### Svensk Simidrott (HIGH confidence structure)

| Resource | URL | Content | Use |
|----------|-----|---------|-----|
| Simmärken overview | https://svensksimidrott.se/simkunnighet/simmarken | Affisch download link, shop | Catalog metadata |
| Simmärkesmaterial | https://svensksimidrott.se/simkunnighet/simmarken/simmarkesmaterial | Protocol PDFs per group; affisch | **Primary** for D-09/D-11 |
| Simmärkesaffisch 2024 PDF | https://svensksimidrott.se/download/18.2834ddaa18d0e8137e44ef74/1705584326390/simm%C3%A4rkesaffisch%202024.pdf | Full tier layout, artwork, requirements on reverse | Categories, sortOrder, images |
| 2026 affisch news | https://svensksimidrott.se/nyheter-svensk-simidrott/svensk-simidrott/2026-02-11-nu-ar-2026-ars-simmarkesaffischer-och-folder-har | Confirms annual affisch updates | `catalogVersion` dating |

**Page metadata:** Simmärkesmaterial last updated **2026-03-02** [CITED: svensksimidrott.se/simmarkesmaterial]. Use as `catalogVersion` basis (e.g. `2026.03.02`).

**Core progression scope (D-05) — include:**

| Category (affisch) | Badges (from protocol PDF groupings) |
|--------------------|--------------------------------------|
| Vattenvana | Baddaren Grön/Blå/Gul; Sköldpaddan; Bläckfisken |
| Nybörjare | Pingvinen Silver/Guld; Simsättsmärke 1–2; Silverfisken; Guldfisken |
| Hajen tier | Hajen Brons/Silver/Guld; Simsättsmärke 3–4 |
| Järn/Brons/Silver/Guld | Järnmärket (`jarnmarket`), Bronsmärket (`bronsmarket`), Silvermärket (`silvermarket`), Guldmärket (`guldmarket`), Kandidaten (`kandidaten`) — **RESOLVED:** Guld is the category tier; terminal badge is Kandidaten |

**Exclude (D-05):** Magistermärken, Vattenpolo, Simborgarmärken (annual), Simhalls special badges.

**Protocol PDF → badge mapping** [CITED: simmarkesmaterial page]:

- Baddaren PDF → Baddaren Grön, Blå, Gul
- Sköldpaddan PDF → Sköldpaddan, Bläckfisken
- Pingvinen PDF → Pingvinen Silver/Guld, Simsättsmärke 1–2
- Silverfisken / Guldfisken PDFs → respective badges
- Hajen PDF → Hajen Brons/Silver/Guld, Simsättsmärke 3–4

**Licensing note:** Simmärkesmaterial page states material may be downloaded for use and marketing of simmärken [CITED: svensksimidrott.se/simmarkesmaterial] — still document in `SOURCES.md` per D-03.

### SLS (MEDIUM confidence — sparser protocols)

| Resource | URL | Content | Confidence |
|----------|-----|---------|------------|
| Simmärken page | https://svenskalivraddningssallskapet.se/simmarken/ | Official landing | Page fetch timed out — verify at implementation |
| Sim- och livräddningsmärken shop | https://shop.svenskalivraddningssallskapet.se/collections/sim-och-livraddningsmarken | Badge names, product images | Names HIGH; requirements LOW |
| Simborgarmärken shop | https://shop.svenskalivraddningssallskapet.se/collections/simborgarmarken | Annual simborgar pins | Out of core progression scope |

**Recommended SLS v1 subset (D-06 discretion)** — main progression badges visible in shop:

1. Droppen → Skräddaren (entry)
2. Doppingen Turkos → Doppingen Blå utomhus
3. Livbojen Blå → Röd → Grön
4. Krabban Blå → Röd
5. Uttern Järn → Brons
6. Krokodilen Silver → Guld
7. Silvergrodan → Guldgrodan (and Grodan Rygg variants if requirements found)
8. Silversälen (+ Guldsälen if documented)

**SLS categories (proposed):** Mirror shop groupings or tier names — e.g. `Grund`, `Livbojen`, `Uttern`, `Krokodilen`, `Grodan`, `Säl` — **must be validated** against any PDF/brochure found on sls.se during 02-02 extraction. Use `nameSv`/`nameEn` for display; `code` slugs as above.

**Coverage gap:** Unlike Simidrott, SLS may lack per-badge protocol PDFs with verbatim bullets. D-09 may require accepting shop product descriptions or brochure text where PDFs don't exist — flag for human verification (D-12).

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| JSON parsing | Manual JSONObject | kotlinx-serialization `@Serializable` DTOs | Type-safe; already in project [CITED: kotlinlang.org] |
| Catalog version compare | Ad-hoc string equality only | Normalized `YYYY.MM.DD` or semver comparator | D-14 needs ordering across releases |
| Image loading in Phase 2 | Custom bitmap cache | Set `imageAssetPath`; defer Coil to Phase 4 | Phase 2 is data-only |
| PDF text extraction in app | Runtime PDF parser in APK | Maintainer scripts (`pdftotext`, manual QC) at build time | Keeps APK small; D-12 human verification |
| Full catalog ORM | Custom SQL generator | Room DAO + transactional merge | FK integrity, Flow observers exist |

**Key insight:** Catalog seeding is a **build-time data + runtime merge** problem, not a UI problem. Invest in extraction QA and stable codes, not display logic.

## Common Pitfalls

### Pitfall 1: DAO Upsert Does Not Merge by `code` Today

**What goes wrong:** Re-running seed creates duplicate categories/badges because `@Insert(REPLACE)` only replaces on PK conflict and new rows have `id = 0`.
**Why it happens:** Phase 1 stub DAO optimized for insert tests, not updates.
**How to avoid:** Add `findByCode` queries; ID-preserving upsert; wrap full catalog in `@Transaction`.
**Warning signs:** Instrumented test inserting seed twice yields 2× row counts.

### Pitfall 2: Requirement Wording Drift

**What goes wrong:** Parents compare app to paper protocol at simskolan; trust lost.
**Why it happens:** Copying from GP articles, old affisch, or paraphrasing.
**How to avoid:** Extract from current protocol PDFs; record affisch date in `catalogVersion`; manual verification per D-12.
**Warning signs:** Bullet count differs from affisch (D-11).

### Pitfall 3: Progress Loss on Catalog Update

**What goes wrong:** App update wipes kid skill checkmarks.
**Why it happens:** DELETE + reinsert catalog; REPLACE strategy; changing requirement codes casually.
**How to avoid:** D-13 merge; preserve IDs; stable codes; never change codes without migration note.
**Warning signs:** `RequirementProgress` row count drops after seed re-run in test with existing progress.

### Pitfall 4: Image Copyright / APK Bloat

**What goes wrong:** Play policy issues or 50MB+ APK from full-res affisch crops.
**Why it happens:** Bundling print-resolution artwork for every badge.
**How to avoid:** WebP ~128–256px grid size; document sources in `SOURCES.md`; tier fallback for missing SLS images (D-04).
**Warning signs:** APK grows >5MB from images alone.

### Pitfall 5: SLS Thin Data Treated as Complete

**What goes wrong:** Shipping guessed requirements for obscure SLS badges.
**Why it happens:** Pressure to match Simidrott depth (D-06 says thinner OK).
**How to avoid:** Ship fewer SLS badges with verified text; use fallback visuals; document gaps in SOURCES.md.
**Warning signs:** Requirements read like GP article paraphrase, not official copy.

### Pitfall 6: Blocking UI Before Seed Completes (Phase 3 concern)

**What goes wrong:** Empty catalog flash on first launch.
**Why it happens:** Async seed without readiness signal.
**How to avoid:** Phase 2 exposes loader completion for tests; Phase 3 can gate catalog screen on `SeedState.Ready`.
**Warning signs:** `observeCatalogs().first()` empty in test without awaiting seed.

## Code Examples

### Read seed JSON from assets

```kotlin
// Source: kotlinlang.org/docs/serialization.html + Android AssetManager pattern
private val json = Json { ignoreUnknownKeys = true }

fun loadCatalogSeed(context: Context, assetPath: String): CatalogSeedDto {
    val text = context.assets.open(assetPath).bufferedReader().use { it.readText() }
    return json.decodeFromString(CatalogSeedDto.serializer(), text)
}
```

### Catalog version gate (D-14)

```kotlin
// Use comparable format: YYYY.MM.DD from source "Senast uppdaterad" date
fun shouldMerge(bundled: String, stored: String?): Boolean {
    if (stored == null) return true
    return CatalogVersion.parse(bundled) > CatalogVersion.parse(stored)
}
```

### CatalogDao additions required (Wave 0 / 02-03)

```kotlin
// Planner task: extend CatalogDao
@Query("SELECT * FROM catalogs WHERE code = :code LIMIT 1")
suspend fun findCatalogByCode(code: String): CatalogEntity?

@Query("SELECT * FROM categories WHERE catalogId = :catalogId AND code = :code LIMIT 1")
suspend fun findCategoryByCatalogAndCode(catalogId: Long, code: String): CategoryEntity?

@Query("SELECT * FROM badges WHERE categoryId = :categoryId AND code = :code LIMIT 1")
suspend fun findBadgeByCategoryAndCode(categoryId: Long, code: String): BadgeEntity?

@Query("SELECT * FROM requirements WHERE badgeId = :badgeId AND code = :code LIMIT 1")
suspend fun findRequirementByBadgeAndCode(badgeId: Long, code: String): RequirementEntity?
```

### Acceptance sample assertions (roadmap SC2)

```kotlin
// Baddaren Grön — requirements from Baddaren protocol PDF
// Hajen Silver — from Hajen protocol PDF
// SLS Droppen or Skräddaren — from best available SLS source
@Test
fun baddarenGronRequirementsMatchOfficial() {
    val reqs = loadRequirements("simidrott", "baddaren-gron")
    assertTrue(reqs.isNotEmpty())
    assertEquals(expectedFromPdf.size, reqs.size) // D-11 bullet count
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| kapt for Room | KSP | Room 2.6+ | Project already on KSP 2.1.21 |
| `@Insert(REPLACE)` for updates | `@Upsert` + ID lookup | Room 2.5+ | Phase 2 must adopt for merge |
| Hardcoded badge lists | JSON assets + version merge | This phase | Enables catalog updates without progress loss |

**Deprecated/outdated:**
- Relying on 2024 affisch only — 2026 affisch published [CITED: Svensk Simidrott news 2026-02-11]; use latest downloadable affisch for extraction.

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | Simmärkesmaterial promotional license covers in-app badge images | Official sources | Legal review needed; fallback to placeholders |
| A2 | `catalogVersion` as `YYYY.MM.DD` string is sufficient for ordering | Merge strategy | String sort breaks if format changes — document format lock |
| A3 | SLS shop product text can substitute where no protocol PDF exists | SLS sources | D-09 verbatim rule violated — reduce SLS badge count |
| A4 | Pruning requirements absent from new seed is acceptable | Merge | Orphan progress removed via CASCADE — aligns with D-15 code-based matching |
| A5 | Guldmärket/Kandidaten naming on current affisch | Simidrott coverage | **RESOLVED** — see Open Questions #2; verify row names during 02-01 extraction |

## Open Questions (RESOLVED)

1. **SLS authoritative requirement documents** — **RESOLVED**
   - What we know: Shop lists badge names and product descriptions; GP article lists skills (secondary, not D-09 authoritative).
   - **Resolution:** sls.se does not publish per-badge protocol PDFs equivalent to Simidrott's simmärkesmaterial page. SLS requirement text comes from shop product descriptions and any downloadable brochures found via `scripts/extract-sls.sh` (MEDIUM confidence). Badges without MEDIUM+ source are excluded per D-06. Document confidence per badge in `SLS-SOURCE-NOTES.md`; do not use GP article as primary source.
   - Plan impact: 02-02 ships 8–20 curated badges with verified text; thinner than Simidrott by design.

2. **Exact Guldmärket / Kandidaten placement** — **RESOLVED**
   - What we know: 2026 affisch (simmärkesmaterial last updated 2026-03-02) places Järn/Brons/Silver/Guld as **category tiers** (D-07), not badge names. Progression badges within those tiers are Järnmärket, Bronsmärket, Silvermärket, Guldmärket, and Kandidaten as the terminal badge.
   - **Resolution:** Use category `code` `guld` with `nameSv` `"Guld"` for the tier. Badge codes: `jarnmarket`, `bronsmarket`, `silvermarket`, `guldmarket`, `kandidaten`. "Guld" is not a badge name — Guldmärket is the gold-tier pin; Kandidaten is the final progression badge after Guldmärket. Confirm exact `nameSv` spellings from downloaded 2026 affisch during 02-01 extraction (D-12).
   - Plan impact: 02-01 checklist must include `guldmarket` and `kandidaten` rows; `kandidaten` may use `imageAssetPath: null` if WebP not in 02-04 priority set.

3. **SLS category tier names** — **RESOLVED**
   - What we know: D-07 affisch-exact naming applies to Simidrott only; SLS has no single affisch authority.
   - **Resolution:** Adopt shop-progression category groups with stable ASCII `code` slugs: `grund` (Droppen, Skräddaren), `doppingen`, `livbojen`, `krabban`, `uttern`, `krokodilen`, `grodan`, `sal`. `nameSv`/`nameEn` reflect shop grouping labels (e.g. `nameSv` = `"Livbojen"`). Codes are immutable once seeded — future additions use new badges within existing categories.
   - Plan impact: 02-02 Task 1 validates names against shop pages; 02-04 `BadgePlaceholderColors` must include all eight SLS category codes.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| JDK | Gradle build | ✓ | 21.0.10 | — |
| Android SDK / Gradle | APK build | ✓ | gradlew present | — |
| adb / emulator | Instrumented tests | ✓ | adb 1.0.41 | Run tests in CI |
| pdftotext / poppler | PDF extraction (02-01) | [ASSUMED] | — | Manual copy from PDF viewer |
| ImageMagick / cwebp | WebP conversion (02-04) | [ASSUMED] | — | Android Studio export WebP |
| Python + translation API | textEn batch (D-10) | [ASSUMED] | — | Manual EN for small set |

**Missing dependencies with no fallback:**
- None blocking code — extraction tooling is maintainer-side.

**Missing dependencies with fallback:**
- PDF CLI tools → manual extraction with D-12 verification checklist.

## Validation Architecture

### Test Framework

| Property | Value |
|----------|-------|
| Framework | JUnit 4 + AndroidX Test (instrumented); Kotlin test (unit) |
| Config file | none — Wave 0 adds test sources |
| Quick run command | `./gradlew :app:testDebugUnitTest` |
| Full suite command | `./gradlew :app:connectedDebugAndroidTest` |

### Phase Requirements → Test Map

| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| CATA-01 | Simidrott catalog seeded with categories, badges, requirements | instrumented | `./gradlew :app:connectedDebugAndroidTest --tests "*.CatalogSeedLoaderTest"` | ❌ Wave 0 |
| CATA-02 | SLS catalog seeded | instrumented | same suite | ❌ Wave 0 |
| CATA-03 | Categories ordered by `sortOrder` | unit | `./gradlew :app:testDebugUnitTest --tests "*.CatalogVersionTest"` + seed ordering assert | ❌ Wave 0 |
| CATA-04 | Requirements present per badge; Baddaren/Hajen/SLS samples match expected | unit + instrumented | `--tests "*.SimidrottRequirementAccuracyTest"` | ❌ Wave 0 |
| CATA-05 | `imageAssetPath` set for badges with bundled art; null allowed with documented fallback | instrumented | `--tests "*.CatalogSeedLoaderTest.badgeImagesResolvable"` | ❌ Wave 0 |
| D-13/D-14 | Merge preserves progress when version bumps | instrumented | `--tests "*.CatalogSeedMergeTest"` | ❌ Wave 0 |
| D-15 | Same requirement `code` keeps progress after text update | instrumented | `--tests "*.CatalogSeedMergeTest.progressPreservedWhenTextChanges"` | ❌ Wave 0 |

### Sampling Rate

- **Per task commit:** `./gradlew :app:testDebugUnitTest`
- **Per wave merge:** `./gradlew :app:connectedDebugAndroidTest`
- **Phase gate:** Full suite green + manual D-12 verification checklist for all badges

### Wave 0 Gaps

- [ ] `app/src/test/java/se/simmarken/data/seed/CatalogVersionTest.kt` — version comparison
- [ ] `app/src/test/java/se/simmarken/data/seed/CatalogSeedParserTest.kt` — JSON → DTO from test fixture
- [ ] `app/src/test/java/se/simmarken/data/seed/SimidrottRequirementAccuracyTest.kt` — golden files for Baddaren, Hajen
- [ ] `app/src/androidTest/java/se/simmarken/data/seed/CatalogSeedLoaderTest.kt` — first-run population
- [ ] `app/src/androidTest/java/se/simmarken/data/seed/CatalogSeedMergeTest.kt` — progress preservation
- [ ] `app/src/test/resources/seed/simidrott_sample.json` — minimal fixture for unit tests
- [ ] `CatalogDao` lookup-by-code queries + transactional merge support

## Security Domain

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|------------------|
| V2 Authentication | no | Offline app; no accounts in v1 |
| V3 Session Management | no | — |
| V4 Access Control | no | Single-user local data |
| V5 Input Validation | yes | Validate seed JSON schema; reject malformed assets at parse time (`Json { coerceInputValues = true; ignoreUnknownKeys = false }` for production seed files) |
| V6 Cryptography | no | No secrets in catalog seed |

### Known Threat Patterns for {stack}

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Malicious JSON in assets (supply chain) | Tampering | Assets ship signed in APK; parse with strict DTO; no dynamic download |
| Oversized asset DoS | Denial of Service | Reasonable JSON size (<2MB per catalog); fail fast on parse errors |
| Image copyright violation | Repudiation/Legal | Document sources in SOURCES.md (D-03); use official promotional materials |

## Sources

### Primary (HIGH confidence)

- [Svensk Simidrott simmärkesmaterial](https://svensksimidrott.se/simkunnighet/simmarken/simmarkesmaterial) — protocol PDF index, promotional use statement, last updated 2026-03-02
- [Svensk Simidrott simmärken](https://svensksimidrott.se/simkunnighet/simmarken) — affisch download entry point
- [Simmärkesaffisch 2024 PDF](https://svensksimidrott.se/download/18.2834ddaa18d0e8137e44ef74/1705584326390/simm%C3%A4rkesaffisch%202024.pdf) — tier structure and requirement text snippets
- [Kotlin serialization](https://kotlinlang.org/docs/serialization.html) — JSON DTO parsing
- [Room @Upsert](https://developer.android.com/reference/androidx/room/Upsert) — insert-or-update semantics, PK-based conflict
- Project codebase — `CatalogEntity`, `CatalogDao`, `AppContainer` (Phase 1)

### Secondary (MEDIUM confidence)

- [SLS shop — sim- och livräddningsmärken](https://shop.svenskalivraddningssallskapet.se/collections/sim-och-livraddningsmarken) — badge name inventory
- [2026 affisch news](https://svensksimidrott.se/nyheter-svensk-simidrott/svensk-simidrott/2026-02-11-nu-ar-2026-ars-simmarkesaffischer-och-folder-har) — annual update cadence
- `.planning/research/PITFALLS.md` — requirement accuracy, image copyright
- `.planning/research/ARCHITECTURE.md` — seed package location, WebP guidance

### Tertiary (LOW confidence — validate during extraction)

- [GP simmärken article](https://www.gp.se/nyheter/sverige/har-ar-alla-simmarken-och-krav-flera-nya-2025.fa1c2ddc-aed7-47f5-ab97-50afed51ad2c) — SLS requirement text; **not** authoritative for D-09

## Metadata

**Confidence breakdown:**
- Standard stack: **HIGH** — kotlinx-serialization + Room already in project; patterns verified against official docs
- Architecture: **HIGH** — clear merge-by-code with ID preservation; DAO gap identified in codebase
- Pitfalls: **MEDIUM-HIGH** — Simidrott sources strong; SLS requirement sourcing remains risky
- Official data: **HIGH** Simidrott / **MEDIUM** SLS

**Research date:** 2026-07-22
**Valid until:** 2026-08-22 (affisch may update annually — re-check simmarkesmaterial before ship)

## RESEARCH COMPLETE
