package com.example.segundoentregable.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.segundoentregable.data.model.User
import com.google.gson.Gson

/**
 * Simple SharedPreferences helper to store current user and list of users.
 * Uses json serialization for list of users.
 */
class SharedPrefManager private constructor(context: Context) {

    companion object {
        private const val PREFS_NAME = "segundo_ent_prefs"
        private const val KEY_USERS = "users_json"
        private const val KEY_CURRENT_EMAIL = "current_email"

        @Volatile
        private var instance: SharedPrefManager? = null

        fun getInstance(context: Context): SharedPrefManager {
            return instance ?: synchronized(this) {
                instance ?: SharedPrefManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val gson = Gson()

    fun saveUsers(users: MutableList<User>) {
        val json = gson.toJson(users)
        prefs.edit().putString(KEY_USERS, json).apply()
    }

    fun getUsers(): MutableList<User> {
        val json = prefs.getString(KEY_USERS, null) ?: return mutableListOf()
        val arr = gson.fromJson(json, Array<User>::class.java) ?: return mutableListOf()
        return arr.toMutableList()
    }

    fun setCurrentUserEmail(email: String?) {
        prefs.edit().putString(KEY_CURRENT_EMAIL, email).apply()
    }

    fun getCurrentUserEmail(): String? = prefs.getString(KEY_CURRENT_EMAIL, null)
}
