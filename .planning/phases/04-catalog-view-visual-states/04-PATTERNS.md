# Phase 4: Catalog View & Visual States - Pattern Map

**Mapped:** 2026-07-23
**Files analyzed:** 24 new/modified files
**Analogs found:** 20 / 24

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|-------------------|------|-----------|----------------|---------------|
| `domain/BadgeStateCalculator.kt` | utility | transform | `domain/validation/KidNameValidation.kt` | exact |
| `domain/model/BadgeVisualState.kt` | model | transform | `ui/home/HomeViewModel.kt` (`KidSheetState`) | role-match |
| `domain/model/CatalogUiModels.kt` | model | transform | `ui/home/HomeViewModel.kt` (`HomeUiState`) | exact |
| `ui/child/ChildCatalogViewModel.kt` | hook (ViewModel) | pub-sub | `ui/home/HomeViewModel.kt` | exact |
| `ui/child/ChildCatalogScreen.kt` | component | request-response | `ui/child/ChildCatalogPlaceholderScreen.kt` | exact |
| `ui/child/components/CatalogTabRow.kt` | component | event-driven | `ui/child/ChildCatalogPlaceholderScreen.kt` (Scaffold) | partial |
| `ui/child/components/CategoryStickyHeader.kt` | component | request-response | `ui/home/HomeScreen.kt` (topBar title) | partial |
| `ui/child/components/BadgeGrid.kt` | component | request-response | `ui/home/HomeScreen.kt` (`LazyColumn`) | role-match |
| `ui/child/components/BadgeGridItem.kt` | component | event-driven | `ui/home/components/ChildCard.kt` | role-match |
| `ui/child/components/CatalogEmptyState.kt` | component | request-response | `ui/home/components/EmptyState.kt` | exact |
| `ui/badge/BadgeDetailPlaceholderScreen.kt` | component | request-response | `ui/child/ChildCatalogPlaceholderScreen.kt` | exact |
| `ui/badge/BadgeDetailViewModel.kt` | hook (ViewModel) | pub-sub | `ui/child/ChildCatalogViewModel.kt` | exact |
| `ui/components/BadgePinVisual.kt` | component | file-I/O | `ui/home/components/KidAvatar.kt` + `ui/badge/BadgePlaceholderColors.kt` | role-match |
| `ui/components/CircularProgressRing.kt` | component | transform | — | no analog |
| `ui/components/StateOverlay.kt` | component | request-response | `ui/home/components/KidAvatar.kt` (circular badge) | partial |
| `navigation/Routes.kt` | route | request-response | `navigation/Routes.kt` (`ChildCatalog`) | exact |
| `navigation/SimmarkenNavHost.kt` | route | request-response | `navigation/SimmarkenNavHost.kt` | exact |
| `navigation/ChildCatalogViewModelFactory.kt` | config | request-response | `navigation/ChildCatalogViewModelFactory.kt` | exact |
| `navigation/BadgeDetailViewModelFactory.kt` | config | request-response | `navigation/KidFormViewModelFactory.kt` | exact |
| `data/local/dao/CatalogDao.kt` | middleware (DAO) | pub-sub | `data/local/dao/CatalogDao.kt` (existing queries) | exact |
| `domain/repository/CatalogRepository.kt` | service | pub-sub | `domain/repository/CatalogRepositoryImpl.kt` | exact |
| `gradle/libs.versions.toml` | config | — | `gradle/libs.versions.toml` | exact |
| `app/build.gradle.kts` | config | — | `app/build.gradle.kts` | exact |
| `test/.../BadgeStateCalculatorTest.kt` | test | transform | `test/.../KidNameValidationTest.kt` | exact |

**Delete after wiring:** `ui/child/ChildCatalogPlaceholderScreen.kt` — replaced by `ChildCatalogScreen.kt`.

## Pattern Assignments

### `domain/BadgeStateCalculator.kt` (utility, transform)

**Analog:** `app/src/main/java/se/simmarken/domain/validation/KidNameValidation.kt`

**Imports pattern** (lines 1-3):
```kotlin
package se.simmarken.domain

object KidNameValidation {
```

