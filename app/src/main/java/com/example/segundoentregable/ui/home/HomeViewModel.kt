package com.example.segundoentregable.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.segundoentregable.data.location.LocationService
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.data.repository.AttractionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class HomeUiState(
    val recomendaciones: List<AtractivoTuristico> = emptyList(),
    val cercanos: List<AtractivoTuristico> = emptyList(),
    val isLoading: Boolean = false
)

class HomeViewModel(
    private val repo: AttractionRepository,
    private val locationService: LocationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        cargarDatosDeInicio()
    }

    private fun cargarDatosDeInicio() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Los datos se cargan desde DataImporter en AppApplication
                // Cargamos las listas usando las funciones del repositorio
                val recomendaciones = withContext(Dispatchers.IO) {
                    repo.getRecomendaciones()
                }
                val cercanos = withContext(Dispatchers.IO) {
                    repo.getCercanos()
                }

                _uiState.update {
                    it.copy(
                        recomendaciones = recomendaciones,
                        cercanos = cercanos,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        // Lógica futura para búsqueda
    }

    fun updateLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 1. Obtener coordenadas del GPS
                val location = locationService.getCurrentLocation()

                if (location != null) {
                    // 2. Pedir al repo los cercanos reales
                    val cercanos = withContext(Dispatchers.IO) {
                        repo.getCercanosReal(location.latitude, location.longitude)
                    }
                    _uiState.update { it.copy(cercanos = cercanos) }
                } else {
                    // Fallback si el GPS está apagado (usamos lógica antigua o lista vacía)
                    // ...
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}