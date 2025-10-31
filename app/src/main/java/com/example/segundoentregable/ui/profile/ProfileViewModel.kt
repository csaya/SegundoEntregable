package com.example.segundoentregable.ui.profile

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ProfileUiState(
    val userName: String = "Sofia Ramirez",
    val userEmail: String = "sofia.ramirez@email.com",
    val downloadedGuides: Int = 3,
    val notificationsEnabled: Boolean = false
)

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

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