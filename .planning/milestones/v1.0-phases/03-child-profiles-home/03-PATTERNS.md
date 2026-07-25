# Phase 3: Child Profiles & Home - Pattern Map

**Mapped:** 2026-07-23
**Files analyzed:** 24 new/modified files
**Analogs found:** 16 / 24

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|-------------------|------|-----------|----------------|---------------|
| `data/local/dao/KidDao.kt` | dao | CRUD + streaming | `data/local/dao/KidDao.kt` (extend) | exact |
| `domain/repository/KidRepository.kt` | repository | CRUD + streaming | `domain/repository/KidRepository.kt` (extend) | exact |
| `domain/repository/KidRepositoryImpl.kt` | repository | CRUD + streaming | `domain/repository/KidRepositoryImpl.kt` (extend) | exact |
| `domain/util/KidAvatarInitials.kt` | utility | transform | `ui/badge/BadgePlaceholderColors.kt` | role-match |
| `ui/theme/KidAvatarColors.kt` | utility | transform | `ui/badge/BadgePlaceholderColors.kt` | exact |
| `navigation/Routes.kt` | route | request-response | `navigation/Routes.kt` (extend) | exact |
| `navigation/SimmarkenNavHost.kt` | route | request-response | `navigation/SimmarkenNavHost.kt` (extend) | exact |
| `navigation/KidFormViewModelFactory.kt` | config | request-response | `navigation/HomeViewModelFactory.kt` | exact |
| `ui/home/HomeScreen.kt` | component | CRUD + event-driven | `ui/home/HomeScreen.kt` (replace) | exact |
| `ui/home/HomeViewModel.kt` | viewmodel | CRUD + streaming | `ui/home/HomeViewModel.kt` (extend) | exact |
| `ui/home/KidFormBottomSheet.kt` | component | transform | — | no analog |
| `ui/home/KidFormViewModel.kt` | viewmodel | CRUD | `ui/home/HomeViewModel.kt` | role-match |
| `ui/home/components/ChildCard.kt` | component | request-response | — | no analog |
| `ui/home/components/KidAvatar.kt` | component | transform | `ui/badge/BadgePlaceholderColors.kt` | partial |
| `ui/home/components/EmptyState.kt` | component | static | `ui/home/HomePlaceholderScreen.kt` | role-match |
| `ui/home/components/ColorSwatchGrid.kt` | component | transform | — | no analog |
| `ui/home/components/DeleteKidDialog.kt` | component | CRUD | — | no analog |
| `ui/child/ChildCatalogPlaceholderScreen.kt` | component | request-response | `ui/home/HomePlaceholderScreen.kt` | role-match |
| `ui/child/ChildCatalogViewModel.kt` | viewmodel | streaming | `ui/home/HomeViewModel.kt` | role-match |
| `test/.../KidAvatarInitialsTest.kt` | test | transform | `test/.../CatalogVersionTest.kt` | role-match |
| `test/.../KidAvatarColorsTest.kt` | test | transform | `test/.../CatalogVersionTest.kt` | role-match |
| `test/.../KidNameValidationTest.kt` | test | transform | `test/.../CatalogVersionTest.kt` | role-match |
| `androidTest/.../KidDaoTest.kt` | test | CRUD | `androidTest/.../DaoInstrumentedTest.kt` | exact |
| `app/build.gradle.kts` | config | batch | `app/build.gradle.kts` (extend) | exact |

## Pattern Assignments

### `data/local/dao/KidDao.kt` (dao, CRUD + streaming)

**Analog:** `app/src/main/java/se/simmarken/data/local/dao/KidDao.kt` + `CatalogDao.kt` observe patterns

**Imports pattern** (lines 1-8):
```kotlin
package se.simmarken.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import se.simmarken.data.local.entity.KidEntity
```

**Core observe + upsert pattern** (lines 10-17):
```kotlin
@Dao
interface KidDao {
    @Query("SELECT * FROM kids ORDER BY sortOrder, name")
    fun observeAll(): Flow<List<KidEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(kid: KidEntity): Long
}
```

**Extend with — copy `CatalogDao` single-entity query style** (lines 17-18, 38-39):
```kotlin
@Query("SELECT * FROM categories WHERE id = :categoryId LIMIT 1")
suspend fun findCategoryById(categoryId: Long): CategoryEntity?
```

**Phase 3 additions (planner target):**
```kotlin
@Query("SELECT * FROM kids ORDER BY sortOrder ASC, createdAtEpochMillis ASC")
fun observeAll(): Flow<List<KidEntity>>

@Query("SELECT * FROM kids WHERE id = :kidId")
fun observeById(kidId: Long): Flow<KidEntity?>

@Query("DELETE FROM kids WHERE id = :kidId")
suspend fun deleteById(kidId: Long)
```

