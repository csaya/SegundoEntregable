package com.example.segundoentregable.ui.login

import android.app.Application
import android.util.Patterns // Para la validación de email
import androidx.lifecycle.AndroidViewModel
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

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = UserRepository(application.applicationContext)

    // 2. Flujo de Estado (StateFlow) para la UI
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // 3. Flujo de Eventos (SharedFlow) para acciones únicas (como navegar)
    private val _loginSuccessEvent = MutableSharedFlow<Unit>()
    val loginSuccessEvent = _loginSuccessEvent.asSharedFlow()

    // 4. Funciones de Eventos: La Vista llama a estas

    fun onEmailChanged(email: String) {
        _uiState.update { currentState ->
            currentState.copy(email = email, errorMessage = null) // Limpia el error al escribir
        }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { currentState ->
            currentState.copy(password = password, errorMessage = null) // Limpia el error al escribir
        }
    }

    fun onLoginClicked() {
        val state = _uiState.value // Obtiene el estado actual

        // 5. Validación (Movida desde la Vista)
        val errorMessage = when {
            state.email.isBlank() -> "El correo es obligatorio"
            !Patterns.EMAIL_ADDRESS.matcher(state.email).matches() -> "Formato de correo inválido"
            state.password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }

        if (errorMessage != null) {
            _uiState.update { it.copy(errorMessage = errorMessage) }
            return // Detiene la ejecución si hay un error
        }

        // 6. Lógica de Login (Movida desde el antiguo UserViewModel)
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val ok = withContext(Dispatchers.IO) {
                repo.login(state.email.trim(), state.password)
            }

            if (ok) {
                // Guarda la sesión
                withContext(Dispatchers.IO) {
                    repo.setCurrentUser(state.email.trim())
                }
                // Emite el evento de éxito para que la Vista navegue
                _loginSuccessEvent.emit(Unit)
            } else {
                _uiState.update {
                    it.copy(errorMessage = "Credenciales incorrectas")
                }
            }

            // Siempre detiene la carga al final
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}