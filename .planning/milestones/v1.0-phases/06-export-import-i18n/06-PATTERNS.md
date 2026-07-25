# Phase 6: Export/Import & i18n - Pattern Map

**Mapped:** 2026-07-24
**Files analyzed:** 32 new/modified files
**Analogs found:** 26 / 32

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|-------------------|------|-----------|----------------|---------------|
| `data/export/BackupDto.kt` | model | transform | `data/seed/CatalogSeedDto.kt` | exact |
| `data/export/ExportJson.kt` | utility | transform | `data/seed/CatalogSeedParser.kt` | exact |
| `data/export/ExportRepository.kt` | service | CRUD + file-I/O + transform | `data/seed/CatalogSeedLoader.kt` | role-match |
| `data/prefs/LocalePreferencesRepository.kt` | repository | CRUD (persist) | — | no analog |
| `domain/export/BackupValidator.kt` | utility | transform | `domain/validation/KidNameValidation.kt` | role-match |
| `domain/export/MergePlanner.kt` | service | batch + transform | `domain/ProgressWriteLogic.kt` | role-match |
| `ui/settings/SettingsScreen.kt` | component | request-response | `ui/child/ChildCatalogScreen.kt` | exact |
| `ui/settings/SettingsViewModel.kt` | provider | event-driven | `ui/home/HomeViewModel.kt` | exact |
| `ui/settings/SettingsViewModelFactory.kt` | provider | — | `navigation/HomeViewModelFactory.kt` | exact |
| `ui/settings/ExportKidPickerDialog.kt` | component | event-driven | `ui/home/components/DeleteKidDialog.kt` | role-match |
| `ui/settings/ImportConfirmDialog.kt` | component | event-driven | `ui/home/components/DeleteKidDialog.kt` | role-match |
| `navigation/Routes.kt` | route | — | `navigation/Routes.kt` (extend) | exact |
| `navigation/SimmarkenNavHost.kt` | route | request-response | `navigation/SimmarkenNavHost.kt` (extend) | exact |
| `di/AppContainer.kt` | config | — | `di/AppContainer.kt` (extend) | exact |
| `data/local/entity/KidEntity.kt` | model | CRUD | `data/local/entity/KidEntity.kt` | exact |
| `data/local/entity/RequirementProgressEntity.kt` | model | CRUD | `data/local/entity/RequirementProgressEntity.kt` | exact |
| `data/local/entity/BadgeProgressEntity.kt` | model | CRUD | `data/local/entity/BadgeProgressEntity.kt` | exact |
| `data/local/AppDatabase.kt` + migration | config | CRUD | `data/local/AppDatabase.kt` | partial |
| `data/local/dao/KidDao.kt` | middleware | CRUD | `data/local/dao/KidDao.kt` | exact |
| `domain/repository/KidRepository.kt` + Impl | service | CRUD | `domain/repository/KidRepository.kt` | exact |
| `domain/repository/ProgressRepository.kt` + Impl | service | CRUD + batch | `domain/repository/ProgressRepository.kt` | exact |
| `ui/home/HomeScreen.kt` | component | request-response | `ui/child/ChildCatalogScreen.kt` (top bar actions) | role-match |
| `MainActivity.kt` | controller | request-response | `MainActivity.kt` | partial |
| `SimmarkenApplication.kt` | provider | — | `SimmarkenApplication.kt` | exact |
| `res/values/strings.xml` + `values-en/strings.xml` | config | transform | `res/values/strings.xml` | partial |
| `res/values/themes.xml` | config | — | `res/values/themes.xml` | exact |
| `res/xml/file_paths.xml` | config | file-I/O | — | no analog |
| `AndroidManifest.xml` | config | event-driven | `AndroidManifest.xml` | partial |
| `app/build.gradle.kts` + `gradle/libs.versions.toml` | config | — | `app/build.gradle.kts` | exact |
| `ui/badge/BadgeDetailViewModel.kt` (updatedAt writes) | provider | CRUD | `ui/badge/BadgeDetailViewModel.kt` | exact |
| UI string migration (~12 Composables) | component | transform | — (no `stringResource` yet) | no analog |
| Test files (`ExportRoundTripTest`, etc.) | test | transform | `ui/badge/BadgeDetailViewModelProgressTest.kt` | exact |

## Pattern Assignments

### `data/export/BackupDto.kt` (model, transform)

**Analog:** `data/seed/CatalogSeedDto.kt`

