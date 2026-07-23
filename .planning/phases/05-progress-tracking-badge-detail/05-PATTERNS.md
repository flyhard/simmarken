# Phase 5: Progress Tracking & Badge Detail - Pattern Map

**Mapped:** 2026-07-23
**Files analyzed:** 17 new/modified files
**Analogs found:** 15 / 17

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|-------------------|------|-----------|----------------|---------------|
| `ui/badge/BadgeDetailScreen.kt` | component | request-response | `ui/badge/BadgeDetailPlaceholderScreen.kt` | exact |
| `ui/badge/components/RequirementChecklist.kt` | component | request-response | `ui/child/components/BadgeGrid.kt` (Column list) | partial |
| `ui/badge/components/RequirementChecklistRow.kt` | component | event-driven | `ui/home/components/ChildCard.kt` + `ui/child/components/BadgeGridItem.kt` | role-match |
| `ui/badge/components/ZeroRequirementNote.kt` | component | request-response | `ui/badge/BadgeDetailPlaceholderScreen.kt` (stub text) | partial |
| `ui/badge/components/PurchaseToggleRow.kt` | component | event-driven | `ui/home/KidFormBottomSheet.kt` (label + control Row) | partial |
| `ui/badge/components/UncheckPurchaseDialog.kt` | component | event-driven | `ui/home/components/DeleteKidDialog.kt` | exact |
| `ui/badge/BadgeDetailViewModel.kt` | hook (ViewModel) | pub-sub + CRUD | `ui/child/ChildCatalogViewModel.kt` + `ui/home/KidFormViewModel.kt` | exact |
| `domain/model/BadgeDetailUiState.kt` | model | transform | `domain/model/BadgeDetailUiState.kt` (existing) | exact |
| `domain/model/RequirementRowUiModel.kt` | model | transform | `domain/model/CatalogUiModels.kt` (`BadgeCellUiModel`) | exact |
| `domain/model/KidProgressSummary.kt` | model | transform | `ui/home/HomeViewModel.kt` (`HomeUiState`) | role-match |
| `domain/repository/ProgressRepository.kt` | service | CRUD | `domain/repository/ProgressRepositoryImpl.kt` | exact |
| `ui/home/HomeViewModel.kt` | hook (ViewModel) | pub-sub + transform | `ui/child/ChildCatalogViewModel.kt` | exact |
| `ui/home/HomeScreen.kt` | component | request-response | `ui/home/HomeScreen.kt` (existing) | exact |
| `ui/home/components/ChildCard.kt` | component | request-response | `ui/home/components/ChildCard.kt` (existing) | exact |
| `navigation/SimmarkenNavHost.kt` | route | request-response | `navigation/SimmarkenNavHost.kt` (`BadgeDetail` block) | exact |
| `navigation/HomeViewModelFactory.kt` | config | request-response | `navigation/ChildCatalogViewModelFactory.kt` | exact |
| `domain/KidProgressSummaryCalculator.kt` | utility | transform | `domain/BadgeCatalogMapper.kt` + `domain/BadgeStateCalculator.kt` | role-match |

**Delete after wiring:** `ui/badge/BadgeDetailPlaceholderScreen.kt` — replaced by `BadgeDetailScreen.kt`.

## Pattern Assignments

### `ui/badge/BadgeDetailScreen.kt` (component, request-response)

**Analog:** `app/src/main/java/se/simmarken/ui/badge/BadgeDetailPlaceholderScreen.kt`

**Scaffold + collectAsState + loading/missing states** (lines 41-79):
```kotlin
val uiState by viewModel.uiState.collectAsStateWithLifecycle()

Scaffold(
    topBar = {
        CenterAlignedTopAppBar(
            title = { Text(uiState.nameSv) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Tillbaka",
                    )
                }
            },
        )
    },
) { innerPadding ->
    if (uiState.badgeMissing) {
        Box(/* centered "Märket hittades inte" */) { ... }
    } else if (uiState.isLoading) {
        Box(/* CircularProgressIndicator */) { ... }
    } else {
        // populated content
    }
}
```

