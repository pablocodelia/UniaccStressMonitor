package com.UniaccStressMonitor.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StressDao {
    @Query("SELECT * FROM StressSession WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSessions(userId: String, limit: Int): Flow<List<StressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: StressEntity)

    @Query("SELECT * FROM StressSession WHERE isSynced = 0")
    suspend fun getUnsyncedSessions(): List<StressEntity>

    @Query("UPDATE StressSession SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)

    @Query("SELECT COUNT(*) FROM StressSession WHERE userId = :userId")
    suspend fun getCount(userId: String): Int

    @Query("DELETE FROM StressSession WHERE userId = :userId AND id IN (SELECT id FROM StressSession WHERE userId = :userId ORDER BY timestamp ASC LIMIT :count)")
    suspend fun deleteOldest(userId: String, count: Int)

    @Query("SELECT * FROM StressSession WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
    fun getLastSession(userId: String): Flow<StressEntity?>

    @Query("SELECT COUNT(*) FROM StressSession WHERE userId = :userId AND stressLevel = :level")
    suspend fun getCountByLevel(userId: String, level: Int): Int

    @Query("SELECT COUNT(*) FROM StressSession WHERE userId = :userId")
    fun getTotalCount(userId: String): Flow<Int>
}