**Core pattern** — pure `object` with no Android/framework deps; deterministic logic (lines 3-11):
```kotlin
object KidNameValidation {
    fun validateName(raw: String): String? {
        val trimmed = raw.trim()
        return when {
            trimmed.isEmpty() -> "Ange ett namn"
            trimmed.length > 30 -> "Namnet får vara högst 30 tecken"
            else -> null
        }
    }
}
```

**Apply to BadgeStateCalculator:** Same package style (`se.simmarken.domain`), `object` singleton, `when`/early-return precedence per CONTEXT D-30. Add companion `progressFraction()` for ring fill.

---

### `domain/model/BadgeVisualState.kt` (model, transform)

**Analog:** `app/src/main/java/se/simmarken/ui/home/HomeViewModel.kt` (`KidSheetState`)

**Enum/sealed pattern** (lines 14-18):
```kotlin
sealed interface KidSheetState {
    data object Hidden : KidSheetState
    data class Add(val sessionId: Long) : KidSheetState
    data class Edit(val kidId: Long, val sessionId: Long) : KidSheetState
}
```

**Apply:** Use simple `enum class BadgeVisualState { LOCKED, IN_PROGRESS, ACHIEVED_TO_BUY, GOTTEN }` in `domain/model/` — enums are appropriate here (fixed 4-tier set, no payload).

---

### `domain/model/CatalogUiModels.kt` (model, transform)

**Analog:** `app/src/main/java/se/simmarken/ui/home/HomeViewModel.kt` (`HomeUiState`)

**UiState data class pattern** (lines 20-24):
```kotlin
data class HomeUiState(
    val kids: List<KidEntity> = emptyList(),
    val sheetState: KidSheetState = KidSheetState.Hidden,
    val deleteTarget: KidEntity? = null,
)
```

**Apply:** Define `CategorySection`, `BadgeCellUiModel` with defaults; include pre-computed `BadgeVisualState`, `progressFraction`, `achievedCount`, `totalRequirements`, `categoryCode` (for `BadgePlaceholderColors.forCategoryCode`).

---

### `ui/child/ChildCatalogViewModel.kt` (hook, pub-sub)

**Analog:** `app/src/main/java/se/simmarken/ui/home/HomeViewModel.kt`

**Imports pattern** (lines 1-12):
```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import se.simmarken.domain.repository.KidRepository
```

**Reactive combine + stateIn** (lines 33-43):
```kotlin
val uiState = combine(
    kidRepository.observeAll(),
    sheetState,
    deleteTarget,
) { kids, sheet, delete ->
    HomeUiState(kids = kids, sheetState = sheet, deleteTarget = delete)
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000),
    initialValue = HomeUiState(),
)
```

**Existing child-name load** — keep from current `ChildCatalogViewModel` (lines 14-20):
```kotlin
val childName = kidRepository.observeById(kidId)
    .map { kid -> kid?.name ?: "" }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = "",
    )
```

**Extend with:** `combine(catalogRepository.observeCatalogs(), progressRepository.observeRequirementProgress(kidId), progressRepository.observeBadgeProgress(kidId), catalogRepository.observeAllRequirements(), selectedCatalogId)` + `flatMapLatest` for category sections per RESEARCH.md Pattern 2. Constructor gains `catalogRepository`, `progressRepository`, `kidId`.

**Tab state (D-04):** Do NOT persist in ViewModel across navigation — use `remember { mutableIntStateOf(0) }` in screen composable and pass `selectedTabIndex` / `onTabSelected` to ViewModel, OR reset tab in screen `LaunchedEffect(Unit)`.

---

### `ui/child/ChildCatalogScreen.kt` (component, request-response)

**Analog:** `app/src/main/java/se/simmarken/ui/child/ChildCatalogPlaceholderScreen.kt` + `ui/home/HomeScreen.kt`

