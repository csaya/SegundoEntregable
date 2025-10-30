package com.example.segundoentregable.ui.profile

import android.app.Application
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

data class ProfileUiState(
    val name: String = "",
    val email: String = ""
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = UserRepository(application.applicationContext)
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    private val _logoutEvent = MutableSharedFlow<Unit>()
    val logoutEvent = _logoutEvent.asSharedFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) {
                val email = repo.getCurrentUserEmail() ?: return@withContext null
                repo.getUser(email)
            }

            if (user != null) {
                _uiState.update {
                    it.copy(name = user.name, email = user.email)
                }
            } else {
                _uiState.update {
                    it.copy(name = "Invitado", email = "Error al cargar")
                }
            }
        }
    }

    fun onLogoutClicked() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repo.setCurrentUser(null)
            }
            _logoutEvent.emit(Unit)
        }
    }
}