# Phase 4: Catalog View & Visual States - Research

**Researched:** 2026-07-23
**Domain:** Android Jetpack Compose catalog UI + derived badge visual state
**Confidence:** HIGH

## Summary

Phase 4 replaces `ChildCatalogPlaceholderScreen` with a dual-catalog (Simidrott / SLS) badge grid showing four derived visual states. The codebase already has the persistence layer (`CatalogRepository`, `ProgressRepository`, seeded JSON with `imageAssetPath`, `BadgePlaceholderColors`) and navigation shell (`ChildCatalog` route, `ChildCatalogViewModel` loading child name). What is missing is: (1) centralized `BadgeStateCalculator` domain logic with JVM unit tests, (2) Coil dependency for bundled WebP assets, (3) reactive aggregation of catalog structure + per-kid progress into UI models, and (4) Compose components for sticky category headers, 3-column badge rows, progress ring, and state overlays.

The highest-risk implementation area is **reactive data joining**: each catalog has multiple categories, each category has multiple badges, and state derivation needs requirement counts per badge cross-referenced with `RequirementProgressEntity` and `BadgeProgressEntity` for the active `kidId`. The established pattern in this project (`HomeViewModel` using `combine` + `stateIn`) should extend to `flatMapLatest` on the selected catalog tab, then `combine` per-category badge flows. A single `observeAllRequirements()` DAO query (or equivalent) avoids N+1 requirement lookups during state computation.

**Primary recommendation:** Implement `BadgeStateCalculator` first (pure JVM, fully unit-tested), add Coil 3 `coil-compose` for asset images, then build `ChildCatalogScreen` with a single non-nested `LazyColumn` (`stickyHeader` + 3-badge `Row` items) and share `BadgePinVisual` with `BadgeDetailPlaceholderScreen`.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

#### Dual-Catalog Navigation
- **D-01:** Tab row below top bar — "Simidrott" | "SLS"; one catalog visible at a time.
- **D-02:** Default to Svensk Simidrott tab when opening a child's catalog screen.
- **D-03:** Short Swedish tab labels: "Simidrott" | "SLS" (full i18n in Phase 6).
- **D-04:** Tab selection is session-only — reset to Simidrott default each time the child catalog screen is opened (no per-child persistence).

#### Locked Badge Rules
- **D-05:** Locked = zero requirements checked for that badge.
- **D-06:** All badges always visible in the grid — locked badges shown grayscale, never hidden.
- **D-07:** First requirement checked → badge immediately leaves locked and becomes in progress (partial ring).
- **D-08:** Categories are independent — no tier gating; parent may start badges in any category without completing prior tiers.

#### In-Progress Visual
- **D-09:** Circular progress ring around the pin image, filled proportionally by requirement count (achieved / total).
- **D-10:** Progress ring uses theme primary color.
- **D-11:** Pin image stays grayscale while in progress — only the ring signals partial completion.
- **D-12:** All requirements met → full-color pin, cart overlay, progress ring removed (achieved-to-buy state).

#### Badge Grid Layout
- **D-13:** 3 columns on a typical phone — larger pins for swim-hall glances.
- **D-14:** Badge name shown below each pin, truncated to one line.
- **D-15:** Comfortable spacing — ~12–16dp gaps between cells for thumb-friendly taps.
- **D-16:** Square tile cells with pin image centered.

#### Category Headers
- **D-17:** Always expanded — no collapsible category sections.
- **D-18:** Sticky category headers while scrolling within the active catalog tab.
- **D-19:** Swedish category names only in Phase 4 — use `nameSv` from seed (`nameEn` in Phase 6).
- **D-20:** Category order follows official affisch `sortOrder` from seed (Phase 2 D-08).

