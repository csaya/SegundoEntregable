package com.example.segundoentregable.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Servicio de autenticación con Firebase.
 * Se integra con el sistema local existente (Room + SharedPrefs).
 *
 * Estrategia "Lazy Registration":
 * - El usuario puede explorar sin cuenta
 * - Al registrarse/login, se sincroniza con Firebase
 * - Room sigue siendo la fuente de verdad local
 */
class FirebaseAuthService {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Usuario actual de Firebase (null si no está autenticado)
     */
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    /**
     * Flow que emite cambios en el estado de autenticación
     */
    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /**
     * Registrar nuevo usuario con email y contraseña
     */
    suspend fun register(email: String, password: String, displayName: String): Result<FirebaseUser> {
        return try {
            // 1. Crear cuenta
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user

            if (user != null) {
                // 2. ✅ Actualizar perfil con displayName
                val profileUpdates = userProfileChangeRequest {
                    this.displayName = displayName
                }
                user.updateProfile(profileUpdates).await()

                Result.success(user)
            } else {
                Result.failure(Exception("No se pudo crear el usuario"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Iniciar sesión con email y contraseña
     */
    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("No se pudo iniciar sesión"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cerrar sesión
     */
    fun logout() {
        auth.signOut()
    }

    /**
     * Verificar si hay un usuario autenticado en Firebase
     */
    fun isAuthenticated(): Boolean = currentUser != null

    /**
     * Obtener email del usuario actual
     */
    fun getCurrentUserEmail(): String? = currentUser?.email

    /**
     * Obtener displayName del usuario actual
     */
    fun getCurrentUserDisplayName(): String? = currentUser?.displayName

    /**
     * Enviar email de recuperación de contraseña
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualizar displayName de un usuario ya existente
     */
    suspend fun updateDisplayName(newDisplayName: String): Result<Unit> {
        return try {
            val user = currentUser ?: return Result.failure(Exception("No hay usuario logueado"))

            val profileUpdates = userProfileChangeRequest {
                displayName = newDisplayName
            }
            user.updateProfile(profileUpdates).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
