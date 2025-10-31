package com.example.segundoentregable.ui.map

import androidx.lifecycle.ViewModel
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.data.repository.FakeAttractionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Estado de la UI: la lista de atractivos y cuál está seleccionado
data class MapUiState(
    val atractivos: List<AtractivoTuristico> = emptyList(),
    val selectedAttraction: AtractivoTuristico? = null
)

class MapViewModel : ViewModel() {

    private val repo = FakeAttractionRepository

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        // Cargamos todos los atractivos para ponerlos en el "mapa"
        _uiState.update {
            it.copy(atractivos = repo.getTodosLosAtractivos())
        }
    }

    // Criterio 2: Lógica para "seleccionar" un marcador
    fun selectAttraction(atractivo: AtractivoTuristico) {
        _uiState.update {
            it.copy(selectedAttraction = atractivo)
        }
    }

    // Lógica para cerrar la hoja inferior
    fun dismissAttractionDetail() {
        _uiState.update {
            it.copy(selectedAttraction = null)
        }
    }
}