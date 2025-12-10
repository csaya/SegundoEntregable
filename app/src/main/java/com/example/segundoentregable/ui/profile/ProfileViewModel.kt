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
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = true,
    val userName: String = "",
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
            val email = withContext(Dispatchers.IO) {
                repo.getCurrentUserEmail()
            }
            
            if (email == null) {
                // No hay sesión activa
                _uiState.update {
                    it.copy(
                        isLoggedIn = false,
                        isLoading = false,
                        userName = "",
                        userEmail = ""
                    )
                }
                return@launch
            }
            
            // Hay email, buscar usuario
            val user = withContext(Dispatchers.IO) {
                repo.getUser(email)
            }

            if (user != null) {
                _uiState.update {
                    it.copy(
                        isLoggedIn = true,
                        isLoading = false,
                        userName = user.name,
                        userEmail = user.email
                    )
                }
            } else {
                // Email en SharedPrefs pero usuario no en DB (datos corruptos)
                // Limpiar sesión
                withContext(Dispatchers.IO) {
                    repo.logout()
                }
                _uiState.update {
                    it.copy(
                        isLoggedIn = false,
                        isLoading = false,
                        userName = "",
                        userEmail = ""
                    )
                }
            }
        }
    }
    
    fun refreshProfile() {
        loadUserProfile()
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