package dev.adriele.adolescare.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val userId: String,
    val username: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val middleInitial: String?,
    val birthday: String,
    val age: Int,
    val sex: String? = null,
    val barangay: String? = null
)
