package dev.adriele.adolescare.database.repositories.implementation

import dev.adriele.adolescare.database.dao.UserDao
import dev.adriele.adolescare.database.entities.User
import dev.adriele.adolescare.database.repositories.UserRepository

class UserRepositoryImpl(private val userDao: UserDao) : UserRepository {
    override suspend fun insertUser(user: User) {
        userDao.insert(user = user)
    }

    override suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username = username)
    }

    override suspend fun updatePasswordByUsername(
        username: String,
        newPassword: String
    ) {
        userDao.updatePasswordByUsername(username = username, newPassword = newPassword)
    }

    override suspend fun updateSexAndBarangay(
        sex: String,
        barangay: String,
        uid: String
    ) {
        userDao.updateSexAndBarangay(sex = sex, barangay = barangay, uid = uid)
    }

    override suspend fun isUsernameTaken(username: String): Boolean {
        return userDao.getUserByUsername(username) != null
    }

}