**Imports pattern** (lines 1-7):
```kotlin
package se.simmarken.data.seed

import kotlinx.serialization.Serializable
import se.simmarken.data.local.entity.BadgeEntity
import se.simmarken.data.local.entity.CatalogEntity
```

**Core @Serializable DTO pattern** (lines 9-44):
```kotlin
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
data class RequirementSeedDto(
    val code: String,
    val textSv: String,
    val textEn: String,
    val sortOrder: Int,
)
```

**Apply:** Mirror nested `@Serializable` data classes with `exportVersion: Int = 1`, catalog codes (not Room IDs), and per-row `updatedAtEpochMillis`. Place in `se.simmarken.data.export` package; no Room entity imports in DTOs.

---

### `data/export/ExportJson.kt` (utility, transform)

**Analog:** `data/seed/CatalogSeedParser.kt`

**Json config pattern** (lines 6-22):
```kotlin
object CatalogSeedParser {
    private val testJson = Json {
        ignoreUnknownKeys = true
    }

    val strictJson = Json {
        ignoreUnknownKeys = false
    }

    fun loadFromString(json: String): CatalogSeedDto {
        return testJson.decodeFromString(CatalogSeedDto.serializer(), text)
    }
}
```

**Apply:** Create `ExportJson` object with `strictJson` (import validation) and `lenientJson` (`ignoreUnknownKeys = true` for same-major forward compat per RESEARCH). Expose `encode(BackupDto)` / `decode(String)` helpers using `.serializer()`.

---

### `data/export/ExportRepository.kt` (service, CRUD + file-I/O + transform)

**Analog:** `data/seed/CatalogSeedLoader.kt` + `domain/repository/ProgressRepositoryImpl.kt`

**Transactional merge pattern** (CatalogSeedLoader lines 26-48):
```kotlin
suspend fun mergeCatalog(seed: CatalogSeedDto) {
    database.withTransaction {
        val stored = catalogDao.findCatalogByCode(seed.code)
        catalogDao.upsertCatalog(
            seed.toEntity(existingId = stored?.id ?: 0),
        )
        val catalogId = catalogDao.findCatalogByCode(seed.code)!!.id
        // ... per-item merge keyed by code ...
    }
}
```

**Repository pass-through + DAO delegation** (ProgressRepositoryImpl lines 7-26):
```kotlin
class ProgressRepositoryImpl(
    private val database: AppDatabase,
) : ProgressRepository {
    private val requirementProgressDao = database.requirementProgressDao()
    private val badgeProgressDao = database.badgeProgressDao()

    override suspend fun upsertRequirementProgress(progress: RequirementProgressEntity) =
        requirementProgressDao.upsert(progress)
}
```

**Code lookup for ID remap** (CatalogDao lines 41-70):
```kotlin
@Query("SELECT * FROM catalogs WHERE code = :code LIMIT 1")
suspend fun findCatalogByCode(code: String): CatalogEntity?

@Query("SELECT * FROM requirements WHERE badgeId = :badgeId AND code = :code LIMIT 1")
suspend fun findRequirementByBadgeAndCode(badgeId: Long, code: String): RequirementEntity?
```

**Apply:** `ExportRepository` reads kids/progress via repos, maps Room IDs → catalog codes for export; on merge, uses `CatalogDao` code lookups to remap to local IDs. Wrap merge writes in `database.withTransaction { }`. IO on `Dispatchers.IO` (match `HomeViewModel.confirmDelete` line 129).

---

### `domain/export/BackupValidator.kt` (utility, transform)

**Analog:** `domain/validation/KidNameValidation.kt`

**Validation object pattern** (lines 3-11):
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

**Apply:** `object BackupValidator` returning sealed result (`Valid` / `Invalid(reason)`). Check `exportVersion`, required fields, non-empty kids list. Return user-facing error keys (not hardcoded strings — use string resource IDs or enum mapped in ViewModel).

---

### `domain/export/MergePlanner.kt` (service, batch + transform)

**Analog:** `domain/ProgressWriteLogic.kt` + `CatalogSeedLoader.mergeCatalog`

**Pure domain logic object** (ProgressWriteLogic lines 5-23):
```kotlin
object ProgressWriteLogic {
    fun shouldSetAchievedAt(
        totalRequirements: Int,
        achievedCountAfterToggle: Int,
        existingAchievedAt: Long?,
    ): Boolean =
        totalRequirements > 0 &&
            achievedCountAfterToggle >= totalRequirements &&
            existingAchievedAt == null

    fun preserveAchievedAt(existing: Long?, proposed: Long?): Long? = existing ?: proposed
}
```

