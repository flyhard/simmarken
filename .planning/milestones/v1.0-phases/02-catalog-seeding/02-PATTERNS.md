# Phase 2: Catalog Seeding - Pattern Map

**Mapped:** 2026-07-22
**Files analyzed:** 18 new/modified files
**Analogs found:** 12 / 18

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|-------------------|------|-----------|----------------|---------------|
| `app/src/main/java/se/simmarken/data/seed/CatalogSeedDto.kt` | model | transform | `entity/CatalogEntity.kt` + `navigation/Routes.kt` | role-match |
| `app/src/main/java/se/simmarken/data/seed/CatalogSeedParser.kt` | utility | file-I/O | `navigation/Routes.kt` (serialization only) | partial |
| `app/src/main/java/se/simmarken/data/seed/CatalogSeedLoader.kt` | service | batch + CRUD | `DaoInstrumentedTest.kt` + `CatalogRepositoryImpl.kt` | role-match |
| `app/src/main/java/se/simmarken/data/seed/CatalogVersion.kt` | utility | transform | — | no analog |
| `app/src/main/java/se/simmarken/data/local/dao/CatalogDao.kt` | DAO | CRUD | existing `CatalogDao.kt` | exact |
| `app/src/main/java/se/simmarken/di/AppContainer.kt` | provider | event-driven | `AppContainer.kt` + `HomeViewModel.kt` | exact |
| `app/src/main/assets/seed/simidrott.json` | config | file-I/O | — | no analog |
| `app/src/main/assets/seed/sls.json` | config | file-I/O | — | no analog |
| `app/src/main/assets/badges/simidrott/*.webp` | config | file-I/O | — | no analog |
| `app/src/main/assets/badges/sls/*.webp` | config | file-I/O | — | no analog |
| `docs/SOURCES.md` | config | — | — | no analog |
| `app/src/test/java/se/simmarken/data/seed/CatalogVersionTest.kt` | test | transform | `PlaceholderUnitTest.kt` | role-match |
| `app/src/test/java/se/simmarken/data/seed/CatalogSeedParserTest.kt` | test | file-I/O + transform | `PlaceholderUnitTest.kt` | role-match |
| `app/src/test/java/se/simmarken/data/seed/SimidrottRequirementAccuracyTest.kt` | test | transform | `DaoInstrumentedTest.kt` (assertions) | partial |
| `app/src/androidTest/java/se/simmarken/data/seed/CatalogSeedLoaderTest.kt` | test | batch + CRUD | `DaoInstrumentedTest.kt` | exact |
| `app/src/androidTest/java/se/simmarken/data/seed/CatalogSeedMergeTest.kt` | test | batch + CRUD | `DaoInstrumentedTest.kt` + `DatabasePersistenceTest.kt` | exact |
| `app/src/test/resources/seed/simidrott_sample.json` | config | file-I/O | — | no analog |

## Pattern Assignments

### `CatalogSeedDto.kt` (model, transform)

**Analog:** `entity/CatalogEntity.kt` (field names) + `navigation/Routes.kt` (serialization)

**Imports pattern** from `Routes.kt` (lines 1-6):

```kotlin
package se.simmarken.navigation

import kotlinx.serialization.Serializable

@Serializable
object Home
```

**Entity field alignment** from `CatalogEntity.kt` (lines 11-18):

```kotlin
data class CatalogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val nameSv: String,
    val nameEn: String,
    val catalogVersion: String,
    val sortOrder: Int = 0,
)
```

**Nested entity fields** — mirror all four entity types:

```kotlin
// CategoryEntity.kt lines 23-30
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val catalogId: Long,
    val code: String,
    val nameSv: String,
    val nameEn: String,
    val sortOrder: Int,
)

// BadgeEntity.kt lines 23-31
data class BadgeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val code: String,
    val nameSv: String,
    val nameEn: String,
    val imageAssetPath: String?,
    val sortOrder: Int,
)

// RequirementEntity.kt lines 23-30
data class RequirementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val badgeId: Long,
    val code: String,
    val textSv: String,
    val textEn: String,
    val sortOrder: Int,
)
```