**Pin + name header** (lines 89-102):
```kotlin
BadgePinVisual(
    imageAssetPath = uiState.imageAssetPath,
    categoryCode = uiState.categoryCode,
    visualState = uiState.visualState,
    progressFraction = uiState.progressFraction,
    contentDescription = "${uiState.nameSv}, ${badgeStateLabel(uiState.visualState)}",
    size = BadgePinSize.Detail,
    totalRequirements = uiState.totalRequirements,
)
Text(
    text = uiState.nameSv,
    style = MaterialTheme.typography.titleLarge,
    modifier = Modifier.padding(top = 24.dp),
)
```

**Apply:** Keep scaffold/loading/missing from placeholder. Replace centered `Column` with **fixed header** (`BadgePinVisual` + name + `"X av Y klara"` subtitle) + **scrollable body** (`Column(Modifier.verticalScroll(rememberScrollState()))` with checklist or `ZeroRequirementNote`, then `PurchaseToggleRow`). Reuse private `badgeStateLabel()` from placeholder (lines 28-33). Wire `viewModel.toggleRequirement`, `setGotten`, `requestClearGotten`, `confirmClearGotten`. Show `UncheckPurchaseDialog` when `uiState.showUncheckPurchaseDialog`.

---

### `ui/badge/components/RequirementChecklist.kt` (component, request-response)

**Analog:** `app/src/main/java/se/simmarken/ui/child/components/BadgeGrid.kt` (list composition pattern)

**Column of keyed children** — mirror how `BadgeGrid` iterates sections; use simple `Column` since checklist is small (3–8 items):
```kotlin
@Composable
fun RequirementChecklist(
    requirements: List<RequirementRowUiModel>,
    onToggle: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        requirements.forEach { requirement ->
            RequirementChecklistRow(
                requirement = requirement,
                onToggle = { onToggle(requirement.id) },
            )
        }
    }
}
```

**Apply:** Render only when `totalRequirements > 0`. Order by `sortOrder` (already sorted from DAO `ORDER BY sortOrder, code`). Parent passes `uiState.requirements`.

---

### `ui/badge/components/RequirementChecklistRow.kt` (component, event-driven)

**Analog:** `app/src/main/java/se/simmarken/ui/home/components/ChildCard.kt` (clickable Row) + `BadgeGridItem.kt`

**Clickable full-width Row** (ChildCard lines 49-67):
```kotlin
Row(
    modifier = Modifier
        .weight(1f)
        .clickable(onClick = onCardClick),
    verticalAlignment = Alignment.CenterVertically,
) {
    // leading + text
    Text(
        text = name,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.weight(1f),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
```

**Apply:** `Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable { onToggle() })` with leading `Checkbox(checked, onCheckedChange = { onToggle() })`, text `Modifier.weight(1f)` with `maxLines = Int.MAX_VALUE` (wrap). 12dp gap checkbox-to-text, 8dp vertical padding. **No Checkbox/Switch in codebase yet** — first Material 3 `Checkbox` usage; follow UI-SPEC colors (`primary` when checked).

---

### `ui/badge/components/ZeroRequirementNote.kt` (component, request-response)

**Analog:** `BadgeDetailPlaceholderScreen.kt` stub text (lines 103-108)

```kotlin
Text(
    text = "Checklista kommer snart",
    style = MaterialTheme.typography.bodyLarge,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier = Modifier.padding(top = 16.dp),
)
```

**Apply:** Replace copy with `"Inga kunskapskrav"`, `bodyLarge`, `onSurfaceVariant`, 16dp vertical padding. Shown only when `totalRequirements == 0`.

---

### `ui/badge/components/PurchaseToggleRow.kt` (component, event-driven)

**Analog:** `app/src/main/java/se/simmarken/ui/home/KidFormBottomSheet.kt` (Row with label + control, lines 81-95)

```kotlin
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.End,
    verticalAlignment = Alignment.CenterVertically,
) {
    TextButton(onClick = onDismiss) { Text("Avbryt") }
    Button(onClick = viewModel::save, enabled = isReadyToSave && !isSaving) {
        Text(saveLabel)
    }
}
```

