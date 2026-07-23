package se.simmarken.domain.repository

import kotlinx.coroutines.flow.Flow
import se.simmarken.data.local.entity.KidEntity

interface KidRepository {
    fun observeAll(): Flow<List<KidEntity>>
    fun observeById(kidId: Long): Flow<KidEntity?>
    suspend fun upsert(kid: KidEntity): Long
    suspend fun delete(kidId: Long)
}
