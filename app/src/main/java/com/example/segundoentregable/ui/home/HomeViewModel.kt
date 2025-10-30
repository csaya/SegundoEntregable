package com.example.segundoentregable.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.segundoentregable.data.model.User
import com.example.segundoentregable.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class HomeUiState(
    val userName: String = ""
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = UserRepository(application.applicationContext)
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

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
                _uiState.update { currentState ->
                    currentState.copy(userName = user.name)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.setCurrentUser(null)
        }
    }
}