**Scaffold + top bar + collectAsState** (ChildCatalogPlaceholderScreen lines 21-42):
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildCatalogPlaceholderScreen(
    viewModel: ChildCatalogViewModel,
    onBack: () -> Unit,
) {
    val childName by viewModel.childName.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(childName) },
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
```

**Navigation callback** (HomeScreen lines 81-83):
```kotlin
onCardClick = {
    navController.navigate(ChildCatalog(kidId = kid.id))
},
```

**Apply:** Replace placeholder `Box` with `CatalogTabRow` + `BadgeGrid` or `CatalogEmptyState`. Add `onBadgeClick: (badgeId: Long) -> Unit` parameter; caller navigates `BadgeDetail(kidId, badgeId)`.

---

### `ui/child/components/CatalogTabRow.kt` (component, event-driven)

**Analog:** `ChildCatalogPlaceholderScreen.kt` (Material 3 scaffold structure)

**Material 3 usage** (lines 8-14):
```kotlin
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
```

**Apply:** Use `PrimaryTabRow` / `Tab` from Material 3 below top bar (not in codebase yet — RESEARCH.md Pattern). Labels hardcoded Swedish: `"Simidrott"`, `"SLS"`. Map tab index → catalog code `"simidrott"` / `"sls"` from `observeCatalogs()` by `sortOrder` or `code`.

---

### `ui/child/components/CategoryStickyHeader.kt` (component, request-response)

**Analog:** `HomeScreen.kt` top bar title typography

**Typography pattern** (HomeScreen line 48):
```kotlin
title = { Text("Simmärken") },
```

**Apply:** `Text(section.category.nameSv, style = MaterialTheme.typography.titleMedium)` on `MaterialTheme.colorScheme.surface` background for sticky header readability (D-19).

---

### `ui/child/components/BadgeGrid.kt` (component, request-response)

**Analog:** `app/src/main/java/se/simmarken/ui/home/HomeScreen.kt`

**LazyColumn with keyed items** (lines 69-92):
```kotlin
LazyColumn(
    modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp),
    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
) {
    items(uiState.kids, key = { it.id }) { kid ->
        ChildCard(
            name = kid.name,
            ...
        )
    }
}
```

**Apply (RESEARCH Pattern 3):** Single `LazyColumn`; per category `stickyHeader(key = section.category.id)` + `items(rows, key = { row.first().id })` with 3-column `Row`. Use `Arrangement.spacedBy(12.dp)` (D-15). Pad incomplete rows with `Spacer(Modifier.weight(1f))`.

---

### `ui/child/components/BadgeGridItem.kt` (component, event-driven)

**Analog:** `app/src/main/java/se/simmarken/ui/home/components/ChildCard.kt`

**Clickable + text truncation** (lines 49-67):
```kotlin
Row(
    modifier = Modifier
        .weight(1f)
        .clickable(onClick = onCardClick),
    verticalAlignment = Alignment.CenterVertically,
) {
    ...
    Text(
        text = name,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .weight(1f)
            .padding(start = 16.dp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
```

**Apply:** `Column` with square `aspectRatio(1f)` cell, centered `BadgePinVisual`, name below with `maxLines = 1`, `TextOverflow.Ellipsis` (D-14). `Modifier.clickable { onClick(badge.id) }`.

---

### `ui/child/components/CatalogEmptyState.kt` (component, request-response)

**Analog:** `app/src/main/java/se/simmarken/ui/home/components/EmptyState.kt`

**Full pattern** (lines 21-53):
```kotlin
@Composable
fun EmptyState(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 24.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.ChildCare,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Lägg till ditt första barn",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Tryck på + för att skapa en profil och börja följa simmärken.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 280.dp),
            )
        }
    }
}
```

**Apply:** Copy structure; Swedish copy for empty catalog tab (e.g. "Inga märken i den här katalogen").

---

### `ui/badge/BadgeDetailPlaceholderScreen.kt` (component, request-response)

**Analog:** `app/src/main/java/se/simmarken/ui/child/ChildCatalogPlaceholderScreen.kt`

**Scaffold + centered content** (lines 29-54):
```kotlin
Scaffold(
    topBar = {
        CenterAlignedTopAppBar(
            title = { Text(childName) },
            navigationIcon = { ... },
        )
    },
) { innerPadding ->
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Simmärken kommer snart",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
```

**Apply:** Title = badge `nameSv`; large `BadgePinVisual` (detail size ~320dp); stub `Text("Checklista kommer snart")` (D-25). Reuse same visual state inputs as grid (D-28).

---

### `ui/badge/BadgeDetailViewModel.kt` (hook, pub-sub)

**Analog:** `app/src/main/java/se/simmarken/ui/child/ChildCatalogViewModel.kt` + `KidFormViewModel.kt`

**Simple observe + stateIn** (ChildCatalogViewModel lines 14-20):
```kotlin
val childName = kidRepository.observeById(kidId)
    .map { kid -> kid?.name ?: "" }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = "",
    )