**Core pattern:** DTOs omit Room-specific fields (`id`, FK longs). Add `toEntity(parentId, existingId?)` extension functions in the same file or a companion mapper. Use `@Serializable` on each DTO; nest `categories → badges → requirements` matching JSON hierarchy from RESEARCH.md.

**Unique code indices** already enforced on entities — DTO `code` values must be unique within parent scope:

```kotlin
// CategoryEntity.kt lines 18-21
indices = [
    Index("catalogId"),
    Index(value = ["catalogId", "code"], unique = true),
],
```

---

### `CatalogSeedParser.kt` (utility, file-I/O)

**Analog:** `navigation/Routes.kt` (serialization config only — no asset-reading analog exists)

**Package placement:** `se.simmarken.data.seed` per ARCHITECTURE.md; sits alongside `data/local/`.

**Json config pattern** (from RESEARCH.md, use in parser):

```kotlin
private val json = Json {
    ignoreUnknownKeys = true  // unit tests with partial fixtures
    // Production seed files: consider ignoreUnknownKeys = false per V5 validation
}

fun loadCatalogSeed(context: Context, assetPath: String): CatalogSeedDto {
    val text = context.assets.open(assetPath).bufferedReader().use { it.readText() }
    return json.decodeFromString(CatalogSeedDto.serializer(), text)
}
```

**Unit test variant** — read from classpath instead of assets:

```kotlin
// For CatalogSeedParserTest: use javaClass.classLoader.getResource("seed/simidrott_sample.json")
```

**Error handling:** Let `Json` throw `SerializationException` on malformed input; loader catches and logs/fails fast. No try/catch wrapper exists in codebase yet — keep propagation simple.

---

### `CatalogSeedLoader.kt` (service, batch + CRUD)

**Analog:** `DaoInstrumentedTest.kt` (insert chain) + `CatalogRepositoryImpl.kt` (thin DAO delegation)

**Insert chain pattern** from `DaoInstrumentedTest.kt` (lines 39-75):

```kotlin
val catalogId = db.catalogDao().upsertCatalog(
    CatalogEntity(
        code = "simidrott",
        nameSv = "Svensk Simidrott",
        nameEn = "Swedish Swimming",
        catalogVersion = "1",
        sortOrder = 0,
    ),
)
val categoryId = db.catalogDao().upsertCategory(
    CategoryEntity(
        catalogId = catalogId,
        code = "grund",
        nameSv = "Grund",
        nameEn = "Basic",
        sortOrder = 0,
    ),
)
val badgeId = db.catalogDao().upsertBadge(
    BadgeEntity(
        categoryId = categoryId,
        code = "simmare",
        nameSv = "Simmare",
        nameEn = "Swimmer",
        imageAssetPath = null,
        sortOrder = 0,
    ),
)
db.catalogDao().upsertRequirement(
    RequirementEntity(
        badgeId = badgeId,
        code = "req1",
        textSv = "Simma 25 meter",
        textEn = "Swim 25 meters",
        sortOrder = 0,
    ),
)
```

**ID-preserving merge** — extend chain with lookup-before-upsert (RESEARCH.md Pattern 2):

```kotlin
suspend fun mergeCategory(catalogId: Long, seed: CategorySeedDto) {
    val existing = catalogDao.findCategoryByCatalogAndCode(catalogId, seed.code)
    val entity = seed.toEntity(
        id = existing?.id ?: 0,
        catalogId = catalogId,
    )
    catalogDao.upsertCategory(entity)
    val categoryId = existing?.id ?: entity.id // resolve after upsert
    seed.badges.forEach { mergeBadge(categoryId, it) }
}
```

**DAO upsert return type** — existing methods return `Long` (inserted row id):

```kotlin
// CatalogDao.kt lines 27-37
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun upsertCatalog(catalog: CatalogEntity): Long

@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun upsertCategory(category: CategoryEntity): Long
```

When `id = 0`, REPLACE inserts new row. When `id = existing.id`, REPLACE updates in place — **critical for progress FK preservation** (D-15).

