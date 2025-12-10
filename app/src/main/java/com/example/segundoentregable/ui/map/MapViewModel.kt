package com.example.segundoentregable.ui.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

private const val TAG = "MapViewModel"

data class MapUiState(
    val allAtractivos: List<AtractivoTuristico> = emptyList(),
    val filteredAtractivos: List<AtractivoTuristico> = emptyList(),
    val selectedAttraction: AtractivoTuristico? = null,
    val searchQuery: String = "",
    val showOnlyFavorites: Boolean = false,
    val favoriteIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val focusAttraction: AtractivoTuristico? = null,
    val shouldAnimateCamera: Boolean = false,
    val focusedAttractionId: String? = null,
    // Campos para vista de ruta
    val routeMode: Boolean = false,
    val routeAtractivos: List<AtractivoTuristico> = emptyList(),
    // Ubicación del usuario para inicio de ruta
    val userLatitude: Double? = null,
    val userLongitude: Double? = null
)

class MapViewModel(
    private val repo: AttractionRepository,
    private val isDataReadyFlow: StateFlow<Boolean>
) : ViewModel() {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        esperarDatosYCargar()
    }

    private fun esperarDatosYCargar() {
        viewModelScope.launch {
            Log.d(TAG, "Esperando a que los datos estén listos...")
            isDataReadyFlow.first { it }
            Log.d(TAG, "Datos listos, cargando marcadores...")
            loadAtractivos()
        }
    }

    private suspend fun loadAtractivos() {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val atractivos = withContext(Dispatchers.IO) {
                repo.getTodosLosAtractivos()
            }
            Log.d(TAG, "Cargados ${atractivos.size} atractivos para el mapa")
            _uiState.update {
                it.copy(
                    allAtractivos = atractivos,
                    filteredAtractivos = atractivos,
                    isLoading = false
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cargando atractivos", e)
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            val filtered = applyFilters(
                state.allAtractivos,
                query,
                state.showOnlyFavorites,
                state.favoriteIds,
                null
            )
            state.copy(
                searchQuery = query,
                filteredAtractivos = filtered,
                focusedAttractionId = null
            )
        }
    }

    fun setShowOnlyFavorites(show: Boolean, favoriteIds: Set<String> = emptySet()) {
        _uiState.update { state ->
            val newFavoriteIds = if (show) favoriteIds else state.favoriteIds
            val filtered = applyFilters(
                state.allAtractivos,
                state.searchQuery,
                show,
                newFavoriteIds,
                null
            )
            state.copy(
                showOnlyFavorites = show,
                favoriteIds = newFavoriteIds,
                filteredAtractivos = filtered,
                focusedAttractionId = null
            )
        }
    }

    private fun applyFilters(
        all: List<AtractivoTuristico>,
        query: String,
        onlyFavorites: Boolean,
        favoriteIds: Set<String>,
        focusedId: String? = null
    ): List<AtractivoTuristico> {
        var result = all

        if (!focusedId.isNullOrBlank()) {
            return result.filter { it.id == focusedId }
        }

        if (onlyFavorites && favoriteIds.isNotEmpty()) {
            result = result.filter { it.id in favoriteIds }
        }

        if (query.isNotBlank()) {
            val lowerQuery = query.lowercase()
            result = result.filter { atractivo ->
                atractivo.nombre.lowercase().contains(lowerQuery) ||
                        atractivo.categoria.lowercase().contains(lowerQuery) ||
                        atractivo.ubicacion.lowercase().contains(lowerQuery)
            }
        }

        return result
    }

    fun selectAttraction(atractivo: AtractivoTuristico) {
        _uiState.update {
            it.copy(selectedAttraction = atractivo)
        }
    }

    fun dismissAttractionDetail() {
        _uiState.update {
            it.copy(selectedAttraction = null)
        }
    }

    fun clearFilters() {
        _uiState.update { state ->
            state.copy(
                searchQuery = "",
                showOnlyFavorites = false,
                filteredAtractivos = state.allAtractivos,
                focusAttraction = null,
                shouldAnimateCamera = false,
                focusedAttractionId = null,
                routeMode = false, // ✅ Salir del modo ruta
                routeAtractivos = emptyList()
            )
        }
    }

    fun focusOnAttraction(attractionId: String) {
        viewModelScope.launch {
            val atractivo = _uiState.value.allAtractivos.find { it.id == attractionId }
                ?: withContext(Dispatchers.IO) { repo.getAtractivoPorId(attractionId) }

            if (atractivo != null) {
                val filtered = listOf(atractivo)

                _uiState.update {
                    it.copy(
                        focusAttraction = atractivo,
                        selectedAttraction = atractivo,
                        shouldAnimateCamera = true,
                        focusedAttractionId = attractionId,
                        filteredAtractivos = filtered
                    )
                }
            }
        }
    }

    /**
     * Carga una ruta en el mapa.
     * Espera a que los datos estén disponibles antes de filtrar.
     */
    fun loadRouteView(routeIds: List<String>) {
        viewModelScope.launch {
            Log.d(TAG, "Cargando vista de ruta con ${routeIds.size} puntos")
            
            // Esperar a que los datos estén cargados
            if (_uiState.value.allAtractivos.isEmpty()) {
                Log.d(TAG, "Datos no cargados aún, esperando...")
                isDataReadyFlow.first { it }
                // Recargar atractivos si es necesario
                if (_uiState.value.allAtractivos.isEmpty()) {
                    loadAtractivos()
                }
            }
            
            // Obtener atractivos por IDs, consultando BD si no están en memoria
            val routeAtractivos = routeIds.mapNotNull { id ->
                _uiState.value.allAtractivos.find { it.id == id }
                    ?: withContext(Dispatchers.IO) { repo.getAtractivoPorId(id) }
            }

            _uiState.update {
                it.copy(
                    routeMode = true,
                    routeAtractivos = routeAtractivos,
                    filteredAtractivos = routeAtractivos,
                    shouldAnimateCamera = true,
                    searchQuery = "",
                    showOnlyFavorites = false,
                    focusedAttractionId = null
                )
            }

            Log.d(TAG, "Ruta cargada: ${routeAtractivos.size} atractivos")
        }
    }

    /**
     * Sale del modo ruta y muestra todos los atractivos.
     */
    fun exitRouteMode() {
        _uiState.update { state ->
            state.copy(
                routeMode = false,
                routeAtractivos = emptyList(),
                filteredAtractivos = state.allAtractivos
            )
        }
    }

    fun onCameraAnimationComplete() {
        _uiState.update {
            it.copy(shouldAnimateCamera = false)
        }
    }
    
    /**
     * Actualiza la ubicación del usuario para inicio de ruta.
     */
    fun updateUserLocation(latitude: Double, longitude: Double) {
        _uiState.update {
            it.copy(
                userLatitude = latitude,
                userLongitude = longitude
            )
        }
    }
}