**Apply:** `Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), SpaceBetween, CenterVertically)` — label `"Fysiskt märke köpt"` (`bodyLarge`, greyed at 38% opacity when disabled), trailing `Switch(checked = isGotten, enabled = isPurchaseEnabled, onCheckedChange)`. Optional `HorizontalDivider` above with 24dp top padding. **First `Switch` in codebase** — M3 defaults per UI-SPEC.

---

### `ui/badge/components/UncheckPurchaseDialog.kt` (component, event-driven)

**Analog:** `app/src/main/java/se/simmarken/ui/home/components/DeleteKidDialog.kt`

**Full AlertDialog pattern** (lines 9-35):
```kotlin
@Composable
fun DeleteKidDialog(
    name: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ta bort $name?") },
        text = {
            Text("All simmarke-framsteg för $name tas bort. Detta går inte att ångra.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Ta bort",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Avbryt")
            }
        },
    )
}
```

**Apply:** Copy structure exactly. Title `"Ta bort köpt-markering?"`, body per UI-SPEC, confirm `"Ta bort markering"` with `error` color, dismiss `"Avbryt"`. Screen shows dialog when ViewModel sets `showUncheckPurchaseDialog`; on dismiss revert switch visually (state still `isGotten = true` until confirm).

---

### `ui/badge/BadgeDetailViewModel.kt` (hook, pub-sub + CRUD)

**Analog (reads):** `app/src/main/java/se/simmarken/ui/child/ChildCatalogViewModel.kt` + existing `BadgeDetailViewModel.kt`

**Existing combine + BadgeCatalogMapper** (BadgeDetailViewModel lines 21-53):
```kotlin
val uiState = catalogRepository.observeBadgeById(badgeId)
    .flatMapLatest { badge ->
        if (badge == null) {
            flowOf(BadgeDetailUiState(badgeMissing = true, isLoading = false))
        } else {
            combine(
                catalogRepository.observeCategoryById(badge.categoryId),
                catalogRepository.observeRequirements(badgeId),
                progressRepository.observeRequirementProgress(kidId),
                progressRepository.observeBadgeProgress(kidId),
            ) { category, requirements, reqProgress, badgeProgress ->
                val requirementProgressById =
                    reqProgress.associate { it.requirementId to it.isAchieved }
                val isGotten = badgeProgress.any { it.badgeId == badgeId && it.isGotten }
                val cell = BadgeCatalogMapper.toBadgeCellUiModel(...)
                BadgeDetailUiState(...)
            }
        }
    }
    .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), ...)
```

**Analog (writes):** `KidFormViewModel.save()` (lines 81-112):
```kotlin
viewModelScope.launch(Dispatchers.IO) {
    try {
        kidRepository.upsert(...)
    } finally {
        _isSaving.value = false
    }
}
```

**Extend reads:** Map `requirements` to `List<RequirementRowUiModel>` (id, textSv, isAchieved from `requirementProgressById[req.id] ?: false`). Add `isGotten`, `isPurchaseEnabled` (derived from `visualState`), `showUncheckPurchaseDialog`.

**Extend writes:**
- `toggleRequirement(requirementId)` → upsert `RequirementProgressEntity(kidId, requirementId, isAchieved, achievedAtEpochMillis)`. On last-requirement achieve, upsert `BadgeProgressEntity` with `achievedAtEpochMillis` write-once (retain if already set). If unchecking while `isGotten`, auto-clear gotten fields (D-10).
- `setGotten(true)` → upsert `BadgeProgressEntity` with `isGotten = true`, `gottenAtEpochMillis = now`.
- `requestClearGotten()` / `confirmClearGotten()` → dialog flow like `HomeViewModel.requestDelete` / `confirmDelete`.

**Dialog state pattern** (HomeViewModel lines 29-30, 59-66):
```kotlin
private val deleteTarget = MutableStateFlow<KidEntity?>(null)
fun requestDelete(kid: KidEntity) { deleteTarget.value = kid }
fun dismissDelete() { deleteTarget.value = null }
```

