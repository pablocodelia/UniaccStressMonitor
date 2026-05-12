package com.UniaccStressMonitor.di

import androidx.room.Room
import com.UniaccStressMonitor.data.local.StressDatabase
import com.UniaccStressMonitor.data.remote.AuthService
import com.UniaccStressMonitor.data.remote.FirestoreService
import com.UniaccStressMonitor.data.repository.StressRepository
import com.UniaccStressMonitor.domain.repository.IStressRepository
import com.UniaccStressMonitor.domain.usecase.DetectStressUseCase
import com.UniaccStressMonitor.domain.usecase.SaveStressSessionUseCase
import com.UniaccStressMonitor.domain.usecase.SyncPendingSessionsUseCase
import com.UniaccStressMonitor.presentation.ui.StressViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Local Data
    single {
        Room.databaseBuilder(
            androidContext(),
            StressDatabase::class.java,
            "stress_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    single { get<StressDatabase>().stressDao() }

    // Remote Data
    single { FirestoreService() }
    single { AuthService() }

    // Repository
    single<IStressRepository> { StressRepository(get(), get()) }

    // Use Cases
    factory { DetectStressUseCase() }
    factory { SaveStressSessionUseCase(get()) }
    factory { SyncPendingSessionsUseCase(get()) }

    // ViewModel
    viewModel { StressViewModel(get(), get()) }
}
