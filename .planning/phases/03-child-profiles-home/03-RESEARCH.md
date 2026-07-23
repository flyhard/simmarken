# Phase 3: Child Profiles & Home - Research

**Researched:** 2026-07-23
**Domain:** Android Jetpack Compose Material 3 — kid CRUD, home screen, bottom sheet forms, type-safe navigation
**Confidence:** HIGH

## Summary

Phase 3 replaces the walking-skeleton `HomeScreen` (kid count + debug insert button) with a production kid-first home experience: single-column child cards with initials avatars, FAB-driven add flow, overflow-menu edit/delete, and navigation to a catalog placeholder. The persistence layer already has `KidEntity`, `KidDao.observeAll()`, and `KidRepository.upsert()` from Phase 1; this phase extends them with **delete**, **observe-by-id**, and a corrected **ORDER BY** clause, then builds the Compose UI per `03-UI-SPEC.md`.

No new external libraries are required — Material 3 `ModalBottomSheet`, `AlertDialog`, `DropdownMenu`, and Navigation Compose type-safe routes (already on classpath at 2.9.8) cover all UI needs. The main engineering risks are: (1) `KidDao` currently orders by `sortOrder, name` but locked decision D-04 requires `sortOrder, createdAtEpochMillis`; (2) bottom sheet dismiss-after-save must use coroutine `sheetState.hide()` before removing from composition [CITED: developer.android.com/develop/ui/compose/components/bottom-sheets]; (3) card tap vs overflow menu requires isolated click targets to avoid accidental navigation on edit/delete [CITED: 03-UI-SPEC.md].

Delete is safe at the data layer: `RequirementProgressEntity` and `BadgeProgressEntity` both declare `ForeignKey(onDelete = CASCADE)` on `kidId` — deleting a kid row cascades progress without manual cleanup [VERIFIED: codebase grep of entity FK definitions].

**Primary recommendation:** Extend `KidDao`/`KidRepository` with delete + observeById + fixed ordering, add pure-function helpers (`KidAvatarInitials`, `KidAvatarColors`) in `ui/theme/`, build Home + form as overlay state on `HomeScreen` (not navigation destinations), add `ChildCatalog(kidId: Long)` type-safe route for placeholder, and cover CRUD + cascade delete with instrumented tests following existing `DaoInstrumentedTest` patterns.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

#### Home Card Layout
- **D-01:** Single-column vertical list — one full-width card per child for easy one-handed tapping at the swim hall.
- **D-02:** Card content shows child name plus a colored circle avatar with initials. No progress summary counts on cards (deferred to Phase 5 / PROG-05).
- **D-03:** Empty state shows a centered message (e.g., "Add your first child") with the FAB always visible.
- **D-04:** Children ordered by creation order — first added stays on top. Use existing `sortOrder` / `createdAtEpochMillis` on `KidEntity`; no manual drag-to-reorder in v1.

#### Add/Edit Child Flow
- **D-05:** Add and edit use a Material bottom sheet — slides up from the FAB (add) or overflow menu (edit), dismissible without navigation.
- **D-06:** Name is required: trim whitespace, reject empty/whitespace-only, cap at ~30 characters.
- **D-07:** Visual identity = initials on a colored circle. Default color derived from name hash; parent can override via a preset color swatch grid (8–12 tappable circles). Store chosen color in `avatarColorArgb` — no avatar image field.
- **D-08:** Edit and delete accessed via overflow menu (⋮) on each child card. Edit reuses the same bottom sheet form. Delete requires a confirmation dialog before removal.

#### Navigation
- **D-09:** Tapping a child card navigates to that child's catalog view route. A placeholder screen is acceptable until Phase 4 ships the badge grid.

### Claude's Discretion
- Exact preset swatch palette and hash-to-color algorithm
- Delete confirmation dialog copy and destructive-action styling
- Catalog placeholder screen content (minimal "badges coming" vs child name header only)
- Bottom sheet peek height, keyboard handling, and inline validation error text
- Hardcoded UI strings in Swedish until Phase 6 i18n (primary user locale)

