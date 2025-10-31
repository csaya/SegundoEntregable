package com.example.segundoentregable.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.data.repository.FakeAttractionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val favoritesList: List<AtractivoTuristico> = emptyList()
)

class FavoritesViewModel : ViewModel() {

    private val repo = FakeAttractionRepository

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    fun loadFavorites() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(favoritesList = repo.getFavoritos())
            }
        }
    }

    fun onToggleFavorite(attractionId: String) {
        repo.toggleFavorito(attractionId)
        loadFavorites()
    }
}