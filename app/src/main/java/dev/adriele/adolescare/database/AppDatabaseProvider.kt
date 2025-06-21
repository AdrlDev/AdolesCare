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
                .build()

            INSTANCE = instance
            instance
        }
    }

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
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
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE modules ADD COLUMN contentCreditsUrl TEXT")
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE modules ADD COLUMN orderBy INTEGER DEFAULT 0 NOT NULL")
        }
    }

}