### Deferred Ideas (OUT OF SCOPE)
None — discussion stayed within phase scope. Unselected gray areas (standalone avatar/color and edit/delete discussions) were partially resolved via add/edit flow choices.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| KIDS-01 | Parent can add a child with a name | `KidFormBottomSheet` + `KidFormViewModel.save()` → `KidRepository.upsert()` with trimmed name; validation per D-06 |
| KIDS-02 | Parent can assign an avatar or color theme to a child | `KidAvatarColors` palette + `ColorSwatchGrid`; store `avatarColorArgb`; default via name hash per UI-SPEC |
| KIDS-03 | Parent can view all children as cards on the home screen | `HomeViewModel` observes `kidRepository.observeAll()` → `LazyColumn` of `ChildCard`; empty state per D-03 |
| KIDS-04 | Parent can edit or remove a child profile | Overflow menu → bottom sheet (edit) or `DeleteKidDialog` (delete) → `KidRepository.upsert()` / `delete()` |
| UI-01 | Home screen uses kid-first navigation with FAB to add a child | `Scaffold` + FAB + child cards as primary content; FAB always visible; card tap navigates to catalog |
</phase_requirements>

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Kid CRUD persistence | Database / Storage (Room) | Domain (KidRepository) | `KidDao` owns SQL; repository wraps IO dispatch |
| Kid list ordering | Database / Storage (Room) | — | `ORDER BY` belongs in DAO query, not Composable sort |
| Cascade delete of progress | Database / Storage (Room FK) | — | `onDelete = CASCADE` on progress tables; no app-level orphan cleanup |
| Home screen layout & cards | Browser / Client (Compose) | — | Pure presentation; observes ViewModel state |
| Add/edit form & validation | Browser / Client (Compose) | API/Backend (ViewModel) | UI renders fields; ViewModel validates and calls repository |
| Bottom sheet / dialog UX | Browser / Client (Compose) | — | Overlay on HomeScreen, not a NavHost destination (D-05) |
| Catalog placeholder | Browser / Client (Compose) | Domain (optional VM) | Separate route; loads kid name by `kidId` |
| Type-safe navigation | Browser / Client (NavHost) | — | `ChildCatalog(kidId)` serializable route |
| Avatar initials & color hash | Domain or UI utility | — | Pure functions — testable without Android framework |
| Progress summary on cards | — | — | **Out of scope** — deferred to Phase 5 (D-02) |
| Badge grid / catalog data | — | — | **Out of scope** — Phase 4 reads `CatalogRepository` |

## Standard Stack

### Core

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Kotlin | 2.1.21 | Language | Project baseline [VERIFIED: gradle/libs.versions.toml] |
| Jetpack Compose BOM | 2025.12.01 | UI toolkit | Pins Material 3, foundation, runtime [VERIFIED: libs.versions.toml] |
| Material 3 (`material3`) | (via BOM) | Bottom sheet, FAB, cards, dialogs | Locked in UI-SPEC; `ModalBottomSheet`, `AlertDialog`, `DropdownMenu` |
| Navigation Compose | 2.9.8 | Type-safe routes | `ChildCatalog(kidId: Long)` route; `toRoute()` extraction [CITED: developer.android.com/guide/navigation/design/type-safety] |
| Room | 2.8.4 | Kid persistence | Existing `KidEntity`/`KidDao`; extend with delete/observeById [VERIFIED: codebase] |
| Lifecycle + ViewModel Compose | 2.8.7 | MVVM bridge | `collectAsStateWithLifecycle`, `viewModel()` [VERIFIED: libs.versions.toml] |
| kotlinx-coroutines | 1.9.0 | Async writes | `viewModelScope.launch(Dispatchers.IO)` pattern from `HomeViewModel` [VERIFIED: codebase] |