```

**Entity load with null guard** (KidFormViewModel lines 45-57):
```kotlin
viewModelScope.launch {
    kidRepository.observeById(kidId).collect { kid ->
        if (kid != null && !initialLoadDone) {
            existingKid = kid
            ...
        }
    }
}
```

**Apply:** `combine` badge by id (new DAO `observeBadgeById` or one-shot from catalog), requirement progress, badge progress → `BadgeDetailUiState`. If badge/kid missing → error/empty state (RESEARCH Security V5).

---

### `ui/components/BadgePinVisual.kt` (component, file-I/O)

**Analog:** `app/src/main/java/se/simmarken/ui/home/components/KidAvatar.kt` + `ui/badge/BadgePlaceholderColors.kt`

**Box + clip visual** (KidAvatar lines 31-45):
```kotlin
Box(
    modifier = modifier
        .size(48.dp)
        .clip(CircleShape)
        .background(backgroundColor),
    contentAlignment = Alignment.Center,
) {
    Text(...)
}
```

**Tier-color fallback** (BadgePlaceholderColors lines 28-30):
```kotlin
fun forCategoryCode(categoryCode: String): Color = categoryColors[categoryCode] ?: default

fun forBadgeCode(badgeCode: String): Color = forCategoryCode(badgeCode)
```

**Apply:** When `imageAssetPath != null`, Coil `AsyncImage` with `file:///android_asset/${imageAssetPath}`. When null, `Box` with `BadgePlaceholderColors.forCategoryCode(categoryCode)` (NOT `forBadgeCode`). Grayscale via `ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })` for LOCKED and IN_PROGRESS (D-11). Wrap with `CircularProgressRing` when IN_PROGRESS; `StateOverlay` when ACHIEVED_TO_BUY or GOTTEN.

---

### `ui/components/CircularProgressRing.kt` (component, transform)

**Analog:** None in codebase

**Reference:** RESEARCH.md Pattern + UI-SPEC — `Canvas.drawArc` with `StrokeCap.Round`, 3dp stroke, `MaterialTheme.colorScheme.primary` for progress (D-10). No third-party chart lib.

---

### `ui/components/StateOverlay.kt` (component, request-response)

**Analog:** `KidAvatar.kt` circular badge styling

**Circular shape pattern** (KidAvatar lines 34-35):
```kotlin
.clip(CircleShape)
.background(backgroundColor),
```

**Apply:** Small `Box` bottom-end aligned on pin; `CircleShape` filled `MaterialTheme.colorScheme.primary` (or `surfaceVariant`); white `Icon` (cart/check). D-24 filled circle badge.

---

### `navigation/Routes.kt` (route, request-response)

**Analog:** `app/src/main/java/se/simmarken/navigation/Routes.kt`

**Type-safe serializable routes** (lines 1-9):
```kotlin
package se.simmarken.navigation

import kotlinx.serialization.Serializable

@Serializable
object Home

@Serializable
data class ChildCatalog(val kidId: Long)
```

**Apply:** Add `@Serializable data class BadgeDetail(val kidId: Long, val badgeId: Long)`.

---

### `navigation/SimmarkenNavHost.kt` (route, request-response)

**Analog:** existing `composable<ChildCatalog>` block (lines 28-40)

**NavHost + factory + screen** (lines 28-40):
```kotlin
composable<ChildCatalog> { backStackEntry ->
    val route = backStackEntry.toRoute<ChildCatalog>()
    val catalogViewModel: ChildCatalogViewModel = viewModel(
        factory = ChildCatalogViewModelFactory(
            application.container.kidRepository,
            route.kidId,
        ),
    )
    ChildCatalogPlaceholderScreen(
        viewModel = catalogViewModel,
        onBack = { navController.popBackStack() },
    )
}
```

