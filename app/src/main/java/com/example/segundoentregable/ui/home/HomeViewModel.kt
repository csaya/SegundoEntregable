package com.example.segundoentregable.ui.home

import androidx.lifecycle.ViewModel
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.data.repository.FakeAttractionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Estado de la UI: Las listas que la pantalla mostrará
data class HomeUiState(
    val recomendaciones: List<AtractivoTuristico> = emptyList(),
    val cercanos: List<AtractivoTuristico> = emptyList()
)

class HomeViewModel : ViewModel() { // No necesitamos Application, así que usamos ViewModel simple

    // Usamos el repositorio falso
    private val repo = FakeAttractionRepository

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Cargar los datos falsos al iniciar el ViewModel
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

    // Dejaremos la lógica de búsqueda para más adelante
    fun onSearchQueryChanged(query: String) {
        // Lógica de búsqueda (Criterio 2)
    }
}