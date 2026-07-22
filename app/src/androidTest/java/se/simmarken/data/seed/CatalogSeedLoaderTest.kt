package se.simmarken.data.seed

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import se.simmarken.data.local.AppDatabase
import se.simmarken.ui.badge.BadgePlaceholderColors

@RunWith(AndroidJUnit4::class)
class CatalogSeedLoaderTest {
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(AppDatabase.DB_NAME)
    }

    @Test
    fun firstRunPopulatesBothCatalogs() = runBlocking {
        val db = buildDatabase()
        CatalogSeedLoader(context, db).seedIfNeeded()

        val catalogs = db.catalogDao().observeCatalogs().first()
        assertTrue(catalogs.any { it.code == "simidrott" })
        assertTrue(catalogs.any { it.code == "sls" })

        db.close()
    }

    @Test
    fun categoriesOrderedBySortOrder() = runBlocking {
        val db = buildDatabase()
        CatalogSeedLoader(context, db).seedIfNeeded()

        val simidrott = db.catalogDao().findCatalogByCode("simidrott")
        assertNotNull(simidrott)
        val categories = db.catalogDao().observeCategories(simidrott!!.id).first()
        val sortOrders = categories.map { it.sortOrder }
        assertEquals(sortOrders, sortOrders.sorted())

        db.close()
    }

    @Test
    fun baddarenGronHasRequirements() = runBlocking {
        val db = buildDatabase()
        CatalogSeedLoader(context, db).seedIfNeeded()

        val simidrott = db.catalogDao().findCatalogByCode("simidrott")!!
        val categories = db.catalogDao().observeCategories(simidrott.id).first()
        val vattenvana = categories.first { it.code == "vattenvana" }
        val badges = db.catalogDao().observeBadges(vattenvana.id).first()
        val baddarenGron = badges.first { it.code == "baddaren-gron" }
        val requirements = db.catalogDao().observeRequirements(baddarenGron.id).first()

        assertTrue(requirements.isNotEmpty())

        db.close()
    }

    @Test
    fun badgeImagesResolvable() = runBlocking {
        val db = buildDatabase()
        CatalogSeedLoader(context, db).seedIfNeeded()

        val simidrott = db.catalogDao().findCatalogByCode("simidrott")!!
        val sls = db.catalogDao().findCatalogByCode("sls")!!
        val allBadges = listOf(simidrott.id, sls.id).flatMap { catalogId ->
            db.catalogDao().observeCategories(catalogId).first().flatMap { category ->
                db.catalogDao().observeBadges(category.id).first()
            }
        }

        for (badge in allBadges) {
            val path = badge.imageAssetPath ?: continue
            context.assets.open(path).use { /* resolves */ }
        }

        db.close()
    }

    @Test
    fun nullImageBadgesHaveCategoryFallbackColor() = runBlocking {
        val db = buildDatabase()
        CatalogSeedLoader(context, db).seedIfNeeded()

        val simidrott = db.catalogDao().findCatalogByCode("simidrott")!!
        val categories = db.catalogDao().observeCategories(simidrott.id).first()
        val nullImageBadges = categories.flatMap { category ->
            db.catalogDao().listBadgesForCategory(category.id)
                .filter { it.imageAssetPath == null }
                .map { badge -> category.code to badge }
        }

        assertTrue(nullImageBadges.isNotEmpty())

        val colors = nullImageBadges.map { (categoryCode, _) ->
            BadgePlaceholderColors.forCategoryCode(categoryCode)
        }.distinct()

        assertTrue(colors.all { it != BadgePlaceholderColors.default })
        assertTrue(colors.size > 1)
        assertNotEquals(Color.Unspecified, colors.first())

        db.close()
    }

    @Test
    fun secondSeedRunDoesNotDuplicate() = runBlocking {
        val db = buildDatabase()
        val loader = CatalogSeedLoader(context, db)
        loader.seedIfNeeded()
        val categoryCountAfterFirst = db.catalogDao().countCategories()

        loader.seedIfNeeded()

        assertEquals(2, db.catalogDao().countCatalogs())
        assertEquals(categoryCountAfterFirst, db.catalogDao().countCategories())

        db.close()
    }

    private fun buildDatabase(): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DB_NAME,
        ).build()
}
