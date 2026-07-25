package se.simmarken.domain.repository

import kotlinx.coroutines.flow.Flow
import se.simmarken.data.local.entity.KidEntity

interface KidRepository {
    fun observeAll(): Flow<List<KidEntity>>
    fun observeById(kidId: Long): Flow<KidEntity?>
    suspend fun findAll(): List<KidEntity>
    suspend fun findByStableId(stableId: String): KidEntity?
    suspend fun findByIds(ids: List<Long>): List<KidEntity>
    suspend fun upsert(kid: KidEntity): Long
    suspend fun delete(kidId: Long)
}
