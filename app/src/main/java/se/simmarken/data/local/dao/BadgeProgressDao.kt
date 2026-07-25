package se.simmarken.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import se.simmarken.data.local.entity.BadgeProgressEntity

@Dao
interface BadgeProgressDao {
    @Query("SELECT * FROM badge_progress WHERE kidId = :kidId")
    fun observeForKid(kidId: Long): Flow<List<BadgeProgressEntity>>

    @Query("SELECT * FROM badge_progress WHERE kidId IN (:kidIds)")
    suspend fun findForKids(kidIds: List<Long>): List<BadgeProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: BadgeProgressEntity)
}