**Apply:** Pure Kotlin `object MergePlanner` — no Android/Room imports. Build immutable `ImportPreview` from local + remote DTOs. Newer-wins: `remoteUpdated > localUpdated`. Separate buckets: updates, new kids (need confirm), skipped unknown codes.

---

### `ui/settings/SettingsScreen.kt` (component, request-response)

**Analog:** `ui/child/ChildCatalogScreen.kt`

**Scaffold + back navigation pattern** (lines 40-52):
```kotlin
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

**Apply:** Same `Scaffold` + `CenterAlignedTopAppBar` + back `IconButton`. Group Language and Data sections with Material 3 list/section components. Use `stringResource` for all chrome (first screen to establish i18n pattern). Wire export/import actions via ViewModel callbacks; launch share sheet / SAF picker from screen using `rememberLauncherForActivityResult`.

---

### `ui/settings/SettingsViewModel.kt` (provider, event-driven)

**Analog:** `ui/home/HomeViewModel.kt`

**UiState + combine + stateIn pattern** (lines 29-101):
```kotlin
data class HomeUiState(
    val kids: List<KidEntity> = emptyList(),
    val summariesByKidId: Map<Long, KidProgressSummary> = emptyMap(),
    val sheetState: KidSheetState = KidSheetState.Hidden,
    val deleteTarget: KidEntity? = null,
)

