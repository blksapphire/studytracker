package com.mayowa.studytracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mayowa.studytracker.data.local.dao.DailyStatsDao
import com.mayowa.studytracker.data.local.dao.SessionDao
import com.mayowa.studytracker.data.local.entity.DailyStatsEntity
import com.mayowa.studytracker.data.local.entity.SessionEntity

@Database(
    entities = [SessionEntity::class, DailyStatsEntity::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun dailyStatsDao(): DailyStatsDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sessions ADD COLUMN distractionCount INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
