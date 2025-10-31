package com.example.segundoentregable.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.segundoentregable.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ProfileUiState(
    // 1. Empezamos con valores por defecto o de "carga"
    val userName: String = "Cargando...",
    val userEmail: String = "",
    val downloadedGuides: Int = 3,
    val notificationsEnabled: Boolean = false
)

// 2. Convertimos el ViewModel en AndroidViewModel para tener el 'context'
class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    // 3. Obtenemos la instancia de nuestro UserRepository
    private val repo = UserRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // 4. Usamos un bloque 'init' para cargar los datos en cuanto el VM se cree
    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            // 5. Obtenemos el usuario en un hilo de fondo (IO)
            val user = withContext(Dispatchers.IO) {
                val email = repo.getCurrentUserEmail()
                if (email != null) {
                    repo.getUser(email)
                } else {
                    null // No hay usuario logueado
                }
            }

            // 6. Actualizamos el UiState con los datos reales
            if (user != null) {
                _uiState.update {
                    it.copy(
                        userName = user.name,
                        userEmail = user.email
                    )
                }
            } else {
                // Esto no debería pasar si la lógica del NavGraph es correcta,
                // pero es bueno tener un fallback.
                _uiState.update {
                    it.copy(
                        userName = "Invitado",
                        userEmail = "Inicia sesión para ver tu perfil"
                    )
                }
            }
        }
    }

    fun onNotificationsToggled(isEnabled: Boolean) {
        _uiState.update {
            it.copy(notificationsEnabled = isEnabled)
        }
    }

    fun onDownloadGuideClicked() {
        // Simular lógica de descarga
        _uiState.update {
            it.copy(downloadedGuides = it.downloadedGuides + 1)
        }
    }
}