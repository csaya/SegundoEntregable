package com.example.segundoentregable.ui.favorites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.data.repository.AttractionRepository
import com.example.segundoentregable.data.repository.FavoriteRepository
import com.example.segundoentregable.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class FavoritesUiState(
    val favoritesList: List<AtractivoTuristico> = emptyList(),
    val isLoading: Boolean = false
)

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val attractionRepo = AttractionRepository(application.applicationContext)
    private val favoriteRepo = FavoriteRepository(application.applicationContext)
    private val userRepo = UserRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    fun loadFavorites() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val userEmail = userRepo.getCurrentUserEmail()
                if (userEmail != null) {
                    val favoritoIds = withContext(Dispatchers.IO) {
                        favoriteRepo.getFavoritosByUser(userEmail)
                    }
                    val favoritos = withContext(Dispatchers.IO) {
                        favoritoIds.mapNotNull { attractionRepo.getAtractivoPorId(it) }
                    }
                    _uiState.update {
                        it.copy(favoritesList = favoritos, isLoading = false)
                    }
                } else {
                    _uiState.update { it.copy(favoritesList = emptyList(), isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onToggleFavorite(attractionId: String) {
        val userEmail = userRepo.getCurrentUserEmail() ?: return
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    favoriteRepo.toggleFavorito(userEmail, attractionId)
                }
                loadFavorites()
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
}