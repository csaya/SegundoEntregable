package com.example.segundoentregable.ui.planner

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.segundoentregable.AppApplication
import com.example.segundoentregable.data.location.LocationService
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.data.repository.AttractionRepository
import com.example.segundoentregable.utils.RouteOptimizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "PlannerViewModel"

data class PlannerUiState(
    val allAtractivos: List<AtractivoTuristico> = emptyList(),
    val selectedAtractivos: List<AtractivoTuristico> = emptyList(),
    val optimizedRoute: List<AtractivoTuristico> = emptyList(),
    val totalDistance: String = "",
    val estimatedTime: String = "",
    val userLocation: Location? = null,
    val isLoading: Boolean = true,
    val isOptimizing: Boolean = false,
    val showOptimizedRoute: Boolean = false,
    val error: String? = null
)

class PlannerViewModel(
    private val attractionRepository: AttractionRepository,
    private val locationService: LocationService,
    private val isDataReadyFlow: StateFlow<Boolean>
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PlannerUiState())
    val uiState: StateFlow<PlannerUiState> = _uiState.asStateFlow()
    
    init {
        esperarDatosYCargar()
    }

    private fun esperarDatosYCargar() {
        viewModelScope.launch {
            Log.d(TAG, "Esperando a que los datos estén listos...")
            isDataReadyFlow.first { it }
            Log.d(TAG, "Datos listos, cargando...")
            loadAtractivos()
            loadUserLocation()
        }
    }
    
    private suspend fun loadAtractivos() {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val atractivos = attractionRepository.getTodosLosAtractivos()
            Log.d(TAG, "Cargados ${atractivos.size} atractivos")
            _uiState.update { 
                it.copy(
                    allAtractivos = atractivos,
                    isLoading = false
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cargando atractivos", e)
            _uiState.update { it.copy(error = e.message, isLoading = false) }
        }
    }
    
    private fun loadUserLocation() {
        viewModelScope.launch {
            try {
                val location = locationService.getCurrentLocation()
                _uiState.update { it.copy(userLocation = location) }
            } catch (e: Exception) {
                // Usar ubicación por defecto (Plaza de Armas de Arequipa)
                val defaultLocation = Location("default").apply {
                    latitude = -16.3989
                    longitude = -71.5349
                }
                _uiState.update { it.copy(userLocation = defaultLocation) }
            }
        }
    }
    
    fun toggleAtractivoSelection(atractivo: AtractivoTuristico) {
        _uiState.update { state ->
            val currentSelection = state.selectedAtractivos.toMutableList()
            if (currentSelection.any { it.id == atractivo.id }) {
                currentSelection.removeAll { it.id == atractivo.id }
            } else {
                currentSelection.add(atractivo)
            }
            state.copy(
                selectedAtractivos = currentSelection,
                showOptimizedRoute = false // Reset cuando cambia selección
            )
        }
    }
    
    fun isSelected(atractivoId: String): Boolean {
        return _uiState.value.selectedAtractivos.any { it.id == atractivoId }
    }
    
    fun optimizeRoute() {
        val state = _uiState.value
        if (state.selectedAtractivos.isEmpty()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isOptimizing = true) }
            
            val userLat = state.userLocation?.latitude ?: -16.3989
            val userLng = state.userLocation?.longitude ?: -71.5349
            
            // Optimizar ruta
            val optimized = RouteOptimizer.optimizeRoute(
                state.selectedAtractivos,
                userLat,
                userLng
            )
            
            // Calcular métricas
            val totalDistanceKm = RouteOptimizer.calculateTotalDistance(optimized, userLat, userLng)
            val estimatedMinutes = RouteOptimizer.estimateTotalTime(optimized, userLat, userLng)
            
            _uiState.update {
                it.copy(
                    optimizedRoute = optimized,
                    totalDistance = RouteOptimizer.formatDistance(totalDistanceKm),
                    estimatedTime = RouteOptimizer.formatTime(estimatedMinutes),
                    isOptimizing = false,
                    showOptimizedRoute = true
                )
            }
        }
    }
    
    fun clearSelection() {
        _uiState.update {
            it.copy(
                selectedAtractivos = emptyList(),
                optimizedRoute = emptyList(),
                showOptimizedRoute = false,
                totalDistance = "",
                estimatedTime = ""
            )
        }
    }
}

class PlannerViewModelFactory(
    private val app: AppApplication
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlannerViewModel::class.java)) {
            return PlannerViewModel(
                attractionRepository = app.attractionRepository,
                locationService = app.locationService,
                isDataReadyFlow = app.isDataReady
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
