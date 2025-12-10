package com.example.segundoentregable.ui.planner

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.segundoentregable.AppApplication
import com.example.segundoentregable.data.local.entity.SavedRouteEntity
import com.example.segundoentregable.data.location.LocationService
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.data.repository.SavedRouteRepository
import com.example.segundoentregable.data.repository.UserRouteRepository
import com.example.segundoentregable.utils.RouteOptimizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "PlannerViewModel"

/**
 * Estado UI para el planificador de rutas personales.
 */
data class PlannerUiState(
    val routeAtractivos: List<AtractivoTuristico> = emptyList(),
    val optimizedRoute: List<AtractivoTuristico> = emptyList(),
    val totalDistance: String = "",
    val estimatedTime: String = "",
    val totalDistanceKm: Float = 0f,
    val estimatedTimeMinutes: Int = 0,
    val userLocation: Location? = null,
    val isLoading: Boolean = true,
    val isOptimizing: Boolean = false,
    val isOptimized: Boolean = false,
    val isEmpty: Boolean = true,
    val error: String? = null,
    // Rutas guardadas
    val savedRoutes: List<SavedRouteEntity> = emptyList(),
    val showSaveDialog: Boolean = false,
    val showLoadDialog: Boolean = false,
    val saveSuccess: Boolean = false,
    val loadedRouteName: String? = null
)