**Version gate** before merge:

```kotlin
suspend fun seedIfNeeded() {
    for (assetPath in listOf("seed/simidrott.json", "seed/sls.json")) {
        val seed = parser.load(context, assetPath)
        val stored = catalogDao.findCatalogByCode(seed.code)
        if (CatalogVersion.shouldMerge(seed.catalogVersion, stored?.catalogVersion)) {
            mergeCatalog(seed)
        }
    }
}
```

**IO dispatcher** — match `HomeViewModel.kt` (lines 28-38):

```kotlin
viewModelScope.launch(Dispatchers.IO) {
    kidRepository.upsert(/* ... */)
}
```

Loader invoked from `AppContainer` init via `CoroutineScope(SupervisorJob() + Dispatchers.IO).launch { ... }`.

**Repository stays thin** — loader talks to `CatalogDao` directly, not `CatalogRepository`:

```kotlin
// CatalogRepositoryImpl.kt — pass-through only, no business logic
override suspend fun upsertCatalog(catalog: CatalogEntity) = catalogDao.upsertCatalog(catalog)
```

---

### `CatalogVersion.kt` (utility, transform)

**Analog:** None in codebase

**Use RESEARCH.md pattern** — comparable `YYYY.MM.DD` strings:

```kotlin
fun shouldMerge(bundled: String, stored: String?): Boolean {
    if (stored == null) return true
    return parse(bundled) > parse(stored)
}
```

**Unit test home:** `app/src/test/java/se/simmarken/data/seed/CatalogVersionTest.kt` — follow `PlaceholderUnitTest.kt` structure (plain JUnit, no Android deps).

---

### `CatalogDao.kt` (DAO, CRUD) — modifications

**Analog:** existing `CatalogDao.kt` (exact)

**Existing observe + upsert pattern** (lines 15-37):

```kotlin
@Query("SELECT * FROM catalogs ORDER BY sortOrder, nameSv")
fun observeCatalogs(): Flow<List<CatalogEntity>>

@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun upsertCatalog(catalog: CatalogEntity): Long
```

**Add lookup-by-code queries** (RESEARCH.md):

```kotlin
@Query("SELECT * FROM catalogs WHERE code = :code LIMIT 1")
suspend fun findCatalogByCode(code: String): CatalogEntity?

@Query("SELECT * FROM categories WHERE catalogId = :catalogId AND code = :code LIMIT 1")
suspend fun findCategoryByCatalogAndCode(catalogId: Long, code: String): CategoryEntity?

@Query("SELECT * FROM badges WHERE categoryId = :categoryId AND code = :code LIMIT 1")
suspend fun findBadgeByCategoryAndCode(categoryId: Long, code: String): BadgeEntity?

@Query("SELECT * FROM requirements WHERE badgeId = :badgeId AND code = :code LIMIT 1")
suspend fun findRequirementByBadgeAndCode(badgeId: Long, code: String): RequirementEntity?
```

**Optional:** `@Transaction suspend fun mergeCatalog(seed: ...)` on DAO or loader — Room `@Transaction` not used anywhere yet; add on loader class method wrapping multiple DAO calls.

**Pitfall:** Current `@Insert(REPLACE)` with `id = 0` creates duplicates on re-seed. Merge must set existing `id` before upsert.

---

### `AppContainer.kt` (provider, event-driven) — modifications

**Analog:** `AppContainer.kt` + `SimmarkenApplication.kt`

**Existing container** (lines 13-26):

```kotlin
class AppContainer(context: Context) {
    private val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        AppDatabase.DB_NAME,
    ).build()

    val kidRepository: KidRepository = KidRepositoryImpl(database.kidDao())
    val catalogRepository: CatalogRepository = CatalogRepositoryImpl(database.catalogDao())
    val progressRepository: ProgressRepository = ProgressRepositoryImpl(
        requirementProgressDao = database.requirementProgressDao(),
        badgeProgressDao = database.badgeProgressDao(),
    )
}
```

**Application wiring** from `SimmarkenApplication.kt` (lines 6-13):

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

