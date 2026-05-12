package com.UniaccStressMonitor.domain.repository

import com.UniaccStressMonitor.domain.model.StressLevel
import com.UniaccStressMonitor.domain.model.StressSession
import kotlinx.coroutines.flow.Flow

interface IStressRepository {
    fun getRecentSessions(userId: String, limit: Int): Flow<List<StressSession>>
    fun getLastSession(userId: String): Flow<StressSession?>
    fun getTotalSessionsCount(userId: String): Flow<Int>
    suspend fun getCountByLevel(userId: String, level: StressLevel): Int
    suspend fun saveSession(session: StressSession)
    suspend fun getUnsyncedSessions(): List<StressSession>
    suspend fun markAsSynced(sessionIds: List<String>)
    suspend fun uploadSessions(sessions: List<StressSession>): Boolean
}
