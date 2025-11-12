package dev.adriele.adolescare.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.adriele.adolescare.database.dao.ArchiveDao
import dev.adriele.adolescare.database.dao.ConversationDao
import dev.adriele.adolescare.database.dao.CycleDao
import dev.adriele.adolescare.database.dao.CycleLogDao
import dev.adriele.adolescare.database.dao.DailyLogDao
import dev.adriele.adolescare.database.dao.MenstrualHistoryDao
import dev.adriele.adolescare.database.dao.ModuleDao
import dev.adriele.adolescare.database.dao.RecentReadAndWatchDao
import dev.adriele.adolescare.database.dao.ReminderDao
import dev.adriele.adolescare.database.dao.UserDao
import dev.adriele.adolescare.database.entities.ArchiveRecentReadAndWatch
import dev.adriele.adolescare.database.entities.ArchiveReminder
import dev.adriele.adolescare.database.entities.Conversations
import dev.adriele.adolescare.database.entities.CycleLogEntity
import dev.adriele.adolescare.database.entities.DailyLog
import dev.adriele.adolescare.database.entities.LearningModule
import dev.adriele.adolescare.database.entities.MenstrualCycle
import dev.adriele.adolescare.database.entities.MenstrualHistoryEntity
import dev.adriele.adolescare.database.entities.RecentReadAndWatch
import dev.adriele.adolescare.database.entities.Reminder
import dev.adriele.adolescare.database.entities.User

@Database(
    entities = [
        User::class,
        MenstrualCycle::class,
        DailyLog::class,
        LearningModule::class,
        Reminder::class,
        Conversations::class,
        MenstrualHistoryEntity::class,
        CycleLogEntity::class,
        RecentReadAndWatch::class,
        ArchiveRecentReadAndWatch::class,
        ArchiveReminder::class
    ],
    version = 10,
    exportSchema = false
)

@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun cycleDao(): CycleDao
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun moduleDao(): ModuleDao
    abstract fun reminderDao(): ReminderDao
    abstract fun conversationDao(): ConversationDao
    abstract fun menstrualHistoryDao(): MenstrualHistoryDao
    abstract fun cycleLogDao(): CycleLogDao
    abstract fun recentReadAndWatchDao(): RecentReadAndWatchDao
    abstract fun archiveDao(): ArchiveDao
}