---

### `domain/model/BadgeDetailUiState.kt` (model, transform)

**Analog:** existing file — extend in place

**Current shape** (lines 3-13):
```kotlin
data class BadgeDetailUiState(
    val nameSv: String = "",
    val imageAssetPath: String? = null,
    val categoryCode: String = "",
    val visualState: BadgeVisualState = BadgeVisualState.LOCKED,
    val progressFraction: Float = 0f,
    val achievedCount: Int = 0,
    val totalRequirements: Int = 0,
    val badgeMissing: Boolean = false,
    val isLoading: Boolean = true,
)
```

**Add:** `requirements: List<RequirementRowUiModel> = emptyList()`, `isGotten: Boolean = false`, `isPurchaseEnabled: Boolean = false`, `showUncheckPurchaseDialog: Boolean = false`. Derive `isPurchaseEnabled` from `visualState` in ViewModel (ACHIEVED_TO_BUY, GOTTEN, or zero-requirement).

---

### `domain/model/RequirementRowUiModel.kt` (model, transform)

**Analog:** `app/src/main/java/se/simmarken/domain/model/CatalogUiModels.kt`

```kotlin
data class BadgeCellUiModel(
    val id: Long,
    val nameSv: String,
    val imageAssetPath: String?,
    val categoryCode: String,
    val visualState: BadgeVisualState,
    val progressFraction: Float,
    val achievedCount: Int,
    val totalRequirements: Int,
)
```

**Apply:**
```kotlin
data class RequirementRowUiModel(
    val id: Long,
    val textSv: String,
    val isAchieved: Boolean,
)
```

---

### `domain/model/KidProgressSummary.kt` (model, transform) — optional

**Analog:** `HomeUiState` + per-badge state from `BadgeCellUiModel`

```kotlin
data class KidProgressSummary(
    val kidId: Long,
    val inProgressCount: Int = 0,
    val toBuyCount: Int = 0,
)
```

Or embed `Map<Long, KidProgressSummary>` in extended `HomeUiState`. Count only `IN_PROGRESS` and `ACHIEVED_TO_BUY` via `BadgeStateCalculator.compute`.

---

### `domain/repository/ProgressRepository.kt` (service, CRUD)

**Analog:** `app/src/main/java/se/simmarken/domain/repository/ProgressRepositoryImpl.kt`

**Already wired** (lines 17-21):
```kotlin
override suspend fun upsertRequirementProgress(progress: RequirementProgressEntity) =
    requirementProgressDao.upsert(progress)

override suspend fun upsertBadgeProgress(progress: BadgeProgressEntity) =
    badgeProgressDao.upsert(progress)
```

**Apply:** No DAO changes needed. ViewModels call existing upserts. Optional helper methods (discretion): `upsertRequirementAchieved(kidId, requirementId, achieved)` — keep logic in ViewModel if simpler.

**Entity fields** (`BadgeProgressEntity` lines 29-35):
```kotlin
data class BadgeProgressEntity(
    val kidId: Long,
    val badgeId: Long,
    val isGotten: Boolean = false,
    val achievedAtEpochMillis: Long? = null,
    val gottenAtEpochMillis: Long? = null,
)
```

---

### `ui/home/HomeViewModel.kt` (hook, pub-sub + transform)

**Analog:** `app/src/main/java/se/simmarken/ui/child/ChildCatalogViewModel.kt`

**Multi-flow combine with progress** (ChildCatalogViewModel lines 71-90):
```kotlin
combine(
    flowOf(categoryBadges),
    progressRepository.observeRequirementProgress(kidId),
    progressRepository.observeBadgeProgress(kidId),
    catalogRepository.observeAllRequirements(),
) { pairs, reqProgress, badgeProgress, allRequirements ->
    val requirementProgressById = reqProgress.associate { it.requirementId to it.isAchieved }
    val gottenByBadgeId = badgeProgress.associate { it.badgeId to it.isGotten }
    // map each badge through BadgeCatalogMapper.toBadgeCellUiModel → count visualState
}
```

