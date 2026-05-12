package com.UniaccStressMonitor.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [StressEntity::class], version = 2)
abstract class StressDatabase : RoomDatabase() {
    abstract fun stressDao(): StressDao
}
