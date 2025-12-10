package com.example.segundoentregable.ui.login

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.segundoentregable.data.firebase.FirebaseAuthService
import com.example.segundoentregable.data.repository.UserRepository
import com.example.segundoentregable.utils.NetworkConnectivityObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "LoginViewModel"

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel para autenticación.
 * Prioriza Firebase Auth cuando hay conexión, fallback a Room offline.
 */
class LoginViewModel(
    private val repo: UserRepository,
    private val firebaseAuth: FirebaseAuthService,
    private val connectivityObserver: NetworkConnectivityObserver
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _loginSuccessEvent = MutableSharedFlow<Unit>()
    val loginSuccessEvent = _loginSuccessEvent.asSharedFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onLoginClicked() {
        val state = _uiState.value

        val errorMessage = when {
            state.email.isBlank() -> "El correo es obligatorio"
            !Patterns.EMAIL_ADDRESS.matcher(state.email).matches() -> "Formato de correo inválido"
            state.password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }

        if (errorMessage != null) {
            _uiState.update { it.copy(errorMessage = errorMessage) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val email = state.email.trim()
                val password = state.password
                
                // Si hay conexión, usar Firebase Auth
                if (connectivityObserver.isCurrentlyConnected()) {
                    Log.d(TAG, "Intentando login con Firebase Auth")
                    val result = firebaseAuth.login(email, password)
                    
                    result.onSuccess { firebaseUser ->
                        Log.d(TAG, "Login Firebase exitoso: ${firebaseUser.email}")
                        // Asegurar que usuario exista en Room para FK constraints
                        withContext(Dispatchers.IO) {
                            repo.ensureUserExistsInRoom(
                                email = email,
                                name = firebaseUser.displayName ?: "Usuario"
                            )
                        }
                        // Guardar sesión local
                        repo.setCurrentUser(email)
                        _loginSuccessEvent.emit(Unit)
                    }.onFailure { error ->
                        Log.w(TAG, "Firebase login falló, intentando offline: ${error.message}")
                        // Fallback a validación local
                        val localOk = withContext(Dispatchers.IO) {
                            repo.login(email, password)
                        }
                        if (localOk) {
                            repo.setCurrentUser(email)
                            _loginSuccessEvent.emit(Unit)
                        } else {
                            _uiState.update {
                                it.copy(errorMessage = translateFirebaseError(error))
                            }
                        }
                    }
                } else {
                    // Sin conexión: validar contra Room
                    Log.d(TAG, "Sin conexión, validando offline")
                    val ok = withContext(Dispatchers.IO) {
                        repo.login(email, password)
                    }
                    if (ok) {
                        repo.setCurrentUser(email)
                        _loginSuccessEvent.emit(Unit)
                    } else {
                        _uiState.update {
                            it.copy(errorMessage = "Credenciales incorrectas")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en login", e)
                _uiState.update {
                    it.copy(errorMessage = "Error al iniciar sesión: ${e.message}")
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    
    /**
     * Traduce errores de Firebase a mensajes amigables en español
     */
    private fun translateFirebaseError(error: Throwable): String {
        val message = error.message ?: return "Error desconocido"
        return when {
            message.contains("no user record", ignoreCase = true) -> 
                "No existe una cuenta con este correo"
            message.contains("wrong-password", ignoreCase = true) ||
            message.contains("invalid-credential", ignoreCase = true) -> 
                "Contraseña incorrecta"
            message.contains("too-many-requests", ignoreCase = true) -> 
                "Demasiados intentos. Intenta más tarde"
            message.contains("network", ignoreCase = true) -> 
                "Error de conexión. Verifica tu internet"
            message.contains("user-disabled", ignoreCase = true) -> 
                "Esta cuenta ha sido deshabilitada"
            else -> "Error: $message"
        }
    }
}