class PlannerViewModel(
    private val userRouteRepository: UserRouteRepository,
    private val savedRouteRepository: SavedRouteRepository,
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
            Log.d(TAG, "Datos listos, observando ruta del usuario...")
            observeUserRoute()
            observeSavedRoutes()
            loadUserLocation()
        }
    }

    /**
     * Observa los cambios en la ruta del usuario (Room Flow)
     */
    private fun observeUserRoute() {
        viewModelScope.launch {
            userRouteRepository.getRouteAtractivosFlow().collect { atractivos ->
                Log.d(TAG, "Ruta actualizada: ${atractivos.size} lugares")
                _uiState.update { 
                    it.copy(
                        routeAtractivos = atractivos,
                        isLoading = false,
                        isEmpty = atractivos.isEmpty(),
                        isOptimized = false,
                        optimizedRoute = emptyList(),
                        totalDistance = "",
                        estimatedTime = "",
                        loadedRouteName = null
                    )
                }
            }
        }
    }

    /**
     * Observa las rutas guardadas
     */
    private fun observeSavedRoutes() {
        viewModelScope.launch {
            savedRouteRepository.getAllRoutesFlow().collect { routes ->
                _uiState.update { it.copy(savedRoutes = routes) }
            }
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

    /**
     * Elimina un atractivo de la ruta
     */
    fun removeFromRoute(atractivoId: String) {
        viewModelScope.launch {
            try {
                userRouteRepository.removeFromRoute(atractivoId)
                Log.d(TAG, "Eliminado de la ruta: $atractivoId")
            } catch (e: Exception) {
                Log.e(TAG, "Error eliminando de la ruta", e)
            }
        }
    }
    
    /**
     * Optimiza el orden de la ruta usando el algoritmo del vecino más cercano
     */
    fun optimizeRoute() {
        val state = _uiState.value
        if (state.routeAtractivos.isEmpty()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isOptimizing = true) }
            
            val userLat = state.userLocation?.latitude ?: -16.3989
            val userLng = state.userLocation?.longitude ?: -71.5349
            
            // Optimizar ruta
            val optimized = RouteOptimizer.optimizeRoute(
                state.routeAtractivos,
                userLat,
                userLng
            )
            
            // Calcular métricas
            val totalDistanceKm = RouteOptimizer.calculateTotalDistance(optimized, userLat, userLng)
            val estimatedMinutes = RouteOptimizer.estimateTotalTime(optimized, userLat, userLng)

            // Guardar el nuevo orden en la BD
            userRouteRepository.updateRouteOrder(optimized.map { it.id })
            
            _uiState.update {
                it.copy(
                    optimizedRoute = optimized,
                    totalDistance = RouteOptimizer.formatDistance(totalDistanceKm),
                    estimatedTime = RouteOptimizer.formatTime(estimatedMinutes),
                    totalDistanceKm = totalDistanceKm.toFloat(),
                    estimatedTimeMinutes = estimatedMinutes,
                    isOptimizing = false,
                    isOptimized = true
                )
            }
            
            Log.d(TAG, "Ruta optimizada: ${optimized.size} lugares, $totalDistanceKm km")
        }
    }
    
    /**
     * Limpia toda la ruta
     */
    fun clearRoute() {
        viewModelScope.launch {
            try {
                userRouteRepository.clearRoute()
                Log.d(TAG, "Ruta limpiada")
            } catch (e: Exception) {
                Log.e(TAG, "Error limpiando ruta", e)
            }
        }
    }

    /**
     * Obtiene la lista de atractivos para navegar (optimizada si existe, sino la original)
     */
    fun getNavigationList(): List<AtractivoTuristico> {
        val state = _uiState.value
        return if (state.isOptimized && state.optimizedRoute.isNotEmpty()) {
            state.optimizedRoute
        } else {
            state.routeAtractivos
        }
    }

    // ========== FUNCIONALIDAD DE GUARDAR/CARGAR RUTAS ==========

    fun showSaveDialog() {
        _uiState.update { it.copy(showSaveDialog = true) }
    }

    fun hideSaveDialog() {
        _uiState.update { it.copy(showSaveDialog = false) }
    }

    fun showLoadDialog() {
        _uiState.update { it.copy(showLoadDialog = true) }
    }

    fun hideLoadDialog() {
        _uiState.update { it.copy(showLoadDialog = false) }
    }

    /**
     * Guarda la ruta actual con un nombre
     */
    fun saveCurrentRoute(nombre: String, descripcion: String = "") {
        val state = _uiState.value
        val atractivos = getNavigationList()
        
        if (atractivos.isEmpty() || nombre.isBlank()) return

        viewModelScope.launch {
            try {
                savedRouteRepository.saveRoute(
                    nombre = nombre.trim(),
                    descripcion = descripcion.trim(),
                    atractivos = atractivos,
                    totalDistance = state.totalDistanceKm,
                    estimatedTime = state.estimatedTimeMinutes
                )
                _uiState.update { 
                    it.copy(
                        showSaveDialog = false,
                        saveSuccess = true,
                        loadedRouteName = nombre.trim()
                    ) 
                }
                Log.d(TAG, "Ruta guardada: $nombre")
            } catch (e: Exception) {
                Log.e(TAG, "Error guardando ruta", e)
                _uiState.update { it.copy(error = "Error al guardar la ruta") }
            }
        }
    }

    /**
     * Carga una ruta guardada
     */
    fun loadSavedRoute(routeId: String) {
        viewModelScope.launch {
            try {
                val route = savedRouteRepository.getRouteById(routeId) ?: return@launch
                val atractivos = savedRouteRepository.getRouteAtractivos(routeId)
                
                if (atractivos.isEmpty()) {
                    _uiState.update { it.copy(error = "La ruta está vacía") }
                    return@launch
                }

                // Limpiar ruta actual y cargar la guardada
                userRouteRepository.clearRoute()
                userRouteRepository.createRouteFromAtractivos(atractivos.map { it.id })
                
                _uiState.update { 
                    it.copy(
                        showLoadDialog = false,
                        loadedRouteName = route.nombre
                    ) 
                }
                Log.d(TAG, "Ruta cargada: ${route.nombre}")
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando ruta", e)
                _uiState.update { it.copy(error = "Error al cargar la ruta") }
            }
        }
    }

    /**
     * Elimina una ruta guardada
     */
    fun deleteSavedRoute(routeId: String) {
        viewModelScope.launch {
            try {
                savedRouteRepository.deleteRoute(routeId)
                Log.d(TAG, "Ruta eliminada: $routeId")
            } catch (e: Exception) {
                Log.e(TAG, "Error eliminando ruta", e)
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}

class PlannerViewModelFactory(
    private val app: AppApplication
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlannerViewModel::class.java)) {
            return PlannerViewModel(
                userRouteRepository = app.userRouteRepository,
                savedRouteRepository = app.savedRouteRepository,
                locationService = app.locationService,
                isDataReadyFlow = app.isDataReady
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
