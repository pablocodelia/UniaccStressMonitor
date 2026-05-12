package com.UniaccStressMonitor.domain.model

import java.util.UUID

data class StressSession(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val stressLevel: StressLevel,
    val durationSeconds: Int = 30,
    val isSynced: Boolean = false
)
