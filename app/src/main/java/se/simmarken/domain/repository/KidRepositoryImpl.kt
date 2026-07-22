package se.simmarken.domain.repository

import se.simmarken.data.local.dao.KidDao
import se.simmarken.data.local.entity.KidEntity

class KidRepositoryImpl(
    private val kidDao: KidDao,
) : KidRepository {
    override fun observeAll() = kidDao.observeAll()
    override suspend fun upsert(kid: KidEntity) = kidDao.upsert(kid)
}