val uiState = combine(
    kidRepository.observeAll(),
    summariesByKidId,
    sheetState,
    deleteTarget,
) { kids, summaries, sheet, delete ->
    HomeUiState(...)
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000),
    initialValue = HomeUiState(),
)
```

**IO write pattern** (lines 126-131):
```kotlin
fun confirmDelete() {
    val kid = deleteTarget.value ?: return
    deleteTarget.value = null
    viewModelScope.launch(Dispatchers.IO) {
        kidRepository.delete(kid.id)
    }
}
```

**Apply:** `SettingsUiState` holds language mode, kids list, export picker visibility, import preview, error/success messages. Export: if `kids.size == 1` skip picker (D-06). Import: parse on IO, expose preview, confirm triggers `ExportRepository.merge()`. Observe `LocalePreferencesRepository` Flow for language radio state.

---

### `ui/settings/SettingsViewModelFactory.kt` (provider)

**Analog:** `navigation/HomeViewModelFactory.kt`

**Factory pattern** (lines 10-25):
```kotlin
class HomeViewModelFactory(
    private val kidRepository: KidRepository,
    private val catalogRepository: CatalogRepository,
    private val progressRepository: ProgressRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(...) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
```

**Apply:** Inject `ExportRepository`, `LocalePreferencesRepository`, `KidRepository`. Place factory in `navigation/` or `ui/settings/` matching existing convention (`HomeViewModelFactory` lives in `navigation/`).

---

### `ui/settings/ExportKidPickerDialog.kt` + `ImportConfirmDialog.kt` (component, event-driven)

**Analog:** `ui/home/components/DeleteKidDialog.kt`

**AlertDialog pattern** (lines 9-34):
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

**Apply:** Export picker — checkbox list inside `AlertDialog` or dedicated dialog composable; default All selected. Import confirm — summary text + checklist for new kids (D-02). All strings via `stringResource`. Confirm/dismiss buttons match existing `TextButton` style.

---

### `navigation/Routes.kt` (route)

**Analog:** `navigation/Routes.kt` (extend in place)

**Type-safe route pattern** (lines 1-12):
```kotlin
package se.simmarken.navigation

import kotlinx.serialization.Serializable

@Serializable
object Home

@Serializable
data class ChildCatalog(val kidId: Long)

@Serializable
data class BadgeDetail(val kidId: Long, val badgeId: Long)
```

**Apply:** Add `@Serializable object Settings`. No arguments needed unless passing pending import Uri (prefer ViewModel/singleton session over route args).

---

### `navigation/SimmarkenNavHost.kt` (route)

**Analog:** `navigation/SimmarkenNavHost.kt` (extend)

**Composable route registration** (lines 23-66):
```kotlin
NavHost(navController = navController, startDestination = Home) {
    composable<Home> {
        val viewModel: HomeViewModel = viewModel(
            factory = HomeViewModelFactory(
                kidRepository = application.container.kidRepository,
                catalogRepository = application.container.catalogRepository,
                progressRepository = application.container.progressRepository,
            ),
        )
        HomeScreen(viewModel = viewModel, navController = navController)
    }
    composable<ChildCatalog> { backStackEntry ->
        val route = backStackEntry.toRoute<ChildCatalog>()
        // ...
        ChildCatalogScreen(
            viewModel = catalogViewModel,
            onBack = { navController.popBackStack() },
            onBadgeClick = { ... },
        )
    }
}
```

**Apply:** Add `composable<Settings>` block with `SettingsViewModelFactory` pulling new container deps. Pass `onBack = { navController.popBackStack() }`.

---

### `di/AppContainer.kt` (config)

**Analog:** `di/AppContainer.kt` (extend)

**Manual DI wiring** (lines 18-35):
```kotlin
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database: AppDatabase = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        AppDatabase.DB_NAME,
    ).build()

    val kidRepository: KidRepository = KidRepositoryImpl(database.kidDao())
    val catalogRepository: CatalogRepository = CatalogRepositoryImpl(database.catalogDao())
    val progressRepository: ProgressRepository = ProgressRepositoryImpl(database)
}
```

**Apply:** Add `.addMigrations(MIGRATION_1_2)` to Room builder. Wire `localePreferencesRepository` (DataStore via `PreferenceDataStoreFactory.create`). Wire `exportRepository` with database + kid/progress/catalog repos. No Hilt — keep manual construction.

---

### Entity + Room migration (`KidEntity`, progress entities, `AppDatabase`)

**Analog:** `data/local/entity/KidEntity.kt` + `data/local/AppDatabase.kt`

**Entity pattern** (KidEntity lines 6-13):
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

**Database declaration** (AppDatabase lines 19-50):
```kotlin
@Database(
    entities = [
        CatalogEntity::class,
        // ...
        KidEntity::class,
        RequirementProgressEntity::class,
        BadgeProgressEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    @Transaction
    suspend fun applyRequirementToggle(
        requirementProgress: RequirementProgressEntity,
        badgeProgress: BadgeProgressEntity?,
    ) {
        badgeProgress?.let { badgeProgressDao().upsert(it) }
        requirementProgressDao().upsert(requirementProgress)
    }
}
```

**Apply:** Bump `version = 2`. Add `KidEntity.stableId: String`, `updatedAtEpochMillis` on both progress entities. Migration backfills UUIDs for existing kids and sets `updatedAt` from `coalesce(achievedAt, gottenAt, createdAt, now)`. Add `@Index("stableId")` on kids. Update `KidFormViewModel` insert to generate UUID.

---

### `domain/repository/KidRepository.kt` + `ProgressRepository.kt` extensions

**Analog:** existing interfaces + impls

**Interface style** (KidRepository lines 6-11):
```kotlin
interface KidRepository {
    fun observeAll(): Flow<List<KidEntity>>
    fun observeById(kidId: Long): Flow<KidEntity?>
    suspend fun upsert(kid: KidEntity): Long
    suspend fun delete(kidId: Long)
}
```

**DAO upsert** (KidDao lines 18-19):
```kotlin
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun upsert(kid: KidEntity): Long
```

**Apply:** Add `suspend fun findByStableId(stableId: String): KidEntity?`, bulk progress getters for export (`getRequirementProgressForKids`, etc.), and transactional merge method or reuse `database.withTransaction` in ExportRepository.

---

### `ui/home/HomeScreen.kt` — settings gear (component)

**Analog:** `ui/child/ChildCatalogScreen.kt` navigation icon + `HomeScreen` top bar

**Top bar today** (HomeScreen lines 46-49):
```kotlin
CenterAlignedTopAppBar(
    title = { Text("Simmärken") },
)
```

**Navigation icon analog** (ChildCatalogScreen lines 44-50):
```kotlin
navigationIcon = {
    IconButton(onClick = onBack) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Tillbaka",
        )
    }
},
```

**Apply:** Add `actions = { IconButton(onClick = onSettingsClick) { Icon(Icons.Default.Settings, ...) } }` to top bar. Pass `onSettingsClick = { navController.navigate(Settings) }`. Use `stringResource(R.string.settings)` for contentDescription.

---

### `MainActivity.kt` + `SimmarkenApplication.kt` (locale + intents)

**Analog:** `MainActivity.kt` + `SimmarkenApplication.kt`

**Current Activity** (MainActivity lines 10-19):
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SimmarkenTheme {
                SimmarkenNavHost()
            }
        }
    }
}
```

