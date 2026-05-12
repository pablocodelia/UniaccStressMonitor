package com.UniaccStressMonitor.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.UniaccStressMonitor.data.remote.AuthService
import com.UniaccStressMonitor.domain.model.StressLevel
import com.UniaccStressMonitor.domain.model.StressSession
import com.UniaccStressMonitor.domain.repository.IStressRepository
import com.UniaccStressMonitor.domain.usecase.SyncPendingSessionsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class DashboardState(
    val lastSession: StressSession? = null,
    val totalSessions: Int = 0,
    val highStressCount: Int = 0,
    val mediumStressCount: Int = 0,
    val lowStressCount: Int = 0
)

class StressViewModel(
    private val repository: IStressRepository,
    private val authService: AuthService
) : ViewModel(), KoinComponent {

    private val currentUserId = MutableStateFlow(authService.getCurrentUserId() ?: "")

    val recentSessions: StateFlow<List<StressSession>> = currentUserId
        .flatMapLatest { userId ->
            repository.getRecentSessions(userId, 10)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val lastSession: StateFlow<StressSession?> = currentUserId
        .flatMapLatest { userId ->
            repository.getLastSession(userId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val totalSessions: StateFlow<Int> = currentUserId
        .flatMapLatest { userId ->
            repository.getTotalSessionsCount(userId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private val _dashboardStats = MutableStateFlow(DashboardState())
    val dashboardStats: StateFlow<DashboardState> = _dashboardStats

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    private val syncPendingSessionsUseCase: SyncPendingSessionsUseCase by inject()

    init {
        refreshDashboardStats()
    }

    fun refreshUser() {
        currentUserId.value = authService.getCurrentUserId() ?: ""
        refreshDashboardStats()
    }

    fun refreshDashboardStats() {
        val userId = currentUserId.value
        if (userId.isEmpty()) return

        viewModelScope.launch {
            val high = repository.getCountByLevel(userId, StressLevel.HIGH_STRESS)
            val medium = repository.getCountByLevel(userId, StressLevel.MEDIUM_STRESS)
            val low = repository.getCountByLevel(userId, StressLevel.LOW_STRESS)
            
            _dashboardStats.update { 
                it.copy(
                    highStressCount = high,
                    mediumStressCount = medium,
                    lowStressCount = low
                )
            }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _isSyncing.value = true
            syncPendingSessionsUseCase()
            _isSyncing.value = false
            refreshDashboardStats() // Refrescar para ver si el estado de sync cambió (aunque Room lo hace automático usualmente)
        }
    }
}