### Supporting

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| kotlinx-serialization-json | 1.7.3 | Route serialization | Already required for `@Serializable` routes [VERIFIED: Routes.kt] |
| JUnit 4 | 4.13.2 | Unit tests | Pure function tests (initials, validation) [VERIFIED: libs.versions.toml] |
| kotlinx-coroutines-test | 1.9.0 | ViewModel tests | **Add to `testImplementation`** — currently only `androidTestImplementation` [VERIFIED: app/build.gradle.kts] |
| androidx.room.testing | 2.8.4 | Instrumented DB tests | Kid delete + cascade verification [VERIFIED: existing androidTest deps] |
| Compose UI Test | (via BOM) | Compose semantics tests | **Optional Wave 0** — not on classpath yet; manual UAT acceptable per `human_verify_mode` |

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| `ModalBottomSheet` overlay on Home | Navigate to full-screen form route | Violates D-05 (dismissible sheet without navigation) |
| Separate `AddKidScreen` / `EditKidScreen` | Single `KidFormBottomSheet` with mode enum | Duplicates form; locked decision uses one sheet |
| Hilt ViewModel injection | Manual `ViewModelFactory` | Project uses `HomeViewModelFactory` — extend pattern, don't introduce Hilt mid-phase |
| `rememberModalBottomSheetState()` | `rememberBottomSheetState(initialValue = SheetValue.Hidden)` | Former deprecated in Material 3 recent releases [CITED: developer.android.com/reference/kotlin/androidx/compose/material3/rememberModalBottomSheetState] — verify against BOM at implementation time |

**Installation:** No new packages required. Optional Wave 0 addition:

```kotlin
// app/build.gradle.kts — if ViewModel unit tests planned
testImplementation(libs.kotlinx.coroutines.test)
```

**Version verification:**

```bash
# Already in project — no npm/pip packages for this phase
grep -E '^(room|navigation|composeBom)' gradle/libs.versions.toml
```

## Package Legitimacy Audit

> **No new external packages** are introduced in Phase 3. All UI uses Material 3 primitives from the existing Compose BOM; persistence uses existing Room stack.

| Package | Registry | Age | Downloads | Source Repo | slopcheck | Disposition |
|---------|----------|-----|-----------|-------------|-----------|-------------|
| *(none new)* | — | — | — | — | — | N/A |

**Packages removed due to slopcheck [SLOP] verdict:** none
**Packages flagged as suspicious [SUS]:** none

*slopcheck was unavailable at research time. No new installs gated.*

## Architecture Patterns

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                         HomeScreen (Compose)                         │
│  ┌──────────┐  ┌─────────────┐  ┌──────────────────────────────┐  │
│  │   FAB    │  │ ChildCard[] │  │ KidFormBottomSheet (overlay) │  │
│  │  (add)   │  │ + overflow  │  │  + ColorSwatchGrid         │  │
│  └────┬─────┘  └──────┬──────┘  └──────────────┬───────────────┘  │
│       │               │ tap card                │ save/dismiss      │
│       │               ▼                         ▼                   │
│       │         navController.navigate    KidFormViewModel         │
│       │         (ChildCatalog)                  │                   │
│       └─────────────────────────────────────────┤                   │
│                                                 ▼                   │
│                                          HomeViewModel              │
└─────────────────────────────────────────────────┬───────────────────┘
                                                  │ StateFlow / events
                                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      KidRepository (domain)                          │
│              observeAll() │ observeById() │ upsert() │ delete()      │
└─────────────────────────────────────────────────┬───────────────────┘
                                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         KidDao (Room)                                │
│         Flow<List<KidEntity>> │ @Delete │ ORDER BY sortOrder, ...    │
└─────────────────────────────────────────────────┬───────────────────┘
                                                  │ CASCADE on kidId
                                                  ▼
                              requirement_progress / badge_progress
```

### Recommended Project Structure

```
app/src/main/java/se/simmarken/
├── data/local/dao/KidDao.kt              # + delete, observeById, fix ORDER BY
├── domain/repository/KidRepository.kt    # + delete, observeById
├── domain/util/                          # optional: KidAvatarInitials.kt
├── navigation/
│   ├── Routes.kt                         # + ChildCatalog(kidId: Long)
│   ├── SimmarkenNavHost.kt               # + catalog placeholder route
│   ├── HomeViewModelFactory.kt           # existing
│   └── KidFormViewModelFactory.kt        # new
├── ui/
│   ├── home/
│   │   ├── HomeScreen.kt                 # replace placeholder
│   │   ├── HomeViewModel.kt              # expose kids list + sheet/dialog state
│   │   ├── KidFormBottomSheet.kt
│   │   ├── KidFormViewModel.kt
│   │   └── components/
│   │       ├── ChildCard.kt
│   │       ├── KidAvatar.kt
│   │       ├── EmptyState.kt
│   │       ├── ColorSwatchGrid.kt
│   │       └── DeleteKidDialog.kt
│   ├── child/
│   │   ├── ChildCatalogPlaceholderScreen.kt
│   │   └── ChildCatalogViewModel.kt      # optional — load kid name
│   └── theme/
│       └── KidAvatarColors.kt            # 10 preset ARGB values per UI-SPEC
```

### Pattern 1: Room Flow as Single Source of Truth for Kid List

**What:** `HomeViewModel` maps `kidRepository.observeAll()` to `HomeUiState(kids: List<KidEntity>)` via `stateIn`. Composable never holds authoritative kid list.
**When to use:** All home screen states (empty, populated, post-delete).
**Example:**

```kotlin
// Source: existing HomeViewModel pattern + ARCHITECTURE.md
data class HomeUiState(
    val kids: List<KidEntity> = emptyList(),
    val sheetState: KidSheetState = KidSheetState.Hidden,
    val deleteTarget: KidEntity? = null,
)

