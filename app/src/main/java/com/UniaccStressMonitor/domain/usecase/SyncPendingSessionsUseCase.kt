package com.UniaccStressMonitor.domain.usecase

import com.UniaccStressMonitor.domain.repository.IStressRepository

class SyncPendingSessionsUseCase(private val repository: IStressRepository) {
    suspend operator fun invoke(): Boolean {
        val unsynced = repository.getUnsyncedSessions()
        if (unsynced.isEmpty()) return true

        // Upload in batches of 50
        val batches = unsynced.chunked(50)
        var allSuccess = true

        for (batch in batches) {
            val success = repository.uploadSessions(batch)
            if (success) {
                repository.markAsSynced(batch.map { it.id })
            } else {
                allSuccess = false
            }
        }
        return allSuccess
    }
}
