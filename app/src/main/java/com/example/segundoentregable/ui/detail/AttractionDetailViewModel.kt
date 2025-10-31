package com.example.segundoentregable.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.data.model.Review
import com.example.segundoentregable.data.repository.FakeAttractionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val atractivo: AtractivoTuristico? = null,
    val reviews: List<Review> = emptyList(),
    val isFavorito: Boolean = false
)

class AttractionDetailViewModel(
    savedStateHandle: SavedStateHandle // Inyectado automáticamente
) : ViewModel() {

    private val repo = FakeAttractionRepository

    // 1. Obtenemos el ID del atractivo desde la navegación
    private val attractionId: String = checkNotNull(savedStateHandle["attractionId"])

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        // 2. Cargamos los datos de ESE atractivo
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch { // (Aunque es falso, simulamos que es asíncrono)
            _uiState.update {
                it.copy(
                    atractivo = repo.getAtractivoPorId(attractionId),
                    reviews = repo.getReviewsForAttraction(attractionId),
                    isFavorito = repo.isFavorito(attractionId)
                )
            }
        }
    }

    // 3. Lógica para el botón "Guardar"
    fun onToggleFavorite() {
        repo.toggleFavorito(attractionId)
        _uiState.update {
            it.copy(isFavorito = repo.isFavorito(attractionId))
        }
    }
}