**Add seed launch in AppContainer init:**

```kotlin
init {
    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
        CatalogSeedLoader(context, database.catalogDao()).seedIfNeeded()
    }
}
```

Pass `context.applicationContext` to loader for asset access. Do not block `onCreate`.

---

### `CatalogSeedLoaderTest.kt` (test, batch + CRUD)

**Analog:** `DaoInstrumentedTest.kt` (exact)

**Test harness** (lines 21-37):

```kotlin
@RunWith(AndroidJUnit4::class)
class DaoInstrumentedTest {
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(AppDatabase.DB_NAME)
    }

    @Test
    fun catalogChainInsertsWithForeignKeys() = runBlocking {
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DB_NAME,
        ).build()
        // ...
        db.close()
    }
}
```

**Seed loader test pattern:**

```kotlin
@Test
fun firstRunPopulatesSimidrottCatalog() = runBlocking {
    val db = buildDb()
    CatalogSeedLoader(context, db.catalogDao()).seedIfNeeded()
    val catalogs = db.catalogDao().observeCatalogs().first()
    assertTrue(catalogs.any { it.code == "simidrott" })
    db.close()
}
```

Use `kotlinx.coroutines.flow.first` (already imported in DaoInstrumentedTest).

---

### `CatalogSeedMergeTest.kt` (test, batch + CRUD)

**Analog:** `DaoInstrumentedTest.kt` (progress FK setup) + `DatabasePersistenceTest.kt` (survives re-open)

**Progress FK chain** from `DaoInstrumentedTest.kt` (lines 67-91):

```kotlin
val requirementId = db.catalogDao().upsertRequirement(
    RequirementEntity(
        badgeId = badgeId,
        code = "req1",
        textSv = "Simma 25 meter",
        textEn = "Swim 25 meters",
        sortOrder = 0,
    ),
)
val kidId = db.kidDao().upsert(/* KidEntity */)
db.requirementProgressDao().upsert(
    RequirementProgressEntity(
        kidId = kidId,
        requirementId = requirementId,
        isAchieved = true,
        achievedAtEpochMillis = 1000L,
    ),
)
```

**Merge test assertion pattern:**

1. Seed catalog v1 → insert progress on `requirementId`
2. Re-seed with bumped `catalogVersion` and changed `textSv` on same `code`
3. Assert `requirement_progress` row count unchanged and `isAchieved` still true
4. Assert `textSv` updated on requirement row

**FK cascade note** from `RequirementProgressEntity.kt` (lines 17-21):

```kotlin
ForeignKey(
    entity = RequirementEntity::class,
    parentColumns = ["id"],
    childColumns = ["requirementId"],
    onDelete = ForeignKey.CASCADE,
),
```

Deleting/replacing requirement rows by PK change destroys progress — ID preservation is mandatory.

---

### `CatalogVersionTest.kt` / `CatalogSeedParserTest.kt` (test, unit)

**Analog:** `PlaceholderUnitTest.kt`

```kotlin
package se.simmarken

import org.junit.Assert.assertTrue
import org.junit.Test

class PlaceholderUnitTest {
    @Test
    fun placeholderPasses() {
        assertTrue(true)
    }
}
```

Place under `app/src/test/java/se/simmarken/data/seed/`. No Android dependencies. Parser test loads `app/src/test/resources/seed/simidrott_sample.json` via classloader.

---

### `SimidrottRequirementAccuracyTest.kt` (test, transform)

**Analog:** `DaoInstrumentedTest.kt` (assertion style) — no golden-file test exists

**Pattern:** Hardcode expected bullet counts/text from official PDFs in test constants; compare against parsed seed fixture (not Room). Keeps D-11/D-12 verification automated for sample badges (Baddaren, Hajen).

---

### Asset files (`simidrott.json`, `sls.json`, `*.webp`)

**Analog:** None — first assets in project

**JSON structure** per RESEARCH.md Pattern 1:

