package com.example.segundoentregable.ui.register

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.segundoentregable.data.model.User
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

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = UserRepository(application.applicationContext)
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()
    private val _registerSuccessEvent = MutableSharedFlow<Unit>()
    val registerSuccessEvent = _registerSuccessEvent.asSharedFlow()

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name, errorMessage = null) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onRegisterClicked() {
        val state = _uiState.value

        val errorMessage = when {
            state.name.isBlank() -> "El nombre es obligatorio"
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
                val ok = withContext(Dispatchers.IO) {
                    repo.register(User(state.name.trim(), state.email.trim(), state.password))
                }

                if (ok) {
                    _registerSuccessEvent.emit(Unit)
                } else {
                    _uiState.update { it.copy(errorMessage = "Correo ya registrado") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error al registrar: ${e.message}") }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }
}