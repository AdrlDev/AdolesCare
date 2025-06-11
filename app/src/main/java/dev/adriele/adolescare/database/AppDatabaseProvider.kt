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
                    ).build()

            INSTANCE = instance
            instance
        }
    }
}