```json
{
  "code": "simidrott",
  "nameSv": "Svensk Simidrott",
  "nameEn": "Swedish Swimming",
  "catalogVersion": "2026.03.02",
  "sortOrder": 0,
  "categories": [ /* ... */ ]
}
```

**Image paths:** `imageAssetPath` values like `"badges/simidrott/baddaren_gron.webp"` — relative to `assets/` root (Coil `file:///android_asset/` convention in Phase 4).

**Code naming** per RESEARCH.md Pattern 4: ASCII slugs (`baddaren-gron`, `hajen-silver`).

---

### `docs/SOURCES.md`

**Analog:** None

Document URLs, extraction dates, licensing per D-03/D-16. No DB provenance fields.

## Shared Patterns

### Package and Layering

**Source:** `di/AppContainer.kt`, `domain/repository/CatalogRepositoryImpl.kt`

- `data/seed/` — parsing and loading (I/O + merge logic)
- `data/local/` — Room entities and DAOs
- `domain/repository/` — thin observe/upsert pass-through (loader bypasses repository)
- Manual DI via `AppContainer`; no Hilt

### Bilingual Fields

**Source:** all entity files

All display text uses `nameSv`/`nameEn` or `textSv`/`textEn`. Seed JSON must populate both. Swedish verbatim (D-09); English translated (D-10).

### Stable `code` as Natural Key

**Source:** entity unique indices

```kotlin
// CatalogEntity.kt line 9
indices = [Index(value = ["code"], unique = true)],

// RequirementEntity.kt lines 19-21
Index(value = ["badgeId", "code"], unique = true),
```

Progress preservation (D-15) depends on stable `code` + preserved `id`.

### Room Upsert Convention

**Source:** `CatalogDao.kt`, `KidDao.kt`

```kotlin
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun upsertCatalog(catalog: CatalogEntity): Long
```

Project uses `@Insert(REPLACE)` not `@Upsert` annotation. REPLACE on matching PK updates row; new PK inserts. Merge logic must pre-set PK from code lookup.

### Coroutine IO Dispatch

**Source:** `HomeViewModel.kt`

```kotlin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

viewModelScope.launch(Dispatchers.IO) { /* write */ }
```

All seed work on `Dispatchers.IO`. Tests use `runBlocking` (instrumented) per existing DAO tests.

### Instrumented Test Setup

**Source:** `DaoInstrumentedTest.kt`, `DatabasePersistenceTest.kt`

```kotlin
@Before
fun setup() {
    context = ApplicationProvider.getApplicationContext()
    context.deleteDatabase(AppDatabase.DB_NAME)
}
```

Always delete DB before seed tests for isolation.

### Ordering Queries

**Source:** `CatalogDao.kt`

```kotlin
@Query("SELECT * FROM categories WHERE catalogId = :catalogId ORDER BY sortOrder, nameSv")
fun observeCategories(catalogId: Long): Flow<List<CategoryEntity>>

@Query("SELECT * FROM requirements WHERE badgeId = :badgeId ORDER BY sortOrder, code")
fun observeRequirements(badgeId: Long): Flow<List<RequirementEntity>>
```

Seed `sortOrder` must match affisch progression (D-08). Verify in tests via ordered query results.

## No Analog Found

| File | Role | Data Flow | Reason |
|------|------|-----------|--------|
| `CatalogVersion.kt` | utility | transform | No version comparison utility in codebase; use RESEARCH.md comparator |
| `assets/seed/*.json` | config | file-I/O | First bundled JSON assets; no existing asset dir |
| `assets/badges/**/*.webp` | config | file-I/O | No image assets bundled yet |
| `docs/SOURCES.md` | config | — | No docs directory in repo yet |
| `test/resources/seed/simidrott_sample.json` | config | file-I/O | No test fixtures directory yet |
| `CatalogSeedParser.kt` (asset I/O half) | utility | file-I/O | No `context.assets` usage anywhere; serialization-only analog |

## Metadata

**Analog search scope:** `app/src/main/java/se/simmarken/`, `app/src/test/`, `app/src/androidTest/`
**Files scanned:** 34 Kotlin source files
**Pattern extraction date:** 2026-07-22