class HomeViewModel(private val kidRepository: KidRepository) : ViewModel() {
    val uiState = kidRepository.observeAll()
        .map { kids -> HomeUiState(kids = kids) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}
```

### Pattern 2: Bottom Sheet as Conditional Composition (Not Navigation)

**What:** Boolean flag + `ModalBottomSheet` only in composition tree when open; dismiss via swipe, scrim, or cancel; save triggers `sheetState.hide()` then removes flag.
**When to use:** Add/edit per D-05.
**Example:**

```kotlin
// Source: [CITED: developer.android.com/develop/ui/compose/components/bottom-sheets]
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(/* ... */) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
  val scope = rememberCoroutineScope()
  var showSheet by remember { mutableStateOf(false) }

  // ... Scaffold with FAB, child list ...

  if (showSheet) {
    ModalBottomSheet(
      onDismissRequest = { showSheet = false },
      sheetState = sheetState,
    ) {
      KidFormContent(
        modifier = Modifier.imePadding(),
        onDismiss = {
          scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) showSheet = false
          }
        },
      )
    }
  }
}
```

> **API note:** With Compose BOM 2025.12.01, check whether `rememberModalBottomSheetState` is deprecated in favor of `rememberBottomSheetState(initialValue = SheetValue.Hidden)` at compile time [CITED: developer.android.com/reference/kotlin/androidx/compose/material3/rememberModalBottomSheetState].

### Pattern 3: Type-Safe Child Catalog Route

**What:** Serializable data class with `kidId`; navigate from card tap; extract in destination composable or ViewModel via `SavedStateHandle.toRoute()`.
**When to use:** D-09 catalog placeholder (Phase 4 replaces screen, keeps route).
**Example:**

```kotlin
// Source: [CITED: developer.android.com/guide/navigation/design/type-safety]
@Serializable
data class ChildCatalog(val kidId: Long)

// SimmarkenNavHost.kt
composable<ChildCatalog> { backStackEntry ->
    val route = backStackEntry.toRoute<ChildCatalog>()
    ChildCatalogPlaceholderScreen(kidId = route.kidId, onBack = { navController.popBackStack() })
}

// Card tap
navController.navigate(ChildCatalog(kidId = kid.id))
```

### Pattern 4: Isolated Click Targets on ChildCard

**What:** Card body uses `Modifier.clickable` for navigation; overflow `IconButton` uses separate modifier and does not propagate to card click.
**When to use:** D-08 overflow menu without accidental catalog navigation.
**Anti-pattern:** Single `clickable` on entire `Row` including overflow button.

```kotlin
// Source: 03-UI-SPEC.md interaction spec
Row(modifier = Modifier.fillMaxWidth()) {
  Row(
    modifier = Modifier
      .weight(1f)
      .clickable { onCardClick() },
    verticalAlignment = Alignment.CenterVertically,
  ) {
    KidAvatar(/* ... */)
    Text(/* name */)
  }
  IconButton(onClick = onOverflowClick) {
    Icon(Icons.Default.MoreVert, contentDescription = "Alternativ för $name")
  }
}
```

### Pattern 5: KidDao Extensions for Phase 3 CRUD

**What:** Add missing DAO methods; fix ordering to match D-04.
**When to use:** Plan 03-01 data layer tasks.

```kotlin
// Source: D-04 + existing KidDao.kt
@Dao
interface KidDao {
    @Query("SELECT * FROM kids ORDER BY sortOrder ASC, createdAtEpochMillis ASC")
    fun observeAll(): Flow<List<KidEntity>>

