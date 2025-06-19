package dev.adriele.adolescare.database

import android.content.Context
import android.os.Environment
import androidx.room.Room
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
                .build()

            INSTANCE = instance
            instance
        }
    }

    val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
        override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
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

}