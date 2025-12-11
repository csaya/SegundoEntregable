package com.example.segundoentregable.data.repository

import android.content.Context
import com.example.segundoentregable.data.firebase.FirebaseAuthService
import com.example.segundoentregable.data.local.SharedPrefManager
import com.example.segundoentregable.data.local.dao.UserDao
import com.example.segundoentregable.data.local.entity.UserEntity
import com.example.segundoentregable.data.model.User
import com.example.segundoentregable.utils.NetworkConnectivityObserver

/**
 * Repositorio de usuarios con modelo híbrido:
 * - Room: Fuente de verdad local (offline-first)
 * - Firebase Auth: Respaldo en la nube cuando hay conexión
 * - SharedPrefs: Estado de sesión local
 */
class UserRepository(
    private val userDao: UserDao,
    context: Context
) {
    private val prefs = SharedPrefManager.getInstance(context)
    private val firebaseAuth = FirebaseAuthService()
    private val connectivityObserver = NetworkConnectivityObserver(context)

    /**
     * Registrar usuario:
     * 1. Guardar en Room (siempre)
     * 2. Si hay conexión, sincronizar con Firebase
     */
    suspend fun register(user: User): Boolean {
        // Verificar si ya existe localmente
        val existingUser = userDao.getUserByEmail(user.email)
        if (existingUser != null) return false

        // Guardar en Room
        userDao.insertUser(UserEntity(user.email, user.name, user.password))

        if (connectivityObserver.isCurrentlyConnected()) {
            val result = firebaseAuth.register(user.email, user.password, user.name) // ✅ Pasar nombre
            result.onFailure {
                // Log silencioso, el usuario está registrado localmente
            }
        }

        return true
    }

    /**
     * Login:
     * 1. Validar contra Room (offline-first)
     * 2. Si hay conexión, sincronizar sesión con Firebase
     */
    suspend fun login(email: String, password: String): Boolean {
        val user = userDao.getUserByEmail(email)
        val localValid = user != null && user.password == password

        if (!localValid) return false

        if (connectivityObserver.isCurrentlyConnected()) {
            val result = firebaseAuth.login(email, password)

            result.onSuccess { firebaseUser ->
                val firebaseName = firebaseUser.displayName
                val roomName = user?.name

                if (!firebaseName.isNullOrBlank()) {
                    userDao.insertUser(
                        UserEntity(email, firebaseName, user?.password ?: "")
                    )
                } else if (!roomName.isNullOrBlank()) {
                    firebaseAuth.updateDisplayName(roomName)
                }
            }
        }
        return true
    }


    suspend fun getUser(email: String): User? {
        val userEntity = userDao.getUserByEmail(email)
        return userEntity?.let { User(it.name, it.email, it.password) }
    }

    fun setCurrentUser(email: String?) {
        prefs.setCurrentUserEmail(email)
        if (email != null) {
            // Aquí puedes hacer algo si necesitas, pero no es necesario para Firebase Auth
        } else {
            firebaseAuth.logout()
        }
    }


    fun getCurrentUserEmail(): String? = prefs.getCurrentUserEmail()

    /**
     * Asegurar que el usuario exista en Room (crear si no existe).
     * Usado después de login con Firebase para mantener consistencia.
     */
    suspend fun ensureUserExistsInRoom(email: String, name: String = "Usuario") {
        val existingUser = userDao.getUserByEmail(email)
        if (existingUser == null) {
            // Crear usuario local sin contraseña (login solo via Firebase)
            userDao.insertUser(UserEntity(email, name, ""))
        } else if (existingUser.name != name && name != "Usuario") {
            // Actualizar nombre si cambió
            userDao.insertUser(UserEntity(email, name, existingUser.password))
        }
    }

    fun isUserLoggedIn(): Boolean {
        return (getCurrentUserEmail() != null)
    }

    /**
     * Logout: Cerrar sesión local y en Firebase
     */
    fun logout() {
        setCurrentUser(null)
        firebaseAuth.logout()
    }
    
    /**
     * Verificar si el usuario está autenticado en Firebase
     */
    fun isFirebaseAuthenticated(): Boolean = firebaseAuth.isAuthenticated()
    
    /**
     * Obtener el servicio de Firebase Auth para operaciones avanzadas
     */
    fun getFirebaseAuthService(): FirebaseAuthService = firebaseAuth
}