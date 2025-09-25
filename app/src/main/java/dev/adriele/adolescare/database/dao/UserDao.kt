package dev.adriele.adolescare.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.adriele.adolescare.database.entities.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Query("SELECT * FROM users WHERE userId = :id")
    suspend fun getUser(id: String): User?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE userId = :uid LIMIT 1")
    suspend fun getUserByUID(uid: String): User?

    @Query("UPDATE users SET password = :newPassword WHERE username = :username")
    suspend fun updatePasswordByUsername(username: String, newPassword: String)

    @Query("UPDATE users SET sex = :sex, barangay = :barangay WHERE userId = :uid")
    suspend fun updateSexAndBarangay(sex: String, barangay: String, uid: String)

    @Query("UPDATE users SET username = :username, birthday = :birthday, age = :age WHERE userId = :uid")
    suspend fun updateUser(username: String, birthday: String, age: Int, uid: String)
}