**Application container** (SimmarkenApplication lines 6-13):
```kotlin
class SimmarkenApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
```

**Apply:** Change to `AppCompatActivity`. Theme parent → `Theme.AppCompat.Light.NoActionBar`. On startup, read DataStore and call `AppCompatDelegate.setApplicationLocales`. Handle `ACTION_VIEW` / `ACTION_SEND` in `onCreate`/`onNewIntent` — stash Uri, navigate to Settings import preview (no auto-write). **No codebase analog for AppCompat locales** — follow RESEARCH.md Pattern 3.

---

### `res/values/strings.xml` + `values-en/strings.xml` (i18n)

**Analog:** `res/values/strings.xml` (minimal today)

**Current pattern** (strings.xml lines 1-3):
```xml
<resources>
    <string name="app_name">Simmärken</string>
</resources>
```

**Hardcoded strings to extract** (examples):

ChildCard (lines 77-87):
```kotlin
text = "$inProgressCount pågår"
text = "$toBuyCount att köpa"
```

DeleteKidDialog (lines 17-31):
```kotlin
title = { Text("Ta bort $name?") }
Text("Avbryt")
```

CatalogTabRow (lines 24-29) — **keep hardcoded per D-16**:
```kotlin
text = { Text("Simidrott") }
text = { Text("SLS") }
```

**Apply:** Swedish in `values/strings.xml`, English in `values-en/strings.xml`. Use `stringResource(R.string.key, arg)` for plurals/counts. Catalog badge/requirement text stays `nameSv`/`textSv` in mappers (D-13) — do not add `nameEn` switching.

---

### `res/values/themes.xml` (AppCompat migration)

**Analog:** `res/values/themes.xml`

**Current theme** (themes.xml lines 1-4):
```xml
<resources>
    <style name="Theme.Simmarken" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
```

**Apply:** Change parent to `Theme.AppCompat.Light.NoActionBar`. Keep Compose Material 3 inside `setContent`.

---

### `ui/badge/BadgeDetailViewModel.kt` (updatedAt on writes)

**Analog:** same file — extend write paths

**Progress write pattern** (lines 151-160):
```kotlin
val requirementProgress = RequirementProgressEntity(
    kidId = kidId,
    requirementId = requirementId,
    isAchieved = flipped,
    achievedAtEpochMillis = if (flipped) System.currentTimeMillis() else null,
)
progressRepository.applyRequirementToggle(
    requirementProgress = requirementProgress,
    badgeProgress = badgeProgress.takeIf { badgeProgressDirty },
)
```

**Apply:** Add `updatedAtEpochMillis = System.currentTimeMillis()` on every requirement/badge upsert (toggle, purchase, uncheck). Required for D-04 newer-wins merge.

---

### `app/build.gradle.kts` + `gradle/libs.versions.toml` (deps)

**Analog:** existing Gradle files

**Dependency block pattern** (build.gradle.kts lines 67-85):
```kotlin
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    // ...
    implementation(libs.kotlinx.serialization.json)
}
```

**Version catalog pattern** (libs.versions.toml lines 19-35):
```toml
[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "serialization" }
```

**Apply:** Add `datastore = "1.2.1"`, `appcompat = "1.7.1"` to `[versions]`; add library entries; `implementation(libs.androidx.datastore.preferences)` and `implementation(libs.androidx.appcompat)`.

---

### Test files (`ExportRoundTripTest`, `BackupValidatorTest`, `MergePlannerTest`, `LocalePreferencesMappingTest`)

**Analog:** `app/src/test/java/se/simmarken/ui/badge/BadgeDetailViewModelProgressTest.kt`

**Test setup pattern** (lines 31-43):
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class BadgeDetailViewModelProgressTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
```

**Fake repository pattern** (lines 454-497):
```kotlin
private class FakeProgressRepository : ProgressRepository {
    private val requirementProgress = MutableStateFlow<List<RequirementProgressEntity>>(emptyList())
    var lastRequirementUpsert: RequirementProgressEntity? = null