**Apply:** Inject `catalogRepository`, `progressRepository`. Combine `kidRepository.observeAll()` with catalog-wide badge list (both catalogs: iterate `observeCatalogs()` → categories → badges, or add `observeAllBadges()` if needed). For each kid, compute `inProgressCount` / `toBuyCount` using `BadgeStateCalculator` on every badge. Extend `HomeUiState` with `summariesByKidId: Map<Long, KidProgressSummary>` or `List<KidWithSummary>`.

**Factory update** — mirror `ChildCatalogViewModelFactory` constructor deps.

---

### `ui/home/HomeScreen.kt` (component, request-response)

**Analog:** existing `HomeScreen.kt`

**ChildCard wiring** (lines 77-90):
```kotlin
items(uiState.kids, key = { it.id }) { kid ->
    ChildCard(
        name = kid.name,
        avatarColorArgb = kid.avatarColorArgb,
        onCardClick = { navController.navigate(ChildCatalog(kidId = kid.id)) },
        onEditClick = { viewModel.openEditSheet(kid.id) },
        onDeleteClick = { viewModel.requestDelete(kid) },
    )
}
```

**Apply:** Pass `inProgressCount` and `toBuyCount` from `uiState.summariesByKidId[kid.id]` (default 0). Dialog pattern unchanged.

---

### `ui/home/components/ChildCard.kt` (component, request-response)

**Analog:** existing `ChildCard.kt` — extend in place

**Current name-only text** (lines 59-67):
```kotlin
Text(
    text = name,
    style = MaterialTheme.typography.bodyLarge,
    modifier = Modifier
        .weight(1f)
        .padding(start = 16.dp),
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
)
```

**Apply:** Wrap name + subtitles in `Column(Modifier.weight(1f).padding(start = 16.dp))`. Add params `inProgressCount: Int = 0`, `toBuyCount: Int = 0`. Show `"$count pågår"` / `"$count att köpa"` as `bodySmall` + `onSurfaceVariant` only when count > 0. Adjust `heightIn(min = if (hasSubtitles) 80.dp else 72.dp)`.

---

### `navigation/SimmarkenNavHost.kt` (route, request-response)

**Analog:** existing `BadgeDetail` composable block (lines 48-62)

```kotlin
composable<BadgeDetail> { backStackEntry ->
    val route = backStackEntry.toRoute<BadgeDetail>()
    val detailViewModel: BadgeDetailViewModel = viewModel(
        factory = BadgeDetailViewModelFactory(
            kidId = route.kidId,
            badgeId = route.badgeId,
            catalogRepository = application.container.catalogRepository,
            progressRepository = application.container.progressRepository,
        ),
    )
    BadgeDetailPlaceholderScreen(
        viewModel = detailViewModel,
        onBack = { navController.popBackStack() },
    )
}
```

**Apply:** Swap `BadgeDetailPlaceholderScreen` → `BadgeDetailScreen`. Update `HomeViewModelFactory` to pass `catalogRepository` + `progressRepository`.

---

### `navigation/HomeViewModelFactory.kt` (config, request-response)

**Analog:** `app/src/main/java/se/simmarken/navigation/ChildCatalogViewModelFactory.kt`

```kotlin
class ChildCatalogViewModelFactory(
    private val kidRepository: KidRepository,
    private val catalogRepository: CatalogRepository,
    private val progressRepository: ProgressRepository,
    private val kidId: Long,
) : ViewModelProvider.Factory { ... }
```

**Apply:** Add `catalogRepository`, `progressRepository` to `HomeViewModelFactory`; pass to `HomeViewModel` constructor.

---

### `domain/KidProgressSummaryCalculator.kt` (utility, transform) — optional discretion

**Analog:** `BadgeCatalogMapper.toBadgeCellUiModel` + `BadgeStateCalculator.compute`

```kotlin
val visualState = BadgeStateCalculator.compute(
    totalRequirements = totalRequirements,
    achievedCount = achievedCount,
    isGotten = isGotten,
)
```

