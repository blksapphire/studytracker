package com.mayowa.studytracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mayowa.studytracker.data.local.dao.DailyStatsDao
import com.mayowa.studytracker.data.local.dao.SessionDao
import com.mayowa.studytracker.data.local.entity.DailyStatsEntity
import com.mayowa.studytracker.data.local.entity.SessionEntity

@Database(
    entities = [SessionEntity::class, DailyStatsEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun dailyStatsDao(): DailyStatsDao
}
