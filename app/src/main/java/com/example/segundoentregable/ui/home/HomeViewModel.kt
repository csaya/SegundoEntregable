package com.example.segundoentregable.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.segundoentregable.data.location.LocationService
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.data.repository.AttractionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "HomeViewModel"

data class HomeUiState(
    val recomendaciones: List<AtractivoTuristico> = emptyList(),
    val cercanos: List<AtractivoTuristico> = emptyList(),
    val isLoading: Boolean = true, // Iniciar en true
    val isEmpty: Boolean = false
)

class HomeViewModel(
    private val repo: AttractionRepository,
    private val locationService: LocationService,
    private val isDataReadyFlow: StateFlow<Boolean>
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        esperarDatosYCargar()
    }

    private fun esperarDatosYCargar() {
        viewModelScope.launch {
            Log.d(TAG, "Esperando a que los datos estén listos...")
            // Esperar a que AppApplication termine de importar datos
            isDataReadyFlow.first { it }
            Log.d(TAG, "Datos listos, cargando...")
            cargarDatosDeInicio()
        }
    }

    private suspend fun cargarDatosDeInicio() {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val recomendaciones = withContext(Dispatchers.IO) {
                repo.getRecomendaciones()
            }
            val cercanos = withContext(Dispatchers.IO) {
                repo.getCercanos()
            }

            Log.d(TAG, "Cargados: ${recomendaciones.size} recomendaciones, ${cercanos.size} cercanos")

            _uiState.update {
                it.copy(
                    recomendaciones = recomendaciones,
                    cercanos = cercanos,
                    isLoading = false,
                    isEmpty = recomendaciones.isEmpty() && cercanos.isEmpty()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cargando datos", e)
            _uiState.update { it.copy(isLoading = false, isEmpty = true) }
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