    @Query("SELECT * FROM kids WHERE id = :kidId")
    fun observeById(kidId: Long): Flow<KidEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(kid: KidEntity): Long

    @Query("DELETE FROM kids WHERE id = :kidId")
    suspend fun deleteById(kidId: Long)
}
```

**sortOrder assignment on add:** `sortOrder = (kids.maxOfOrNull { it.sortOrder } ?: -1) + 1` computed in ViewModel from current list, or `SELECT MAX(sortOrder)` query — ViewModel approach avoids extra DAO method [ASSUMED].

### Anti-Patterns to Avoid

- **Storing sheet form state only in Composable `remember`:** Survives rotation poorly; use `KidFormViewModel` or `HomeViewModel` sheet state.
- **Navigating to add/edit screens:** Violates D-05 locked decision.
- **Manual progress cleanup on kid delete:** FK CASCADE handles it — don't duplicate in repository.
- **Ordering kids in Composable:** Sort belongs in SQL `ORDER BY` for consistency across screens.
- **Using `ORDER BY sortOrder, name`:** Current `KidDao` query conflicts with D-04 — must change to `createdAtEpochMillis` tiebreaker.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Modal bottom sheet | Custom animated panel | `ModalBottomSheet` (Material 3) | Drag handle, scrim, accessibility, keyboard insets built-in |
| Delete confirmation | Toast or snackbar only | `AlertDialog` | Destructive action needs explicit confirm per D-08 |
| Overflow actions menu | Custom popup | `DropdownMenu` + `IconButton` | Standard M3 positioning and dismiss behavior |
| Color picker | Full HSV wheel | Preset `KidAvatarColors` swatch grid (D-07) | 8–12 circles sufficient for sibling distinction |
| Navigation argument passing | String routes `"catalog/{id}"` | `@Serializable data class ChildCatalog(val kidId: Long)` | Compile-time safety already adopted in project |
| Avatar image upload | Image picker + storage | Initials + `avatarColorArgb` | Locked in D-07; no image field on entity |
| Kid list reactive updates | Manual list refresh after CRUD | Room `Flow` from `observeAll()` | PITFALLS.md — Room is single source of truth |

**Key insight:** Phase 3 is primarily wiring existing Room kid table to Material 3 patterns already on the classpath. Custom UI chrome adds risk without user value at the swim hall.

## Common Pitfalls

### Pitfall 1: KidDao Ordering Mismatch

**What goes wrong:** Children appear alphabetically by name instead of creation order; siblings reorder unexpectedly after edit.
**Why it happens:** Current query is `ORDER BY sortOrder, name` [VERIFIED: KidDao.kt] but D-04/UI-SPEC require `sortOrder ASC, createdAtEpochMillis ASC`.
**How to avoid:** Update DAO query in 03-01; set `sortOrder` incrementally on insert; never re-sort by name in UI.
**Warning signs:** "Ella" appears before "Adam" despite Adam being added first.

### Pitfall 2: Bottom Sheet Not Removed After Hide Animation

**What goes wrong:** Sheet reappears, blocks touches, or leaks composition after save/dismiss.
**Why it happens:** Setting `showSheet = false` immediately without waiting for `sheetState.hide()` animation.
**How to avoid:** `scope.launch { sheetState.hide() }.invokeOnCompletion { if (!sheetState.isVisible) showSheet = false }` [CITED: developer.android.com/develop/ui/compose/components/bottom-sheets].
**Warning signs:** Scrim persists; FAB unclickable after dismiss.

### Pitfall 3: Card Click Fires on Overflow Tap

**What goes wrong:** Parent taps ⋮ intending to edit; app navigates to catalog instead.
**Why it happens:** Single clickable modifier on entire card row.
**How to avoid:** Split click targets per Pattern 4; use `DropdownMenu` anchored to overflow button.
**Warning signs:** UAT reports "can't delete without opening badges".

### Pitfall 4: Forgetting CASCADE Already Handles Progress

**What goes wrong:** Over-engineered delete in repository trying to delete progress rows manually; or fear of deleting kids with progress blocks implementation.
**Why it happens:** Not reading Phase 1 FK definitions on progress entities.
**How to avoid:** `kidDao.deleteById(kidId)` only; add instrumented test proving progress rows disappear.
**Warning signs:** Repository injects `ProgressRepository` for delete — unnecessary.

### Pitfall 5: Upsert Overwrites `createdAtEpochMillis` on Edit

**What goes wrong:** Editing a child's name changes their list position.
**Why it happens:** Re-building `KidEntity` with `System.currentTimeMillis()` on every save.
**How to avoid:** On edit, preserve `id`, `createdAtEpochMillis`, and `sortOrder` from existing entity; only update `name` and `avatarColorArgb`.
**Warning signs:** Kid moves in list after rename.

### Pitfall 6: Yellow Swatch Fails Contrast with White Initials

**What goes wrong:** Initials unreadable on `#FDD835` yellow swatch.
**Why it happens:** UI-SPEC notes yellow may need `Color.Black` text.
**How to avoid:** `KidAvatar` checks luminance or hardcodes yellow index → black text [ASSUMED: UI-SPEC discretion].
**Warning signs:** WCAG contrast failure on yellow avatar in dark/light themes.