    override suspend fun upsertRequirementProgress(progress: RequirementProgressEntity) {
        lastRequirementUpsert = progress
        val updated = requirementProgress.value
            .filterNot { it.kidId == progress.kidId && it.requirementId == progress.requirementId } +
            progress
        requirementProgress.value = updated
    }
}
```

**Apply:** JVM unit tests under `app/src/test/java/se/simmarken/data/export/` and `domain/export/`. No Mockito — inline fakes like above. `runTest` + `advanceUntilIdle` for ViewModel tests. Room migration test can use `androidx.room:room-testing` (already in `androidTestImplementation`).

---

## Shared Patterns

### MVVM + Manual ViewModel Factories
**Source:** `navigation/HomeViewModelFactory.kt`, `SimmarkenNavHost.kt`
**Apply to:** Settings screen, any new ViewModel

```kotlin
val viewModel: HomeViewModel = viewModel(
    factory = HomeViewModelFactory(
        kidRepository = application.container.kidRepository,
        catalogRepository = application.container.catalogRepository,
        progressRepository = application.container.progressRepository,
    ),
)
```

### Repository IO on Dispatchers.IO
**Source:** `ui/home/HomeViewModel.kt` (line 129), `ui/home/KidFormViewModel.kt` (line 81)
**Apply to:** ExportRepository calls, import merge, locale persist

```kotlin
viewModelScope.launch(Dispatchers.IO) {
    kidRepository.delete(kid.id)
}
```

### kotlinx-serialization JSON
**Source:** `data/seed/CatalogSeedParser.kt`, `data/seed/CatalogSeedDto.kt`
**Apply to:** BackupDto encode/decode — never hand-roll JSON

```kotlin
val strictJson = Json { ignoreUnknownKeys = false }
strictJson.decodeFromString(BackupDto.serializer(), text)
```

### Room Transactions for Multi-Table Writes
**Source:** `data/seed/CatalogSeedLoader.kt`, `data/local/AppDatabase.kt`
**Apply to:** Import merge (progress upserts + optional kid creates)

```kotlin
database.withTransaction {
    // multiple dao upserts
}
```

### Catalog Code as Cross-Device Identity
**Source:** `data/local/dao/CatalogDao.kt`, `data/seed/CatalogSeedLoader.kt`
**Apply to:** Export/import progress remap — never export Room `requirementId`/`badgeId` Longs

```kotlin
suspend fun findRequirementByBadgeAndCode(badgeId: Long, code: String): RequirementEntity?
```

### Pure Domain Objects for Business Rules
**Source:** `domain/ProgressWriteLogic.kt`, `domain/validation/KidNameValidation.kt`
**Apply to:** MergePlanner, BackupValidator — no Android imports

### Confirmation Before Destructive/Mutating Actions
**Source:** `ui/home/components/DeleteKidDialog.kt`, `HomeViewModel.requestDelete`
**Apply to:** Import confirm (D-03), per-new-kid selection (D-02)

### Catalog Display Language (D-13)
**Source:** `ui/badge/BadgeDetailViewModel.kt` (lines 70-73)
**Apply to:** All catalog mappers — always bind Swedish fields

```kotlin
RequirementRowUiModel(
    id = requirement.id,
    textSv = requirement.textSv,
    isAchieved = requirementProgressById[requirement.id] == true,
)
```

## No Analog Found

| File | Role | Data Flow | Reason |
|------|------|-----------|--------|
| `data/prefs/LocalePreferencesRepository.kt` | repository | CRUD | No DataStore usage in codebase yet |
| `res/xml/file_paths.xml` + FileProvider manifest | config | file-I/O | No FileProvider configured |
| `AndroidManifest.xml` intent filters | config | event-driven | Only LAUNCHER filter exists today |
| UI `stringResource` migration | component | transform | Zero `stringResource` calls in project — establish new pattern from RESEARCH.md |
| Room migration v1→v2 | migration | CRUD | DB at version 1 with no migrations; follow Room Migration API + RESEARCH backfill spec |
| `ActivityResultContracts.OpenDocument` | — | file-I/O | No SAF/activity-result usage in codebase |
| Share sheet `ACTION_SEND` launcher | — | file-I/O | No share intent usage in codebase |

**Planner guidance for no-analog files:** Use RESEARCH.md Code Examples sections (FileProvider share, manifest filters, `AppCompatDelegate.setApplicationLocales`, DataStore Preferences flow). For `stringResource`, standard Compose pattern: `import androidx.compose.ui.res.stringResource` + `Text(stringResource(R.string.key))`.

## Metadata

**Analog search scope:** `app/src/main/java/se/simmarken/**`, `app/src/test/**`, `app/src/main/res/**`, `app/build.gradle.kts`, `gradle/libs.versions.toml`
**Files scanned:** ~45 Kotlin/XML/Gradle files
**Pattern extraction date:** 2026-07-24
