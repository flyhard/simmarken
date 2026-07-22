package se.simmarken.di

import android.content.Context
import androidx.room.Room
import se.simmarken.data.local.AppDatabase
import se.simmarken.domain.repository.CatalogRepository
import se.simmarken.domain.repository.CatalogRepositoryImpl
import se.simmarken.domain.repository.KidRepository
import se.simmarken.domain.repository.KidRepositoryImpl
import se.simmarken.domain.repository.ProgressRepository
import se.simmarken.domain.repository.ProgressRepositoryImpl

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
