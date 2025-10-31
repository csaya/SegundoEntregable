package com.example.segundoentregable.ui.home

import androidx.lifecycle.ViewModel
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.data.repository.FakeAttractionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class HomeUiState(
    val recomendaciones: List<AtractivoTuristico> = emptyList(),
    val cercanos: List<AtractivoTuristico> = emptyList()
)

class HomeViewModel : ViewModel() {

    private val repo = FakeAttractionRepository

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        cargarDatosDeInicio()
    }

    private fun cargarDatosDeInicio() {
        _uiState.update {
            it.copy(
                recomendaciones = repo.getRecomendaciones(),
                cercanos = repo.getCercanos()
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        // Lógica de búsqueda
    }
}