**Apply:** Swap `ChildCatalogPlaceholderScreen` → `ChildCatalogScreen`; extend factory with `catalogRepository`, `progressRepository`. Add:
```kotlin
composable<BadgeDetail> { entry ->
    val route = entry.toRoute<BadgeDetail>()
    // BadgeDetailPlaceholderScreen + BadgeDetailViewModelFactory
}
```

---

### `navigation/ChildCatalogViewModelFactory.kt` (config, request-response)

**Analog:** `app/src/main/java/se/simmarken/navigation/ChildCatalogViewModelFactory.kt`

**Factory pattern** (lines 8-18):
```kotlin
class ChildCatalogViewModelFactory(
    private val kidRepository: KidRepository,
    private val kidId: Long,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChildCatalogViewModel::class.java)) {
            return ChildCatalogViewModel(kidRepository, kidId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
```

**Apply:** Add `catalogRepository: CatalogRepository`, `progressRepository: ProgressRepository` constructor params; pass to `ChildCatalogViewModel`.

---

### `navigation/BadgeDetailViewModelFactory.kt` (config, request-response)

**Analog:** `app/src/main/java/se/simmarken/navigation/KidFormViewModelFactory.kt`

**Factory with route id** (lines 8-18):
```kotlin
class KidFormViewModelFactory(
    private val kidRepository: KidRepository,
    private val kidId: Long?,
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

**Apply:** Non-null `kidId`, `badgeId`; inject `catalogRepository`, `progressRepository`.

---

### `data/local/dao/CatalogDao.kt` + `CatalogRepository` (middleware/service, pub-sub)

**Analog:** existing `CatalogDao.kt` observe queries (lines 14-24)

**DAO Flow query pattern** (lines 14-24):
```kotlin
@Query("SELECT * FROM catalogs ORDER BY sortOrder, nameSv")
fun observeCatalogs(): Flow<List<CatalogEntity>>

@Query("SELECT * FROM categories WHERE catalogId = :catalogId ORDER BY sortOrder, nameSv")
fun observeCategories(catalogId: Long): Flow<List<CategoryEntity>>

@Query("SELECT * FROM badges WHERE categoryId = :categoryId ORDER BY sortOrder, nameSv")
fun observeBadges(categoryId: Long): Flow<List<BadgeEntity>>

