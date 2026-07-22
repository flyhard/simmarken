package se.simmarken.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import se.simmarken.data.local.entity.RequirementProgressEntity

@Dao
interface RequirementProgressDao {
    @Query("SELECT * FROM requirement_progress WHERE kidId = :kidId")
    fun observeForKid(kidId: Long): Flow<List<RequirementProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: RequirementProgressEntity)
}
