package se.simmarken.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import se.simmarken.data.local.entity.KidEntity

@Dao
interface KidDao {
    @Query("SELECT * FROM kids ORDER BY sortOrder ASC, createdAtEpochMillis ASC")
    fun observeAll(): Flow<List<KidEntity>>

    @Query("SELECT * FROM kids WHERE id = :kidId")
    fun observeById(kidId: Long): Flow<KidEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(kid: KidEntity): Long

    @Query("DELETE FROM kids WHERE id = :kidId")
    suspend fun deleteById(kidId: Long)
}