#### State Overlays
- **D-21:** Achieved-to-buy: shopping cart icon overlay, bottom-right corner, on full-color pin.
- **D-22:** Gotten: checkmark icon overlay, bottom-right corner, on full-color pin.
- **D-23:** Locked: grayscale pin only — no lock icon or dim scrim.
- **D-24:** Cart and checkmark overlays use a small filled circle badge with white icon (bottom-right) for readability on pin art.

#### Badge Detail Placeholder
- **D-25:** Detail screen shows large pin image, badge name, and stub text "Checklista kommer snart" — confirms tap target before Phase 5 checklist.
- **D-26:** New navigation route `BadgeDetail(kidId, badgeId)` pushed on the stack from grid tap.
- **D-27:** View only in Phase 4 — no requirement checklist toggles until Phase 5.
- **D-28:** Detail screen respects the same 4-tier visuals as the grid (grayscale, ring, cart, checkmark).

#### Badge State Computation (carried forward + locked here)
- **D-29:** Derive visual state at read time via centralized `BadgeStateCalculator` — never persist enum in DB (Phase 1 D-05).
- **D-30:** State precedence: `isGotten` → GOTTEN; else all requirements achieved → ACHIEVED_TO_BUY; else any requirement achieved → IN_PROGRESS; else → LOCKED.

### Claude's Discretion
- Empty-catalog tab behavior if a catalog has zero badges (both tabs remain visible with empty state)
- Progress ring stroke width, cap style, and whether to animate on change
- Exact grayscale `ColorFilter` / saturation approach for locked and in-progress pins
- Coil image request sizes for grid vs detail; tier-color fallback when `imageAssetPath` is null (Phase 2 D-04)
- Lazy list structure for sticky headers + per-category grids (single `LazyColumn` with sticky header items vs nested scroll)
- Hardcoded Swedish UI strings until Phase 6 i18n

### Deferred Ideas (OUT OF SCOPE)
None — discussion stayed within phase scope.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| UI-02 | Child profile screen shows catalog badges grouped by category in a grid | `LazyColumn` + `stickyHeader` per category + 3-column `Row` chunks; dual-tab catalog switch via catalog codes `simidrott` / `sls` |
| UI-04 | Four visual badge states clearly distinguishable | `BadgeVisualState` enum + `BadgePinVisual` with grayscale `ColorFilter`, `CircularProgressRing`, `StateOverlay` (cart/check) per UI-SPEC |
| PROG-04 | Badge visual state reflects locked, in progress, achieved-to-buy, or gotten | `BadgeStateCalculator` derives from `RequirementProgressEntity.isAchieved` + `BadgeProgressEntity.isGotten`; never persisted |
</phase_requirements>

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Badge visual state derivation | Domain (`BadgeStateCalculator`) | — | Pure logic; shared by grid and detail; unit-testable without Android framework |
| Catalog structure loading | API/Data (`CatalogRepository` + Room DAOs) | Domain (optional mapper) | Categories/badges/requirements are authoritative in Room; UI observes Flows |
| Per-kid progress reads | API/Data (`ProgressRepository`) | — | `RequirementProgress` + `BadgeProgress` tables owned by data layer |
| Tab selection + screen UI state | Browser/Client (ViewModel + Compose) | — | Session-only tab index; no persistence in Phase 4 |
| Category-grouped grid rendering | Browser/Client (Compose) | — | `LazyColumn`, sticky headers, badge rows are presentation |
| Badge image loading | Browser/Client (Coil `AsyncImage`) | CDN/Static (bundled assets) | WebP files in `assets/badges/`; loaded client-side |
| Navigation to detail | Browser/Client (`NavHost` + routes) | — | Type-safe `BadgeDetail(kidId, badgeId)` route on stack |
| Requirement toggles / purchase | — (Phase 5) | — | Explicitly out of scope; display-only reads |

## Standard Stack

