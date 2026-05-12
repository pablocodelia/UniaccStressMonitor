package com.UniaccStressMonitor.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.UniaccStressMonitor.domain.model.StressLevel
import com.UniaccStressMonitor.domain.model.StressSession

@Entity(tableName = "StressSession")
data class StressEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val timestamp: Long,
    val stressLevel: Int,
    val durationSeconds: Int,
    val isSynced: Boolean
) {
    fun toDomain() = StressSession(
        id = id,
        userId = userId,
        timestamp = timestamp,
        stressLevel = StressLevel.fromInt(stressLevel),
        durationSeconds = durationSeconds,
        isSynced = isSynced
    )

    companion object {
        fun fromDomain(session: StressSession) = StressEntity(
            id = session.id,
            userId = session.userId,
            timestamp = session.timestamp,
            stressLevel = session.stressLevel.value,
            durationSeconds = session.durationSeconds,
            isSynced = session.isSynced
        )
    }
}
