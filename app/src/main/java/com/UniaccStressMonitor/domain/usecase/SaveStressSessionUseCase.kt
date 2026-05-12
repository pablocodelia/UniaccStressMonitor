package com.UniaccStressMonitor.domain.usecase

import com.UniaccStressMonitor.domain.model.StressSession
import com.UniaccStressMonitor.domain.repository.IStressRepository

class SaveStressSessionUseCase(private val repository: IStressRepository) {
    suspend operator fun invoke(session: StressSession) {
        repository.saveSession(session)
    }
}