## Code Examples

Verified patterns from official sources and existing codebase:

### Kid Avatar Initials (Pure Function)

```kotlin
// Source: 03-UI-SPEC.md KidAvatar spec
object KidAvatarInitials {
    fun fromName(name: String): String {
        val trimmed = name.trim()
        val words = trimmed.split(Regex("\\s+")).filter { it.isNotEmpty() }
        return when {
            words.size >= 2 -> "${words[0].first()}${words[1].first()}".uppercase()
            trimmed.length >= 2 -> trimmed.take(2).uppercase()
            trimmed.isNotEmpty() -> trimmed.first().uppercase()
            else -> "?"
        }
    }
}
```

### Default Avatar Color from Name Hash

```kotlin
// Source: 03-UI-SPEC.md Color section
object KidAvatarColors {
    val palette: List<Int> = listOf(
        0xFFE53935.toInt(), 0xFFFB8C00.toInt(), 0xFFFDD835.toInt(),
        0xFF43A047.toInt(), 0xFF1E88E5.toInt(), 0xFF8E24AA.toInt(),
        0xFFD81B60.toInt(), 0xFF00ACC1.toInt(), 0xFF6D4C41.toInt(),
        0xFF546E7A.toInt(),
    )

    fun defaultForName(name: String): Int {
        val index = kotlin.math.abs(name.trim().hashCode()) % palette.size
        return palette[index]
    }
}
```

### Name Validation (ViewModel)

```kotlin
// Source: D-06 + UI-SPEC copywriting
fun validateName(raw: String): String? {
    val trimmed = raw.trim()
    return when {
        trimmed.isEmpty() -> "Ange ett namn"
        trimmed.length > 30 -> "Namnet får vara högst 30 tecken"
        else -> null
    }
}
```

### Type-Safe Navigation (Existing Project Pattern)

```kotlin
// Source: [CITED: developer.android.com/guide/navigation/design/type-safety] + Routes.kt
@Serializable
object Home

@Serializable
data class ChildCatalog(val kidId: Long)
```

### ViewModel Factory (Existing Pattern)