### Core

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Kotlin | 2.1.21 | Language | Project baseline in `libs.versions.toml` |
| Jetpack Compose (BOM) | 2025.12.01 | UI | Existing screens; Material 3 theme |
| Material 3 | via BOM | Design system | `SimmarkenTheme`, `PrimaryTabRow`, app bars |
| Navigation Compose | 2.9.8 | Routing | Type-safe `@Serializable` routes already used |
| Room | 2.8.4 | Offline catalog + progress | `CatalogRepository`, `ProgressRepository` Flows |
| **Coil Compose** | **3.5.0** | Badge WebP loading | Official image loader for Compose; supports `file:///android_asset/` URIs [CITED: coil-kt.github.io/coil/getting_started] |

### Supporting

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| kotlinx-coroutines | 1.9.0 | Flow combine / flatMapLatest | ViewModel reactive catalog assembly |
| JUnit 4 | 4.13.2 | JVM unit tests | `BadgeStateCalculator` tests in `app/src/test` |
| `BadgePlaceholderColors` | existing | Tier-color fallback | When `imageAssetPath` is null |

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Coil `AsyncImage` | `Image(painterResource)` per asset | Seed uses dynamic paths; Coil handles caching + sizing |
| Nested `LazyVerticalGrid` | Single `LazyColumn` + `Row` chunks | Nested scroll breaks sticky headers; UI-SPEC recommends non-nested |
| Persist `BadgeVisualState` enum | Derive at read time | Stale state bugs (Phase 1 D-05, Pitfall 3) |

**Installation (Phase 4 — Coil not yet in project):**

```kotlin
// gradle/libs.versions.toml
[versions]
coil = "3.5.0"

[libraries]
coil-compose = { group = "io.coil-kt.coil3", name = "coil-compose", version.ref = "coil" }

// app/build.gradle.kts
implementation(libs.coil.compose)
```

**Version verification:** Coil 3.5.0 documented on official getting-started page [CITED: coil-kt.github.io/coil/getting_started]. Project already pins Navigation 2.9.8, Room 2.8.4, Compose BOM 2025.12.01 in `gradle/libs.versions.toml`.

## Package Legitimacy Audit

> Phase adds **Maven/Gradle** dependency only. npm legitimacy gate is not applicable; verified against official Coil documentation and Maven Central coordinates.

| Package | Registry | Age | Downloads | Source Repo | Verdict | Disposition |
|---------|----------|-----|-----------|-------------|---------|-------------|
| `io.coil-kt.coil3:coil-compose:3.5.0` | Maven Central | Mature (Coil 1.x→3.x) | Widely used | github.com/coil-kt/coil | OK | Approved — assets-only; **no** `coil-network-okhttp` needed |

**Packages removed due to [SLOP] verdict:** none

**Packages flagged as suspicious [SUS]:** none

## Architecture Patterns

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                     ChildCatalogScreen (Compose)                     │
│  PrimaryTabRow ──► LazyColumn(stickyHeader + BadgeRow×3)            │
│       │                    │                                         │
│       │                    └── tap ──► NavController.navigate       │
└───────┼────────────────────┼────────────────────────────────────────┘
        │                    │
        ▼                    ▼
┌───────────────────┐  ┌────────────────────────────────────────────┐
│ ChildCatalogVM    │  │ BadgeDetailPlaceholderScreen + BadgeDetailVM │
│ StateFlow<UiState>│  │ (same BadgePinVisual + BadgeStateCalculator) │
└─────────┬─────────┘  └──────────────────┬─────────────────────────┘
          │ combine + flatMapLatest          │ observe badge + progress
          ▼                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        Domain Layer                                  │
│  BadgeStateCalculator.compute(isGotten, achievedCount, total)       │
│  BadgeVisualState { LOCKED, IN_PROGRESS, ACHIEVED_TO_BUY, GOTTEN }│
└──────────────────────────────┬──────────────────────────────────────┘
                               │
          ┌────────────────────┼────────────────────┐
          ▼                    ▼                    ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────────────┐
