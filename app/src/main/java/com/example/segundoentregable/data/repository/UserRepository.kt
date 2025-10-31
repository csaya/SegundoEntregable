package com.example.segundoentregable.data.repository

import android.content.Context
import com.example.segundoentregable.data.local.SharedPrefManager
import com.example.segundoentregable.data.model.User

class UserRepository(context: Context) {

    private val prefs = SharedPrefManager.getInstance(context)

    fun register(user: User): Boolean {
        val users = prefs.getUsers()
        if (users.any { it.email.equals(user.email, ignoreCase = true) }) return false
        users.add(user)
        prefs.saveUsers(users)
        return true
    }

    fun login(email: String, password: String): Boolean {
        val users = prefs.getUsers()
        return users.any { it.email.equals(email, ignoreCase = true) && it.password == password }
    }

    fun getUser(email: String): User? {
        return prefs.getUsers().find { it.email.equals(email, ignoreCase = true) }
    }

    fun setCurrentUser(email: String?) {
        prefs.setCurrentUserEmail(email)
    }

    fun getCurrentUserEmail(): String? = prefs.getCurrentUserEmail()

    fun isUserLoggedIn(): Boolean {
        // Comprueba si ya hay un email guardado
        return (getCurrentUserEmail() != null)
    }

    fun logout() {
        // setCurrentUser(null) ya existe, esto es solo un alias
        setCurrentUser(null)
    }
}
