package com.example.segundoentregable.data.repository

import android.content.Context
import com.example.segundoentregable.data.local.AppDatabase
import com.example.segundoentregable.data.local.SharedPrefManager
import com.example.segundoentregable.data.local.entity.UserEntity
import com.example.segundoentregable.data.model.User

class UserRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val userDao = db.userDao()
    private val prefs = SharedPrefManager.getInstance(context)

    suspend fun register(user: User): Boolean {
        val existingUser = userDao.getUserByEmail(user.email)
        if (existingUser != null) return false
        userDao.insertUser(UserEntity(user.email, user.name, user.password))
        return true
    }

    suspend fun login(email: String, password: String): Boolean {
        val user = userDao.getUserByEmail(email)
        return user != null && user.password == password
    }

    suspend fun getUser(email: String): User? {
        val userEntity = userDao.getUserByEmail(email)
        return userEntity?.let { User(it.name, it.email, it.password) }
    }

    fun setCurrentUser(email: String?) {
        prefs.setCurrentUserEmail(email)
    }

    fun getCurrentUserEmail(): String? = prefs.getCurrentUserEmail()

    fun isUserLoggedIn(): Boolean {
        return (getCurrentUserEmail() != null)
    }

    fun logout() {
        setCurrentUser(null)
    }
}
