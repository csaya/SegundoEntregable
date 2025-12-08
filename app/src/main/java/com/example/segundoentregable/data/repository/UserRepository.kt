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
        
        // Intentar sincronizar con Firebase si hay conexión
        if (connectivityObserver.isCurrentlyConnected()) {
            val result = firebaseAuth.register(user.email, user.password)
            // No fallar si Firebase falla - Room es la fuente de verdad
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
        // Validar contra Room primero
        val user = userDao.getUserByEmail(email)
        val localValid = user != null && user.password == password
        
        if (!localValid) return false
        
        // Si hay conexión, sincronizar con Firebase
        if (connectivityObserver.isCurrentlyConnected()) {
            val result = firebaseAuth.login(email, password)
            // Si Firebase falla pero Room validó, permitir acceso
            // (el usuario puede estar offline o no sincronizado)
        }
        
        return true
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