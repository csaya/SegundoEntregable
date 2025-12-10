package com.example.segundoentregable.ui.profile

import androidx.lifecycle.ViewModel
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
    val userName: String = "Cargando...",
    val userEmail: String = "",
    val downloadedGuides: Int = 3,
    val notificationsEnabled: Boolean = false
)

// CAMBIO: Hereda de ViewModel y recibe el Repo
class ProfileViewModel(
    private val repo: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) {
                val email = repo.getCurrentUserEmail()
                if (email != null) {
                    repo.getUser(email)
                } else {
                    null
                }
            }

            if (user != null) {
                _uiState.update {
                    it.copy(
                        userName = user.name,
                        userEmail = user.email
                    )
                }
            } else {
                // Usuario no logueado - no debería llegar aquí normalmente
                // pero si llega, mostrar mensaje para iniciar sesión
                _uiState.update {
                    it.copy(
                        userName = "Sin sesión",
                        userEmail = "Inicia sesión para acceder a tu perfil"
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
        _uiState.update {
            it.copy(downloadedGuides = it.downloadedGuides + 1)
        }
    }

    // NUEVO: Función para cerrar sesión real
    fun logout() {
        repo.logout()
    }
}