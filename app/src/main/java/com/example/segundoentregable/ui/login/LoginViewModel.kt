package com.example.segundoentregable.ui.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.segundoentregable.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

// CAMBIO: Hereda de ViewModel y recibe el Repo
class LoginViewModel(
    private val repo: UserRepository
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
                // Usamos el repo inyectado
                val ok = withContext(Dispatchers.IO) {
                    repo.login(state.email.trim(), state.password)
                }

                if (ok) {
                    withContext(Dispatchers.IO) {
                        repo.setCurrentUser(state.email.trim())
                    }
                    _loginSuccessEvent.emit(Unit)
                } else {
                    _uiState.update {
                        it.copy(errorMessage = "Credenciales incorrectas")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Error al iniciar sesión: ${e.message}")
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}