**Error handling:** Room throws on FK violations; no DAO-level try/catch — let repository/ViewModel surface errors.

---

### `domain/repository/KidRepository.kt` + `KidRepositoryImpl.kt` (repository, CRUD + streaming)

**Analog:** `domain/repository/KidRepository.kt` + `KidRepositoryImpl.kt` + `CatalogRepository.kt`

**Interface pattern** (lines 1-9):
```kotlin
package se.simmarken.domain.repository

import kotlinx.coroutines.flow.Flow
import se.simmarken.data.local.entity.KidEntity

interface KidRepository {
    fun observeAll(): Flow<List<KidEntity>>
    suspend fun upsert(kid: KidEntity): Long
}
```

**Impl delegation pattern** (lines 1-11):
```kotlin
class KidRepositoryImpl(
    private val kidDao: KidDao,
) : KidRepository {
    override fun observeAll() = kidDao.observeAll()
    override suspend fun upsert(kid: KidEntity) = kidDao.upsert(kid)
}
```

**Extend interface mirroring `CatalogRepository` observe methods:**
```kotlin
fun observeById(kidId: Long): Flow<KidEntity?>
suspend fun delete(kidId: Long)
```

**Impl — thin pass-through only; no manual progress cleanup** (FK CASCADE on `RequirementProgressEntity` / `BadgeProgressEntity`).

---

### `domain/util/KidAvatarInitials.kt` (utility, transform)

**Analog:** `ui/badge/BadgePlaceholderColors.kt` (pure Kotlin object, no Android deps)

**Object + pure function pattern** (lines 5-30):
```kotlin
object BadgePlaceholderColors {
    val default: Color = Color(0xFF9E9E9E)

    private val categoryColors: Map<String, Color> = mapOf(
        "vattenvana" to Color(0xFF4CAF50),
        // ...
    )

    fun forCategoryCode(categoryCode: String): Color = categoryColors[categoryCode] ?: default
}
```

**Apply:** Use `object KidAvatarInitials` with `fun fromName(name: String): String` — no Compose imports; JUnit-testable like `CatalogVersionTest`.

---

### `ui/theme/KidAvatarColors.kt` (utility, transform)

**Analog:** `ui/badge/BadgePlaceholderColors.kt`

**Palette object pattern** (lines 5-30):
```kotlin
object BadgePlaceholderColors {
    val default: Color = Color(0xFF9E9E9E)

    private val categoryColors: Map<String, Color> = mapOf(
        "vattenvana" to Color(0xFF4CAF50),
        // ...
    )

    fun forCategoryCode(categoryCode: String): Color = categoryColors[categoryCode] ?: default
}
```

**Phase 3 variant — store ARGB `Int` (matches `KidEntity.avatarColorArgb`), not `Color`:**
```kotlin
object KidAvatarColors {
    val palette: List<Int> = listOf(
        0xFFE53935.toInt(), 0xFFFB8C00.toInt(), /* ... 10 swatches per UI-SPEC */
    )

    fun defaultForName(name: String): Int {
        val index = kotlin.math.abs(name.trim().hashCode()) % palette.size
        return palette[index]
    }
}
```

**Package:** Place in `ui/theme/` per UI-SPEC (alongside `Color.kt`).

---

### `navigation/Routes.kt` (route, request-response)

**Analog:** `navigation/Routes.kt`

**Type-safe route pattern** (lines 1-6):
```kotlin
package se.simmarken.navigation

import kotlinx.serialization.Serializable

@Serializable
object Home
```

**Add:**
```kotlin
@Serializable
data class ChildCatalog(val kidId: Long)
```

---

### `navigation/SimmarkenNavHost.kt` (route, request-response)

**Analog:** `navigation/SimmarkenNavHost.kt`

**Imports pattern** (lines 1-11):
```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import se.simmarken.SimmarkenApplication
import se.simmarken.ui.home.HomeScreen
import se.simmarken.ui.home.HomeViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
```

**NavHost + factory DI pattern** (lines 13-25):
```kotlin
@Composable
fun SimmarkenNavHost() {
    val navController = rememberNavController()
    val application = LocalContext.current.applicationContext as SimmarkenApplication

    NavHost(navController = navController, startDestination = Home) {
        composable<Home> {
            val viewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(application.container.kidRepository),
            )
            HomeScreen(viewModel = viewModel)
        }
    }
}
```

