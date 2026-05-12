package com.UniaccStressMonitor.data.repository

import com.UniaccStressMonitor.data.local.StressDao
import com.UniaccStressMonitor.data.local.StressEntity
import com.UniaccStressMonitor.data.remote.FirestoreService
import com.UniaccStressMonitor.domain.model.StressLevel
import com.UniaccStressMonitor.domain.model.StressSession
import com.UniaccStressMonitor.domain.repository.IStressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StressRepository(
    private val dao: StressDao,
    private val remote: FirestoreService
) : IStressRepository {

    override fun getRecentSessions(userId: String, limit: Int): Flow<List<StressSession>> {
        return dao.getRecentSessions(userId, limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getLastSession(userId: String): Flow<StressSession?> {
        return dao.getLastSession(userId).map { it?.toDomain() }
    }

    override fun getTotalSessionsCount(userId: String): Flow<Int> {
        return dao.getTotalCount(userId)
    }

    override suspend fun getCountByLevel(userId: String, level: StressLevel): Int {
        return dao.getCountByLevel(userId, level.value)
    }

    override suspend fun saveSession(session: StressSession) {
        dao.insert(StressEntity.fromDomain(session))
        val count = dao.getCount(session.userId)
        if (count > 10000) {
            dao.deleteOldest(session.userId, count - 10000)
        }
    }

    override suspend fun getUnsyncedSessions(): List<StressSession> {
        return dao.getUnsyncedSessions().map { it.toDomain() }
    }

    override suspend fun markAsSynced(sessionIds: List<String>) {
        dao.markAsSynced(sessionIds)
    }

    override suspend fun uploadSessions(sessions: List<StressSession>): Boolean {
        return remote.uploadSessions(sessions)
    }
}