```kotlin
// Source: HomeViewModelFactory.kt — extend for KidFormViewModel
class KidFormViewModelFactory(
    private val kidRepository: KidRepository,
    private val kidId: Long?, // null = add mode
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KidFormViewModel::class.java)) {
            return KidFormViewModel(kidRepository, kidId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| String-based Nav routes | `@Serializable` type-safe routes | Navigation 2.8.0 (2024) | Project already uses `composable<Home>` — extend with `ChildCatalog` |
| `rememberModalBottomSheetState` | `rememberBottomSheetState(SheetValue.Hidden)` | Material3 recent releases | Verify at implementation; deprecated overload may still compile with warning |
| LiveData in ViewModels | `StateFlow` + `stateIn` | Compose era | Project standard — keep pattern |
| Full-screen add/edit forms | Modal bottom sheet for quick CRUD | This phase (D-05) | Swim-hall one-handed UX |

**Deprecated/outdated:**
- `HomeScreen` debug `addTestKid()` button — remove in 03-02
- `HomePlaceholderScreen.kt` — unused; can delete or leave (planner discretion)

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | `rememberModalBottomSheetState` still works on Compose BOM 2025.12.01 | Pattern 2 | Compile warning or API migration needed to `rememberBottomSheetState` |
| A2 | `sortOrder = max + 1` in ViewModel is sufficient without DAO `MAX(sortOrder)` query | Pattern 5 | Race if concurrent inserts — unlikely with single-user parent app |
| A3 | Yellow swatch (index 2) needs black initials for contrast | Pitfall 6 | Minor readability issue on one color |
| A4 | `ChildCatalogViewModel` optional — can load kid name via `observeById` in screen | Project Structure | Slightly messier Composable if skipped |
| A5 | No Compose UI test library needed for MVP — instrumented DAO tests + manual UAT | Validation Architecture | UI regressions caught later or manually |

## Open Questions

1. **Should `HomeViewModel` own sheet/dialog state or split to separate form VM only?**
   - What we know: UI-SPEC lists both `HomeViewModel` and `KidFormViewModel`; sheet state matrix in UI-SPEC shows Home orchestrates sheet open/close.
   - What's unclear: Whether delete dialog state lives in Home or form VM.
   - Recommendation: `HomeViewModel` owns `sheetMode` (Hidden/Add/Edit) and `deleteTarget`; `KidFormViewModel` owns form fields and validation. Planner can merge if simpler for MVP.

2. **Compose UI instrumented tests in this phase?**
   - What we know: `human_verify_mode: end-of-phase`; no `compose-ui-test` dependency yet.
   - What's unclear: Whether Nyquist requires automated UI tests for KIDS-03/UI-01.
   - Recommendation: Wave 0 adds DAO/repository instrumented tests; defer Compose UI tests to Phase 4 unless planner wants `createComposeRule` for smoke test.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| Android SDK / AGP | Build | ✓ | AGP 8.13.2, compileSdk 35 | — |
| Gradle | Build | ✓ | 8.13 (wrapper) | — |
| Kotlin | Compile | ✓ | 2.1.21 | — |
| Android device/emulator | Manual UAT | ? | — | Required for end-of-phase human verify |
| Node.js | GSD tools only | ✓ | v22.10.0 | Not needed for app implementation |

**Missing dependencies with no fallback:**
- None for code implementation (all libraries already in `build.gradle.kts`).

**Missing dependencies with fallback:**
- Android emulator not verified at research time — manual UAT blocked until device available.

## Validation Architecture

### Test Framework

| Property | Value |
|----------|-------|
| Framework | JUnit 4.13.2 (unit), AndroidJUnit4 + Room Testing 2.8.4 (instrumented) |
| Config file | none — standard Gradle `app/build.gradle.kts` test deps |
| Quick run command | `./gradlew :app:testDebugUnitTest --tests "se.simmarken.domain.*"` |
| Full suite command | `./gradlew :app:testDebugUnitTest :app:connectedDebugAndroidTest` |

### Phase Requirements → Test Map

| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| KIDS-01 | Add child with name persists | instrumented | `./gradlew :app:connectedDebugAndroidTest --tests "*.KidDaoTest.addKid"` | ❌ Wave 0 |
| KIDS-01 | Name validation rejects empty/>30 | unit | `./gradlew :app:testDebugUnitTest --tests "*.KidNameValidationTest"` | ❌ Wave 0 |
| KIDS-02 | Default color from name hash | unit | `./gradlew :app:testDebugUnitTest --tests "*.KidAvatarColorsTest"` | ❌ Wave 0 |
| KIDS-02 | Initials from name (1 word, 2 words) | unit | `./gradlew :app:testDebugUnitTest --tests "*.KidAvatarInitialsTest"` | ❌ Wave 0 |
| KIDS-03 | observeAll returns kids in sortOrder/createdAt order | instrumented | `./gradlew :app:connectedDebugAndroidTest --tests "*.KidDaoTest.observeAllOrder"` | ❌ Wave 0 |
| KIDS-04 | Edit preserves id/createdAt/sortOrder | instrumented | `./gradlew :app:connectedDebugAndroidTest --tests "*.KidDaoTest.upsertPreservesFields"` | ❌ Wave 0 |
| KIDS-04 | Delete cascades progress rows | instrumented | `./gradlew :app:connectedDebugAndroidTest --tests "*.KidDaoTest.deleteCascadesProgress"` | ❌ Wave 0 |
| UI-01 | Home shows FAB + cards | manual | End-of-phase human verify on device | — |

### Sampling Rate

- **Per task commit:** `./gradlew :app:testDebugUnitTest` (unit tests only, <30s)
- **Per wave merge:** `./gradlew :app:connectedDebugAndroidTest` (DAO tests on device/emulator)
- **Phase gate:** Full suite green + human UAT checklist from UI-SPEC state matrix before `/gsd-verify-work`

### Wave 0 Gaps

- [ ] `app/src/test/java/se/simmarken/domain/util/KidAvatarInitialsTest.kt` — covers KIDS-02 initials logic
- [ ] `app/src/test/java/se/simmarken/ui/theme/KidAvatarColorsTest.kt` — covers KIDS-02 default color hash
- [ ] `app/src/test/java/se/simmarken/domain/validation/KidNameValidationTest.kt` — covers KIDS-01/D-06
- [ ] `app/src/androidTest/java/se/simmarken/data/local/KidDaoTest.kt` — covers KIDS-01/03/04 CRUD, ordering, cascade delete
- [ ] `testImplementation(libs.kotlinx.coroutines.test)` — enable ViewModel unit tests if planned
- [ ] Fix `KidDao.observeAll()` ORDER BY to match D-04 (prerequisite for ordering test)

## Security Domain

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|------------------|
| V2 Authentication | no | N/A — local single-user parent app, no accounts |
| V3 Session Management | no | N/A |
| V4 Access Control | no | N/A — no multi-user |
| V5 Input Validation | yes | Trim name, max 30 chars, reject whitespace-only in ViewModel before persist |
| V6 Cryptography | no | N/A this phase |

### Known Threat Patterns for Android Compose + Room

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| SQL injection via kid name | Tampering | Room parameterized queries — never string-concat SQL |
| Unvalidated input stored | Tampering | ViewModel validation before `upsert()` |
| Accidental data loss on delete | Denial of Service (user data) | Confirmation dialog per D-08; CASCADE only after explicit confirm |
| Sensitive data in logs | Information Disclosure | Don't log kid names in production Timber/debug calls [ASSUMED] |

## Project Constraints (from .cursor/rules/)

No `.cursor/rules/` directory exists in the project workspace. No additional project-level agent constraints beyond user rules and GSD config (`security_enforcement: true`, `nyquist_validation: true`).

## Sources

### Primary (HIGH confidence)
- Existing codebase — `KidEntity.kt`, `KidDao.kt`, `KidRepository.kt`, `HomeViewModel.kt`, `SimmarkenNavHost.kt`, `Routes.kt`, progress entity FK definitions
- `03-CONTEXT.md` — locked decisions D-01 through D-09
- `03-UI-SPEC.md` — component inventory, copywriting, spacing, interaction specs
- `gradle/libs.versions.toml` — verified dependency versions

### Secondary (MEDIUM confidence)
- [Android Bottom sheets guide](https://developer.android.com/develop/ui/compose/components/bottom-sheets) — conditional composition, programmatic hide [CITED]
- [Navigation type safety](https://developer.android.com/guide/navigation/design/type-safety) — `@Serializable` routes, `toRoute()` [CITED]
- [rememberModalBottomSheetState API](https://developer.android.com/reference/kotlin/androidx/compose/material3/rememberModalBottomSheetState) — deprecation note toward `rememberBottomSheetState` [CITED]
- `.planning/research/ARCHITECTURE.md`, `STACK.md`, `PITFALLS.md` — MVVM, Room Flow, anti-patterns

### Tertiary (LOW confidence)
- WebSearch synthesis on ModalBottomSheet deprecation — verified against official API reference URL; exact BOM behavior needs compile-time check (A1)

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — all libraries already in project; no new package risk
- Architecture: HIGH — extends established Phase 1 patterns; UI-SPEC is detailed and checker-approved
- Pitfalls: HIGH — KidDao ordering mismatch verified in source; FK cascade verified in entities

**Research date:** 2026-07-23
**Valid until:** 2026-08-22 (stable Android stack; check Material3 bottom sheet API if BOM bumps)