**Extend — pass `navController` to `HomeScreen`; add catalog route:**
```kotlin
composable<ChildCatalog> { backStackEntry ->
    val route = backStackEntry.toRoute<ChildCatalog>()
    ChildCatalogPlaceholderScreen(
        kidId = route.kidId,
        kidRepository = application.container.kidRepository,
        onBack = { navController.popBackStack() },
    )
}
```

**Import:** `androidx.navigation.toRoute`

---

### `navigation/KidFormViewModelFactory.kt` (config, request-response)

**Analog:** `navigation/HomeViewModelFactory.kt`

**Factory pattern** (lines 1-18):
```kotlin
class HomeViewModelFactory(
    private val kidRepository: KidRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(kidRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
```

**Variant — accept optional `kidId: Long?` (null = add, non-null = edit):**
```kotlin
class KidFormViewModelFactory(
    private val kidRepository: KidRepository,
    private val kidId: Long?,
) : ViewModelProvider.Factory { /* KidFormViewModel(kidRepository, kidId) */ }
```

---

### `ui/home/HomeScreen.kt` (component, CRUD + event-driven)

**Analog:** `ui/home/HomeScreen.kt` (replace placeholder)

**Composable + lifecycle collect pattern** (lines 1-19):
```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
```

**Scaffold shell** (lines 21-38):
```kotlin
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "Simmärken")
            // ...
        }
    }
}
```

**Replace with:** `Scaffold` + `CenterAlignedTopAppBar` + `FloatingActionButton` + `LazyColumn` of `ChildCard`; conditional `ModalBottomSheet` / `DeleteKidDialog` overlays driven by `HomeViewModel` sheet/dialog state. Keep `collectAsStateWithLifecycle()` — do not hold kid list in `remember`.

**Navigation:** Add `navController: NavHostController` param; card tap calls `navController.navigate(ChildCatalog(kidId = kid.id))`.

---

### `ui/home/HomeViewModel.kt` (viewmodel, CRUD + streaming)

**Analog:** `ui/home/HomeViewModel.kt`

**UiState + stateIn pattern** (lines 13-26):
```kotlin
data class HomeUiState(
    val kidCount: Int = 0,
)

class HomeViewModel(
    private val kidRepository: KidRepository,
) : ViewModel() {
    val uiState = kidRepository.observeAll()
        .map { kids -> HomeUiState(kidCount = kids.size) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(),
        )
```

**IO write pattern** (lines 28-39):
```kotlin
    fun addTestKid() {
        viewModelScope.launch(Dispatchers.IO) {
            kidRepository.upsert(
                KidEntity(
                    name = "Test Kid",
                    avatarColorArgb = 0xFF2196F3.toInt(),
                    createdAtEpochMillis = System.currentTimeMillis(),
                    sortOrder = 0,
                ),
            )
        }
    }
```

**Extend `HomeUiState`:**
```kotlin
data class HomeUiState(
    val kids: List<KidEntity> = emptyList(),
    val sheetState: KidSheetState = KidSheetState.Hidden,
    val deleteTarget: KidEntity? = null,
)
```

**Map full list:** `.map { kids -> HomeUiState(kids = kids) }` — ordering comes from DAO, not Composable sort.

**Delete on IO:**
```kotlin
fun confirmDelete(kid: KidEntity) {
    viewModelScope.launch(Dispatchers.IO) {
        kidRepository.delete(kid.id)
    }
}
```

**Remove:** `addTestKid()` debug method.

---

### `ui/home/KidFormViewModel.kt` (viewmodel, CRUD)

**Analog:** `ui/home/HomeViewModel.kt` (write path)

**Reuse IO launch + upsert pattern** (lines 28-38):
```kotlin
viewModelScope.launch(Dispatchers.IO) {
    kidRepository.upsert(
        KidEntity(
            name = trimmedName,
            avatarColorArgb = selectedColorArgb,
            createdAtEpochMillis = existing?.createdAtEpochMillis ?: System.currentTimeMillis(),
            sortOrder = existing?.sortOrder ?: (currentMaxSortOrder + 1),
            id = existing?.id ?: 0,
        ),
    )
}
```

**Validation (pure, testable):**
```kotlin
fun validateName(raw: String): String? {
    val trimmed = raw.trim()
    return when {
        trimmed.isEmpty() -> "Ange ett namn"
        trimmed.length > 30 -> "Namnet får vara högst 30 tecken"
        else -> null
    }
}
```

**Edit mode:** Load existing kid via `kidRepository.observeById(kidId)` — preserve `id`, `createdAtEpochMillis`, `sortOrder` on save.

---

### `ui/home/KidFormBottomSheet.kt` (component, transform)

