package dev.adriele.adolescare.database.repositories

import dev.adriele.adolescare.database.entities.User

interface UserRepository {
    suspend fun insertUser(user: User)

    suspend fun getUserByUsername(username: String): User?

    suspend fun updatePasswordByUsername(username: String, newPassword: String)

    suspend fun updateSexAndBarangay(sex: String, barangay: String, uid: String)
}