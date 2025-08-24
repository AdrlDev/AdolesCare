package dev.adriele.adolescare.database

import android.content.Context
import android.os.Environment
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.io.File

object AppDatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            // Create external folder
            val dir = File(Environment.getExternalStorageDirectory(), "data/${context.packageName}")
            if (!dir.exists()) dir.mkdirs()

            val dbFile = File(dir, "adolescare.db")

            val instance = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                dbFile.absolutePath // raw external path
            )
                .addMigrations(MIGRATION_1_2)
                .addMigrations(MIGRATION_2_3)
                .addMigrations(MIGRATION_3_4)
                .addMigrations(MIGRATION_4_5)
                .addMigrations(MIGRATION_5_6)
                .addMigrations(MIGRATION_6_7)
                .addMigrations(MIGRATION_7_8)
                .fallbackToDestructiveMigration(false)
                .build()

            INSTANCE = instance
            instance
        }
    }

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
            CREATE TABLE IF NOT EXISTS recent_read_and_watch (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                moduleId TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                FOREIGN KEY(moduleId) REFERENCES modules(id) ON DELETE CASCADE
            )
            """.trimIndent()
            )
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE modules ADD COLUMN contentCreditsUrl TEXT")
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE modules ADD COLUMN orderBy INTEGER DEFAULT 0 NOT NULL")
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE chat_bot_conversation ADD COLUMN sources TEXT")
        }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE cycle_logs ADD COLUMN mood TEXT")
            db.execSQL("ALTER TABLE cycle_logs ADD COLUMN vaginalDischarge TEXT")
            db.execSQL("ALTER TABLE cycle_logs ADD COLUMN digestionAndStool TEXT")
            db.execSQL("ALTER TABLE cycle_logs ADD COLUMN physicalActivity TEXT")
        }
    }

    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE cycles ADD COLUMN createdAt TEXT NOT NULL DEFAULT ''")
        }
    }

    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
            CREATE TABLE IF NOT EXISTS archive_recent_read_and_watch (
                id INTEGER PRIMARY KEY NOT NULL,
                moduleId TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                FOREIGN KEY(moduleId) REFERENCES modules(id) ON DELETE CASCADE
            )
            """.trimIndent()
            )
            db.execSQL(
                """
            CREATE TABLE IF NOT EXISTS archive_reminders (
                id INTEGER PRIMARY KEY NOT NULL,
                userId TEXT NOT NULL,
                title TEXT NOT NULL,
                message TEXT NOT NULL,
                dateTime TEXT NOT NULL,
                type TEXT NOT NULL
            )
            """.trimIndent()
            )
        }
    }

}