│ CatalogRepository│ │ProgressRepository│ │ KidRepository          │
│ observeCatalogs  │ │ observeReqProg   │ │ observeById (name)     │
│ observeCategories│ │ observeBadgeProg │ │                        │
│ observeBadges    │ │ (read-only P4)   │ │                        │
│ observeAllReqs*  │ │                  │ │                        │
└────────┬────────┘ └────────┬────────┘ └───────────┬─────────────┘
         │                   │                      │
         ▼                   ▼                      ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     Room (SQLite) — offline                          │
│  catalogs / categories / badges / requirements (seeded)              │
│  requirement_progress / badge_progress (per kid, sparse until P5)    │
└─────────────────────────────────────────────────────────────────────┘

* observeAllRequirements — recommended new DAO query for efficient state map
```

### Recommended Project Structure

```
app/src/main/java/se/simmarken/
├── domain/
│   ├── BadgeStateCalculator.kt
│   └── model/
│       ├── BadgeVisualState.kt
│       └── CatalogUiModels.kt          # CategorySection, BadgeCellUiModel
├── ui/
│   ├── child/
│   │   ├── ChildCatalogScreen.kt       # replaces ChildCatalogPlaceholderScreen
│   │   ├── ChildCatalogViewModel.kt    # extended
│   │   └── components/
│   │       ├── CatalogTabRow.kt
│   │       ├── CategoryStickyHeader.kt
│   │       ├── BadgeGrid.kt            # LazyColumn builder
│   │       ├── BadgeGridItem.kt
│   │       └── CatalogEmptyState.kt
│   ├── badge/
│   │   ├── BadgeDetailPlaceholderScreen.kt
│   │   ├── BadgeDetailViewModel.kt
│   │   └── BadgePlaceholderColors.kt   # existing
│   └── components/
│       ├── BadgePinVisual.kt
│       ├── CircularProgressRing.kt
│       └── StateOverlay.kt
└── navigation/
    ├── Routes.kt                       # + BadgeDetail(kidId, badgeId)
    ├── SimmarkenNavHost.kt
    ├── ChildCatalogViewModelFactory.kt # extend deps
    └── BadgeDetailViewModelFactory.kt
```

### Pattern 1: Centralized Derived Badge State

**What:** Pure `BadgeStateCalculator` maps progress inputs → `BadgeVisualState`  
**When to use:** Grid, detail placeholder, and (later) home summary counts  
**Example:**

```kotlin
// Source: Phase 4 CONTEXT D-30; ARCHITECTURE.md derived state pattern
enum class BadgeVisualState { LOCKED, IN_PROGRESS, ACHIEVED_TO_BUY, GOTTEN }

object BadgeStateCalculator {
    fun compute(
        totalRequirements: Int,
        achievedCount: Int,
        isGotten: Boolean,
    ): BadgeVisualState {
        if (isGotten) return BadgeVisualState.GOTTEN
        val allAchieved = totalRequirements == 0 || achievedCount >= totalRequirements
        if (allAchieved) return BadgeVisualState.ACHIEVED_TO_BUY
        if (achievedCount > 0) return BadgeVisualState.IN_PROGRESS
        return BadgeVisualState.LOCKED
    }

    fun progressFraction(achievedCount: Int, totalRequirements: Int): Float =
        if (totalRequirements <= 0) 0f else achievedCount.toFloat() / totalRequirements
}
```

**Edge case:** Badge with zero requirements → `allAchieved` is vacuously true → `ACHIEVED_TO_BUY` (cart overlay) unless `isGotten`. Unit-test this explicitly.

### Pattern 2: Reactive Catalog Assembly (ViewModel)

**What:** `combine` progress Flows with `flatMapLatest` on selected catalog  
**When to use:** `ChildCatalogViewModel` building `List<CategorySection>`  
**Example:**

```kotlin
// Source: HomeViewModel pattern; [CITED: developer.android.com navigation + kotlinx Flow docs]
@OptIn(ExperimentalCoroutinesApi::class)
private fun observeCategorySections(catalogId: Long): Flow<List<CategorySection>> =
    catalogRepository.observeCategories(catalogId).flatMapLatest { categories ->
        if (categories.isEmpty()) {
            flowOf(emptyList())
        } else {
            combine(
                categories.map { category ->
                    catalogRepository.observeBadges(category.id).map { badges ->
                        category to badges
                    }
                },
            ) { pairs -> pairs.map { (cat, badges) -> CategorySection(cat, badges) } }
        }
    }
