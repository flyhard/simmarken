package se.simmarken.data.seed

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import se.simmarken.data.local.AppDatabase

@RunWith(AndroidJUnit4::class)
class CatalogSeedLoaderSmokeTest {
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(AppDatabase.DB_NAME)
    }

    @Test
    fun seedIfNeededPopulatesBothCatalogs() = runBlocking {
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DB_NAME,
        ).build()

        val loader = CatalogSeedLoader(context, db)
        loader.seedIfNeeded()

        assertNotNull(db.catalogDao().findCatalogByCode("simidrott"))
        assertNotNull(db.catalogDao().findCatalogByCode("sls"))

        db.close()
    }
}