@Query("SELECT * FROM requirements WHERE badgeId = :badgeId ORDER BY sortOrder, code")
fun observeRequirements(badgeId: Long): Flow<List<RequirementEntity>>
```

**Repository passthrough** (CatalogRepositoryImpl lines 12-15):
```kotlin
override fun observeCatalogs() = catalogDao.observeCatalogs()
override fun observeCategories(catalogId: Long) = catalogDao.observeCategories(catalogId)
override fun observeBadges(categoryId: Long) = catalogDao.observeBadges(categoryId)
override fun observeRequirements(badgeId: Long) = catalogDao.observeRequirements(badgeId)
```

**Add:**
```kotlin
@Query("SELECT * FROM requirements")
fun observeAllRequirements(): Flow<List<RequirementEntity>>
```
Plus optional `observeBadgeById(badgeId: Long)` for detail screen. Mirror in `CatalogRepository` interface + `CatalogRepositoryImpl`.

---

### `gradle/libs.versions.toml` + `app/build.gradle.kts` (config)

**Analog:** existing dependency blocks

**Version catalog** (libs.versions.toml lines 1-16, 58-74):
```toml
[versions]
coroutines = "1.9.0"
...
[libraries]
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
```

**build.gradle.kts dependencies** (lines 58-74):
```kotlin
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    ...
    implementation(libs.kotlinx.serialization.json)
}
```

**Add (RESEARCH.md):**
```toml
coil = "3.5.0"
coil-compose = { group = "io.coil-kt.coil3", name = "coil-compose", version.ref = "coil" }
```
```kotlin
implementation(libs.coil.compose)
```

---

### `test/.../BadgeStateCalculatorTest.kt` (test, transform)

**Analog:** `app/src/test/java/se/simmarken/domain/validation/KidNameValidationTest.kt`

**JUnit 4 structure** (lines 1-27):
```kotlin
package se.simmarken.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KidNameValidationTest {
    @Test
    fun emptyStringReturnsError() {
        assertEquals("Ange ett namn", KidNameValidation.validateName(""))
    }
    ...
}
```

**Apply:** Package `se.simmarken.domain`; test matrix from RESEARCH.md (gotten overrides, all achieved → ACHIEVED_TO_BUY, one req → IN_PROGRESS, zero → LOCKED, zero-requirement badge edge case, `progressFraction`).

---

## Shared Patterns

### MVVM + StateFlow
**Source:** `HomeViewModel.kt`, `ChildCatalogViewModel.kt`
**Apply to:** `ChildCatalogViewModel`, `BadgeDetailViewModel`
```kotlin
val uiState = combine(...).stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000),
    initialValue = ChildCatalogUiState(),
)
```

### Composable state collection
**Source:** `ChildCatalogPlaceholderScreen.kt` line 27
**Apply to:** All new screens
```kotlin
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
```

### Manual ViewModel factories in `navigation/`
**Source:** `ChildCatalogViewModelFactory.kt`, `HomeViewModelFactory.kt`
**Apply to:** All new ViewModels; wire deps from `SimmarkenApplication.container`
```kotlin
val application = LocalContext.current.applicationContext as SimmarkenApplication
// application.container.catalogRepository, .progressRepository, .kidRepository
```

### Repository → DAO pass-through
**Source:** `CatalogRepositoryImpl.kt`, `ProgressRepositoryImpl.kt`
**Apply to:** New `observeAllRequirements()` only — no business logic in repository impl

### Progress default: absent row = not achieved
**Source:** `RequirementProgressEntity.isAchieved`; RESEARCH Pitfall 2
**Apply to:** ViewModel indexing when building `Map<requirementId, Boolean>`
```kotlin
// Default false when requirementId not in progress list
val achieved = progressByRequirementId[req.id] ?: false
```

### Badge state derivation (single source)
**Source:** CONTEXT D-29, D-30; ARCHITECTURE.md Pattern 2
**Apply to:** `ChildCatalogViewModel`, `BadgeDetailViewModel` — call `BadgeStateCalculator.compute()` only; never duplicate if/else

### Swedish hardcoded strings
**Source:** `ChildCatalogPlaceholderScreen.kt` ("Tillbaka", "Simmärken kommer snart"), `EmptyState.kt`
**Apply to:** Tab labels, empty states, detail stub — Phase 6 i18n deferred

### Theme primary for progress ring
**Source:** `ui/theme/Color.kt` + `Theme.kt`
```kotlin
val BluePrimary = Color(0xFF2196F3)
// MaterialTheme.colorScheme.primary in composables
```

### AppContainer repositories
**Source:** `di/AppContainer.kt` lines 32-37
```kotlin
val catalogRepository: CatalogRepository = CatalogRepositoryImpl(database.catalogDao())
val progressRepository: ProgressRepository = ProgressRepositoryImpl(
    requirementProgressDao = database.requirementProgressDao(),
    badgeProgressDao = database.badgeProgressDao(),
)
```

## No Analog Found

| File | Role | Data Flow | Reason |
|------|------|-----------|--------|
| `ui/components/CircularProgressRing.kt` | component | transform | No `Canvas`/`drawArc` usage in codebase; use RESEARCH.md + Android Compose graphics docs |
| `ui/child/components/CatalogTabRow.kt` | component | event-driven | No `TabRow`/`PrimaryTabRow` in codebase yet; follow Material 3 API per RESEARCH.md |
| `ui/child/components/CategoryStickyHeader.kt` | component | request-response | No `stickyHeader` usage in codebase; follow RESEARCH.md LazyColumn pattern |
| Coil `AsyncImage` usage | component | file-I/O | Coil not in `build.gradle.kts` yet; RESEARCH.md Pattern 4 is authoritative |

## Metadata

**Analog search scope:** `app/src/main/java/se/simmarken/` (ui, domain, navigation, data, di), `app/src/test/`, `gradle/`
**Files scanned:** ~45 Kotlin sources + gradle configs
**Pattern extraction date:** 2026-07-23
