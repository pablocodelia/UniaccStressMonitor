package com.UniaccStressMonitor.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.UniaccStressMonitor.domain.usecase.SyncPendingSessionsUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val syncPendingSessionsUseCase: SyncPendingSessionsUseCase by inject()

    override suspend fun doWork(): Result {
        val success = syncPendingSessionsUseCase()
        return if (success) {
            Result.success()
        } else {
            Result.retry()
        }
    }
}