```

**Progress indexing:** On each emission, build `Map<Long, Boolean>` from `requirementProgress` (default `false` if row missing) and `Map<Long, Boolean>` for `badgeProgress.isGotten`. Join with `requirements.groupBy { it.badgeId }` from `observeAllRequirements()`.

### Pattern 3: Non-Nested Sticky Header Grid

**What:** One `LazyColumn`; per category: `stickyHeader` + `items(badgeRows)` where each row holds up to 3 `BadgeGridItem`s  
**When to use:** Category-grouped 3-column grid with D-18 sticky headers  
**Example:**

```kotlin
// Source: [CITED: developer.android.com/develop/ui/compose/lists — stickyHeader]
LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
    sections.forEach { section ->
        stickyHeader(key = section.category.id) {
            CategoryStickyHeader(nameSv = section.category.nameSv)
        }
        val rows = section.badges.chunked(3)
        items(rows, key = { row -> row.first().id }) { row ->
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                row.forEach { badge -> BadgeGridItem(...) }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}
```

### Pattern 4: Coil Asset URI for Seed Paths

**What:** Prefix seed `imageAssetPath` with `file:///android_asset/`  
**When to use:** `BadgePinVisual` when `imageAssetPath != null`  
**Example:**

```kotlin
// Source: [CITED: coil-kt.github.io/coil/getting_started; Coil GitHub #10 android_asset]
val assetUri = "file:///android_asset/${imageAssetPath}"  // e.g. badges/simidrott/baddaren_gron.webp

AsyncImage(
    model = ImageRequest.Builder(context)
        .data(assetUri)
        .size(96) // grid; 320 for detail
        .build(),
    contentDescription = contentDescription,
    contentScale = ContentScale.Fit,
    colorFilter = if (grayscale) grayscaleColorFilter else null,
)
```

### Anti-Patterns to Avoid

- **Nested `LazyVerticalGrid` inside `LazyColumn`:** Breaks scroll + sticky header behavior; use chunked rows instead (UI-SPEC discretion).
- **Duplicate state logic in catalog vs detail ViewModels:** Use `BadgeStateCalculator` only (Pitfall 3).
- **Hiding locked badges:** Violates D-06; always render all badges grayscale.
- **Using `BadgePlaceholderColors.forBadgeCode`:** Existing helper maps badge code to category keys incorrectly — pass `category.code` via `forCategoryCode(categoryCode)` (verified in `BadgePlaceholderColors.kt`).
- **Persisting visual state enum:** Violates D-29 / Phase 1 D-05.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Asset image loading + caching | Custom `AssetManager` bitmap decode | Coil `AsyncImage` | Memory management, sizing, Compose integration |
| Grayscale filter | Custom pixel shader | `ColorFilter.colorMatrix` + `setToSaturation(0f)` | Standard Compose API [CITED: developer.android.com/develop/ui/compose/graphics/images/customize] |
| Sticky category headers | Manual scroll offset tracking | `LazyColumn { stickyHeader { } }` | Platform-supported, accessible |
| Circular progress arc | Third-party chart lib | `Canvas.drawArc` with `StrokeCap.Round` | Lightweight; UI-SPEC specifies 3dp stroke |
| Multi-flow catalog assembly | Manual polling / one-shot queries | `combine` + `flatMapLatest` + `stateIn` | Matches `HomeViewModel` pattern; reactive to Room |

**Key insight:** Phase 4 is mostly composition of existing Room data and Compose primitives. The only new external dependency justified is Coil for dynamic asset paths from seed JSON.

## Common Pitfalls

### Pitfall 1: Badge State Drift Across Screens

**What goes wrong:** Grid shows IN_PROGRESS but detail shows LOCKED for same badge.  
**Why it happens:** Duplicated if/else in ViewModels.  
**How to avoid:** Single `BadgeStateCalculator`; both screens pass same inputs.  
**Warning signs:** Different precedence order or missing `isGotten` check.

### Pitfall 2: Missing Progress Rows Treated as Achieved

**What goes wrong:** Badges appear in progress without parent checking anything.  
**Why it happens:** Assuming progress row exists; wrong default for absent `RequirementProgressEntity`.  
**How to avoid:** Default `isAchieved = false` when no row; only count explicit `true`.  
**Warning signs:** Fresh kid shows colored pins.

### Pitfall 3: Nested Scroll / Broken Sticky Headers

**What goes wrong:** Headers scroll away or grid doesn't scroll smoothly.  
**Why it happens:** `LazyVerticalGrid` nested in `LazyColumn`.  
**How to avoid:** Chunk badges into rows of 3 inside single `LazyColumn`.  
**Warning signs:** Scroll fling conflicts, header not pinning.

### Pitfall 4: Tab State Persisted Across Sessions

**What goes wrong:** SLS tab still selected after leaving and re-entering catalog.  
**Why it happens:** Saving tab in `ViewModel` scoped to activity or DataStore.  
**How to avoid:** `remember { mutableIntStateOf(0) }` in screen composable OR reset tab in ViewModel `init` per navigation entry (D-04).  
**Warning signs:** Tab not Simidrott on fresh open.

### Pitfall 5: Coil Not Added Before UI Work

**What goes wrong:** Build failure or placeholder-only pins despite bundled WebP assets.  
**Why it happens:** STACK.md lists Coil but `app/build.gradle.kts` currently has no Coil dependency (verified).  
**How to avoid:** Add `coil-compose` in plan 04-02 Wave 0 or 04-03 prerequisite.  
**Warning signs:** Unresolved `AsyncImage` import.

## Code Examples

### BadgeStateCalculator Unit Test Matrix

```kotlin
// Source: CONTEXT D-05, D-07, D-12, D-30
@Test fun gottenOverridesAll() {
    assertEquals(GOTTEN, BadgeStateCalculator.compute(5, 0, isGotten = true))
}

@Test fun allRequirementsAchieved_isAchievedToBuy() {
    assertEquals(ACHIEVED_TO_BUY, BadgeStateCalculator.compute(3, 3, isGotten = false))
}

@Test fun oneRequirement_isInProgress() {
    assertEquals(IN_PROGRESS, BadgeStateCalculator.compute(3, 1, isGotten = false))
}

@Test fun zeroAchieved_isLocked() {
    assertEquals(LOCKED, BadgeStateCalculator.compute(3, 0, isGotten = false))
}
```

### Grayscale ColorFilter

```kotlin
// Source: [CITED: developer.android.com/develop/ui/compose/graphics/images/customize]
private val GrayscaleFilter = ColorFilter.colorMatrix(
    ColorMatrix().apply { setToSaturation(0f) },
)
```

### Circular Progress Ring

```kotlin
// Source: UI-SPEC; CONTEXT D-09–D-11
Canvas(modifier = modifier) {
    val stroke = 3.dp.toPx()
    val inset = stroke / 2f
    val diameter = size.minDimension - stroke
    val topLeft = Offset(inset, inset)
    val arcSize = Size(diameter, diameter)
    drawArc(
        color = trackColor,
        startAngle = 0f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = stroke, cap = StrokeCap.Round),
    )
    drawArc(
        color = progressColor,
        startAngle = -90f,
        sweepAngle = 360f * progress,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = stroke, cap = StrokeCap.Round),
    )
}
```

### Type-Safe Navigation Route

```kotlin
// Source: [CITED: developer.android.com/develop/ui/compose/navigation]; existing Routes.kt pattern
@Serializable
data class BadgeDetail(val kidId: Long, val badgeId: Long)

// SimmarkenNavHost.kt
composable<BadgeDetail> { entry ->
    val route = entry.toRoute<BadgeDetail>()
    // BadgeDetailPlaceholderScreen(...)
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| `ChildCatalogPlaceholderScreen` | Full catalog grid + detail stub | Phase 4 | Delivers UI-02 |
| No image loader dep | Coil 3 `coil-compose` | Phase 4 add | Required for WebP assets |
| Progress display deferred | Read-only progress observation | Phase 4 | PROG-04 visual only; writes in Phase 5 |

**Deprecated/outdated:**
- `ChildCatalogPlaceholderScreen` — delete after `ChildCatalogScreen` wired in NavHost.

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | Catalog codes are `"simidrott"` and `"sls"` in seed JSON | Standard Stack | Tab mapping fails if codes differ |
| A2 | Absent `RequirementProgressEntity` row means `isAchieved = false` | Pitfall 2 | Wrong default state for new kids |
| A3 | Coil 3.5.0 is compatible with project AGP 8.13 / compileSdk 35 | Standard Stack | Build resolution failure |
| A4 | `observeAllRequirements()` DAO addition is acceptable scope | Architecture | N+1 queries if omitted |

**Items A1 verified:** `simidrott.json` and `sls.json` headers in `app/src/main/assets/seed/` [VERIFIED: codebase].

## Open Questions (RESOLVED)

1. **Should `observeAllRequirements()` live in CatalogDao or is per-badge `observeRequirements` sufficient?** *(RESOLVED — 04-01 plan task 3)*
   - What we know: State calc needs requirement IDs grouped by `badgeId` for every visible badge.
   - Resolution: Add `@Query("SELECT * FROM requirements") fun observeAllRequirements(): Flow<List<RequirementEntity>>` to `CatalogDao` with thin `CatalogRepository` passthrough. Catalog is small (~hundreds of rows), static after seed; one aggregate query avoids N+1 in ViewModel combine.

2. **Loading UI for catalog screen?** *(RESOLVED — 04-02 plan)*
   - What we know: Room Flows emit quickly on local DB; UI-SPEC says optional loading indicator.
   - Resolution: Skip dedicated loading UI in Phase 4. `ChildCatalogScreen` uses populated grid, `CatalogEmptyState`, or `Barnet hittades inte` error branch only — no spinner.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| Android SDK / AGP | Build | ✓ | AGP 8.13.2, compileSdk 35 | — |
| JDK 17 | Kotlin compile | ✓ | jvmTarget 17 in `app/build.gradle.kts` | — |
| Gradle | Build | ✓ | 8.13 wrapper | — |
| Coil (new) | BadgePinVisual | ✗ (not in gradle yet) | 3.5.0 target | Must add — no fallback for dynamic asset paths |
| Android device/emulator | Manual UAT | — | minSdk 26 | Required for visual verification |

**Missing dependencies with no fallback:**
- `io.coil-kt.coil3:coil-compose` — add in first UI plan wave.

**Missing dependencies with fallback:**
- None once Coil is added.

## Validation Architecture

### Test Framework

| Property | Value |
|----------|-------|
| Framework | JUnit 4.13.2 (JVM unit tests) |
| Config file | none — standard Android `app/src/test` |
| Quick run command | `./gradlew :app:testDebugUnitTest --tests "se.simmarken.domain.BadgeStateCalculatorTest"` |
| Full suite command | `./gradlew :app:testDebugUnitTest` |

### Phase Requirements → Test Map

| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| PROG-04 | State precedence: gotten > achieved-to-buy > in-progress > locked | unit | `./gradlew :app:testDebugUnitTest --tests "*BadgeStateCalculatorTest*"` | ❌ Wave 0 (04-01) |
| PROG-04 | Zero requirements → ACHIEVED_TO_BUY edge case | unit | same as above | ❌ Wave 0 |
| PROG-04 | Progress fraction for ring | unit | same as above | ❌ Wave 0 |
| UI-02 | Category grouping + sortOrder | manual / instrumented | — | Optional Phase 4 UAT |
| UI-04 | Visual distinction of 4 states | manual | Human verify at ship | — |

### Sampling Rate

- **Per task commit:** `./gradlew :app:testDebugUnitTest --tests "*BadgeStateCalculatorTest*"`
- **Per wave merge:** `./gradlew :app:testDebugUnitTest`
- **Phase gate:** Unit tests green + manual catalog/detail navigation check

### Wave 0 Gaps

- [ ] `app/src/test/java/se/simmarken/domain/BadgeStateCalculatorTest.kt` — covers PROG-04 precedence + edge cases
- [ ] `app/src/main/java/se/simmarken/domain/BadgeStateCalculator.kt` — domain object under test
- [ ] `app/src/main/java/se/simmarken/domain/model/BadgeVisualState.kt` — enum
- [ ] Coil dependency in `gradle/libs.versions.toml` + `app/build.gradle.kts`
- [ ] Optional: `CatalogDao.observeAllRequirements()` + repository passthrough

## Security Domain

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|------------------|
| V2 Authentication | no | N/A — local parent app, no auth |
| V3 Session Management | no | N/A |
| V4 Access Control | no | Single-user offline app |
| V5 Input Validation | yes (minimal) | Validate `kidId` / `badgeId` route args exist; pop or show error if missing |
| V6 Cryptography | no | N/A this phase |

### Known Threat Patterns for Compose + Room offline app

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Invalid navigation IDs showing stale UI | Spoofing | ViewModel checks kid/badge exist; empty/error state per UI-SPEC |
| SQL injection via route params | Tampering | Room parameterized queries only; IDs are `Long` types |
| Untrusted image paths in seed | Tampering | Seed is bundled; `imageAssetPath` from trusted JSON only |

## Project Constraints (from .cursor/rules/)

No `.cursor/rules/` directory found in project root. Follow existing codebase conventions: MVVM, manual `ViewModelProvider.Factory` in `navigation/` package, Swedish hardcoded strings, Material 3 theme.

## Sources

### Primary (HIGH confidence)

- Existing codebase: `ChildCatalogPlaceholderScreen.kt`, `CatalogRepository.kt`, `ProgressRepository.kt`, `BadgePlaceholderColors.kt`, `SimmarkenNavHost.kt`, seed JSON — verified via direct file read
- Phase artifacts: `04-CONTEXT.md`, `04-UI-SPEC.md`, `ARCHITECTURE.md`, `PITFALLS.md`, `STACK.md`

### Secondary (MEDIUM confidence)

- [Android Compose lists — stickyHeader](https://developer.android.com/develop/ui/compose/lists) — sticky header pattern
- [Coil getting started](https://coil-kt.github.io/coil/getting_started/) — `coil-compose:3.5.0` coordinates
- [Compose image color filter](https://developer.android.com/develop/ui/compose/graphics/images/customize) — grayscale via `ColorMatrix`
- [Navigation Compose type-safe routes](https://developer.android.com/develop/ui/compose/navigation) — `@Serializable` routes

### Tertiary (LOW confidence)

- Stack Overflow / Medium articles on Flow `combine` + `flatMapLatest` — pattern only; prefer kotlinx official API

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — verified against `libs.versions.toml`, existing patterns, official Coil/Android docs
- Architecture: HIGH — codebase inspection confirms integration points; flow assembly pattern matches `HomeViewModel`
- Pitfalls: HIGH — PITFALLS.md Pitfall 3 + CONTEXT locked decisions align

**Research date:** 2026-07-23  
**Valid until:** 2026-08-23 (stable Compose/Room stack)