**Analog:** None in codebase — use Material 3 overlay pattern from `03-RESEARCH.md` Pattern 2

**Compose from `HomeScreen` lifecycle pattern:**
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KidFormBottomSheet(/* sheetState, onDismiss, viewModel */) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        // OutlinedTextField, ColorSwatchGrid, actions
        // Modifier.imePadding() on content Column
    }
}
```

**Dismiss-after-save (RESEARCH Pattern 2):**
```kotlin
scope.launch { sheetState.hide() }.invokeOnCompletion {
    if (!sheetState.isVisible) showSheet = false
}
```

**ViewModel wiring:** `viewModel(factory = KidFormViewModelFactory(...))` inside sheet composable or passed from `HomeScreen`.

---

### `ui/home/components/ChildCard.kt` (component, request-response)

**Analog:** None — no card/list components exist yet

**Structural guidance from RESEARCH Pattern 4 + `HomeScreen` Material3 imports:**
```kotlin
ElevatedCard(modifier = Modifier.fillMaxWidth()) {
    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onCardClick),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            KidAvatar(name = name, avatarColorArgb = avatarColorArgb)
            Text(text = name, style = MaterialTheme.typography.bodyLarge, /* ellipsis */)
        }
        IconButton(onClick = onOverflowClick) {
            Icon(Icons.Default.MoreVert, contentDescription = "Alternativ för $name")
        }
    }
}
```

**DropdownMenu** anchored to overflow — separate click target from card body (D-08).

---

### `ui/home/components/KidAvatar.kt` (component, transform)

**Analog:** `ui/badge/BadgePlaceholderColors.kt` (color lookup) + `ui/theme/Color.kt`

**Circle + background pattern:**
```kotlin
Box(
    modifier = Modifier
        .size(48.dp)
        .background(Color(avatarColorArgb), CircleShape),
    contentAlignment = Alignment.Center,
) {
    Text(
        text = KidAvatarInitials.fromName(name),
        style = MaterialTheme.typography.titleLarge,
        color = Color.White, // or Color.Black for yellow swatch index 2
        maxLines = 1,
    )
}
```

---

### `ui/home/components/EmptyState.kt` (component, static)

**Analog:** `ui/home/HomePlaceholderScreen.kt`

**Centered placeholder pattern** (lines 10-17):
```kotlin
@Composable
fun HomePlaceholderScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text("Simmärken")
    }
}
```

**Extend with:** icon (`Icons.Outlined.ChildCare`), `titleLarge` heading, `bodyLarge` body per UI-SPEC copywriting. FAB stays in parent `Scaffold` — no duplicate CTA.

---

### `ui/child/ChildCatalogPlaceholderScreen.kt` (component, request-response)

**Analog:** `ui/home/HomePlaceholderScreen.kt` + `HomeScreen.kt` Scaffold

**Placeholder center content** (HomePlaceholderScreen lines 10-17) + **Scaffold shell** (HomeScreen lines 21-22).

**Add:** `TopAppBar` with back `IconButton` → `onBack()`; load child name via `ChildCatalogViewModel` observing `kidRepository.observeById(kidId)`.

---

### `ui/child/ChildCatalogViewModel.kt` (viewmodel, streaming) — optional

**Analog:** `ui/home/HomeViewModel.kt`

**stateIn from repository Flow:**
```kotlin
val kidName = kidRepository.observeById(kidId)
    .map { it?.name ?: "" }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")
```

---

### Test files

#### `KidDaoTest.kt` (test, CRUD)

**Analog:** `androidTest/java/se/simmarken/data/local/DaoInstrumentedTest.kt`

**Test harness pattern** (lines 21-29):
```kotlin
@RunWith(AndroidJUnit4::class)
class DaoInstrumentedTest {
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(AppDatabase.DB_NAME)
    }
```

**Kid upsert + observe pattern** (lines 76-105):
```kotlin
        val kidId = db.kidDao().upsert(
            KidEntity(
                name = "Ella",
                avatarColorArgb = 0xFF2196F3.toInt(),
                createdAtEpochMillis = 1L,
                sortOrder = 0,
            ),
        )
        // ...
        assertEquals(1, db.kidDao().observeAll().first().size)
```

**Add tests:** ordering (`sortOrder`, `createdAtEpochMillis`), `observeById`, edit preserves fields, `deleteById` cascades progress rows (reuse progress insert block lines 84-98).

#### Unit tests (`KidAvatarInitialsTest`, `KidAvatarColorsTest`, `KidNameValidationTest`)

**Analog:** `test/java/se/simmarken/data/seed/CatalogVersionTest.kt`

**JUnit 4 structure** (lines 1-11):
```kotlin
import org.junit.Assert.assertEquals
import org.junit.Test