**Apply:** Pure function `fun countForKid(badges, requirementsByBadgeId, reqProgress, badgeProgress): KidProgressSummary` — increment `inProgressCount` for `IN_PROGRESS`, `toBuyCount` for `ACHIEVED_TO_BUY`. Exclude `GOTTEN` and `LOCKED`.

---

## Shared Patterns

### MVVM + StateFlow (reads)
**Source:** `BadgeDetailViewModel.kt`, `ChildCatalogViewModel.kt`, `HomeViewModel.kt`
**Apply to:** Extended ViewModels
```kotlin
val uiState = combine(...).stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000),
    initialValue = ...,
)
```

### Composable state collection
**Source:** `BadgeDetailPlaceholderScreen.kt` line 41
```kotlin
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
```

### Room writes on Dispatchers.IO
**Source:** `KidFormViewModel.kt` lines 81-82, `HomeViewModel.confirmDelete` lines 71-73
**Apply to:** `BadgeDetailViewModel` toggle/write methods
```kotlin
viewModelScope.launch(Dispatchers.IO) {
    progressRepository.upsertRequirementProgress(...)
}
```

### Single BadgeStateCalculator path (Pitfall 3)
**Source:** `BadgeCatalogMapper.kt` lines 26-34
**Apply to:** Detail, grid, home counts — never duplicate visual-state if/else
```kotlin
visualState = BadgeStateCalculator.compute(
    totalRequirements = totalRequirements,
    achievedCount = achievedCount,
    isGotten = isGotten,
)
```

### Progress default: absent row = not achieved
**Source:** `ChildCatalogViewModel.kt` line 77
```kotlin
val requirementProgressById = reqProgress.associate { it.requirementId to it.isAchieved }
// requirementProgressById[req.id] ?: false
```

### achievedAt write-once (Phase 1 D-03)
**Source:** CONTEXT D-09, D-18
**Apply on last-requirement check:**
```kotlin
val achievedAt = existingBadgeProgress?.achievedAtEpochMillis
    ?: if (allAchieved) System.currentTimeMillis() else null
// Never clear achievedAt on uncheck
```

### Confirmation dialog flow
**Source:** `HomeViewModel` + `DeleteKidDialog` + `HomeScreen` lines 118-124
**Apply to:** Köpt undo — `requestClearGotten()` sets flag; screen renders `UncheckPurchaseDialog`; `confirmClearGotten()` writes on IO.

### Swedish hardcoded strings
**Source:** `DeleteKidDialog.kt`, `BadgeDetailPlaceholderScreen.kt`
**Apply to:** All Phase 5 copy per UI-SPEC Copywriting Contract

### AppContainer repositories
**Source:** `di/AppContainer.kt` lines 32-37
```kotlin
val progressRepository: ProgressRepository = ProgressRepositoryImpl(
    requirementProgressDao = database.requirementProgressDao(),
    badgeProgressDao = database.badgeProgressDao(),
)
```

### Reuse badgeStateLabel helper
**Source:** `BadgeDetailPlaceholderScreen.kt` lines 28-33, duplicated in `BadgeGridItem.kt`
**Apply:** Extract to shared util or duplicate in `BadgeDetailScreen` — same 4-tier Swedish labels for content descriptions.

## No Analog Found

| File | Role | Data Flow | Reason |
|------|------|-----------|--------|
| `ui/badge/components/RequirementChecklistRow.kt` | component | event-driven | No `Checkbox` in codebase; use Material 3 `Checkbox` per UI-SPEC |
| `ui/badge/components/PurchaseToggleRow.kt` | component | event-driven | No `Switch` in codebase; use Material 3 `Switch` per UI-SPEC |
| `BadgeDetailScreen` scroll layout | component | request-response | No `verticalScroll` in codebase; follow UI-SPEC fixed header + `rememberScrollState()` |

## Metadata

**Analog search scope:** `app/src/main/java/se/simmarken/` (ui/badge, ui/home, ui/child, domain, navigation, data, di), `app/src/test/`
**Files scanned:** ~40 Kotlin sources
**Pattern extraction date:** 2026-07-23
