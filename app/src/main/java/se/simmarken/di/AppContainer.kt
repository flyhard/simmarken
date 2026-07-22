package se.simmarken.di

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import se.simmarken.data.local.AppDatabase
import se.simmarken.data.seed.CatalogSeedLoader
import se.simmarken.domain.repository.CatalogRepository
import se.simmarken.domain.repository.CatalogRepositoryImpl
import se.simmarken.domain.repository.KidRepository
import se.simmarken.domain.repository.KidRepositoryImpl
import se.simmarken.domain.repository.ProgressRepository
import se.simmarken.domain.repository.ProgressRepositoryImpl

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database: AppDatabase = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        AppDatabase.DB_NAME,
    ).build()

    init {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            CatalogSeedLoader(appContext, database).seedIfNeeded()
        }
    }

    val kidRepository: KidRepository = KidRepositoryImpl(database.kidDao())
    val catalogRepository: CatalogRepository = CatalogRepositoryImpl(database.catalogDao())
    val progressRepository: ProgressRepository = ProgressRepositoryImpl(
        requirementProgressDao = database.requirementProgressDao(),
        badgeProgressDao = database.badgeProgressDao(),
    )
}