class CatalogVersionTest {
    @Test
    fun shouldMergeReturnsTrueOnFirstRun() {
        assertTrue(CatalogVersion.shouldMerge("2026.03.02", null))
    }
}
```

#### `app/build.gradle.kts` (config)

**Analog:** existing `testImplementation` / `androidTestImplementation` blocks (lines 74-80):
```kotlin
    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.room.testing)
```

**Optional Wave 0:** `testImplementation(libs.kotlinx.coroutines.test)` for ViewModel unit tests.

---

## Shared Patterns

### MVVM + Room Flow as Single Source of Truth
**Source:** `ui/home/HomeViewModel.kt` + `domain/repository/KidRepositoryImpl.kt`  
**Apply to:** `HomeViewModel`, `KidFormViewModel`, `ChildCatalogViewModel`, all screens

```kotlin
val uiState = kidRepository.observeAll()
    .map { kids -> HomeUiState(kids = kids) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
```

Composable observes via `collectAsStateWithLifecycle()` — never authoritative list in `remember`.

### ViewModel IO Writes
**Source:** `ui/home/HomeViewModel.kt` lines 28-38  
**Apply to:** `KidFormViewModel.save()`, `HomeViewModel.confirmDelete()`

```kotlin
viewModelScope.launch(Dispatchers.IO) {
    kidRepository.upsert(kid)
}
```

### Manual ViewModel Factory DI (no Hilt)
**Source:** `navigation/HomeViewModelFactory.kt` + `di/AppContainer.kt`  
**Apply to:** All ViewModels

```kotlin
// AppContainer.kt
val kidRepository: KidRepository = KidRepositoryImpl(database.kidDao())

// SimmarkenNavHost.kt
val application = LocalContext.current.applicationContext as SimmarkenApplication
viewModel(factory = HomeViewModelFactory(application.container.kidRepository))
```

### Type-Safe Navigation
**Source:** `navigation/Routes.kt` + `navigation/SimmarkenNavHost.kt`  
**Apply to:** `ChildCatalog` route, card tap navigation

```kotlin
@Serializable
data class ChildCatalog(val kidId: Long)

composable<ChildCatalog> { /* toRoute<ChildCatalog>() */ }
```

### Theme Wrapper
**Source:** `ui/theme/Theme.kt` + `MainActivity.kt`  
**Apply to:** All Composables (already wrapped)

```kotlin
SimmarkenTheme {
    SimmarkenNavHost()
}
```

Use `MaterialTheme.colorScheme`, `MaterialTheme.typography` — no custom fonts this phase.

### Cascade Delete (no manual progress cleanup)
**Source:** `data/local/entity/RequirementProgressEntity.kt` lines 10-16

```kotlin
ForeignKey(
    entity = KidEntity::class,
    parentColumns = ["id"],
    childColumns = ["kidId"],
    onDelete = ForeignKey.CASCADE,
),
```

**Apply to:** `KidRepository.delete()` — only `kidDao.deleteById(kidId)`.

### KidEntity Shape
**Source:** `data/local/entity/KidEntity.kt`

```kotlin
@Entity(tableName = "kids")
data class KidEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val avatarColorArgb: Int,
    val createdAtEpochMillis: Long,
    val sortOrder: Int = 0,
)
```

---

## No Analog Found

Files with no close match in the codebase (planner should use `03-RESEARCH.md` + `03-UI-SPEC.md`):

| File | Role | Data Flow | Reason |
|------|------|-----------|--------|
| `ui/home/KidFormBottomSheet.kt` | component | transform | No `ModalBottomSheet` usage in project yet |
| `ui/home/components/ChildCard.kt` | component | request-response | No card/list item components; first `ElevatedCard` + `LazyColumn` |
| `ui/home/components/ColorSwatchGrid.kt` | component | transform | No grid/selection UI; use M3 `FlowRow` per UI-SPEC |
| `ui/home/components/DeleteKidDialog.kt` | component | CRUD | No `AlertDialog` usage yet; follow UI-SPEC destructive confirm |

**Fallback references for no-analog UI:**
- `03-RESEARCH.md` Pattern 2 (bottom sheet), Pattern 4 (isolated click targets)
- `03-UI-SPEC.md` Component Inventory sections 2, 6, 7, 8 (spacing, copy, touch targets)

---

## Metadata

**Analog search scope:** `app/src/main/java/se/simmarken/**`, `app/src/test/**`, `app/src/androidTest/**`
**Files scanned:** 42 Kotlin source/test files
**Pattern extraction date:** 2026-07-23
