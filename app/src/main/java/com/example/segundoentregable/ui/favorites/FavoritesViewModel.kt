package com.example.segundoentregable.ui.favorites

import android.util.Log
import androidx.lifecycle.ViewModel
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

class FavoritesViewModel(
    private val attractionRepo: AttractionRepository,
    private val favoriteRepo: FavoriteRepository,
    private val userRepo: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    // Cargar favoritos al iniciar
    init {
        loadFavorites()
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Usamos "guest_user" si es nulo para no romper la app en pruebas,
                // o puedes dejarlo nulo si prefieres que no muestre nada.
                val userEmail = userRepo.getCurrentUserEmail() ?: "guest_user"

                // 1. Obtener IDs
                val favoritoIds = favoriteRepo.getFavoritosByUser(userEmail)

                // 2. Obtener objetos completos
                val favoritos = favoritoIds.mapNotNull { id ->
                    attractionRepo.getAtractivoPorId(id)
                }

                _uiState.update {
                    it.copy(favoritesList = favoritos, isLoading = false)
                }
            } catch (e: Exception) {
                Log.e("FavoritesVM", "Error cargando favoritos", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onToggleFavorite(attractionId: String) {
        viewModelScope.launch {
            try {
                val userEmail = userRepo.getCurrentUserEmail() ?: "guest_user"

                favoriteRepo.toggleFavorito(userEmail, attractionId)

                // Recargar la lista después de quitar/poner
                loadFavorites()
            } catch (e: Exception) {
                Log.e("FavoritesVM", "Error toggle favorito", e)
            }
        }
    }
}