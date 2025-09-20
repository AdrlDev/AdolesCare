package dev.adriele.adolescare.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.adriele.adolescare.database.entities.User
import dev.adriele.adolescare.database.repositories.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepository) : ViewModel() {
    private val _insertStatus = MutableLiveData<Pair<Boolean, String>>()
    val insertStatus: LiveData<Pair<Boolean, String>> = _insertStatus

    private val _updatePasswordStatus = MutableLiveData<Boolean>()
    val updatePasswordStatus: LiveData<Boolean> = _updatePasswordStatus

    private val _updateSexBarangay = MutableLiveData<Pair<Boolean, String>>()
    val updateSexBarangay: LiveData<Pair<Boolean, String>> = _updateSexBarangay

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    fun insertUser(user: User) {
        viewModelScope.launch {
            try {
                repository.insertUser(user)
                _insertStatus.value = Pair(true, user.userId)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error inserting user", e)
                _insertStatus.value = Pair(false, user.userId)
            }
        }
    }

    fun getUserByUsername(username: String) {
        viewModelScope.launch {
            try {
                _user.value = repository.getUserByUsername(username)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error getting user", e)
                _user.value = null
            }
        }
    }

    fun getUserByUID(uid: String) {
        viewModelScope.launch {
            try {
                _user.value = repository.getUserByUID(uid = uid)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error getting user", e)
                _user.value = null
            }
        }
    }

    suspend fun getUserNameById(userId: String): User? {
        return repository.getUserByUID(userId)
    }

    fun updatePasswordByUsername(username: String, newPassword: String) {
        viewModelScope.launch {
            try {
                repository.updatePasswordByUsername(username, newPassword)
                _updatePasswordStatus.value = true
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error updating password", e)
                _updatePasswordStatus.value = false
            }
        }
    }

    fun updateSexAndBarangay(sex: String, barangay: String, uid: String) {
        viewModelScope.launch {
            try {
                repository.updateSexAndBarangay(sex = sex, barangay = barangay, uid = uid)
                _updateSexBarangay.value = Pair(true, sex)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error updating user", e)
                _updateSexBarangay.value = Pair(false, sex)
            }
        }
    }

    suspend fun isUsernameTaken(username: String): Boolean {
        return repository.isUsernameTaken(username)
    }
}