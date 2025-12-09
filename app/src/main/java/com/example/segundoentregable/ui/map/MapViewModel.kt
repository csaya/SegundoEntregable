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
    val shouldAnimateCamera: Boolean = false
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

    /**
     * Actualiza el query de búsqueda y filtra los atractivos
     */
    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            val filtered = applyFilters(state.allAtractivos, query, state.showOnlyFavorites, state.favoriteIds)
            state.copy(searchQuery = query, filteredAtractivos = filtered)
        }
    }

    /**
     * Activa/desactiva el modo de solo favoritos
     */
    fun setShowOnlyFavorites(show: Boolean, favoriteIds: Set<String> = emptySet()) {
        _uiState.update { state ->
            val newFavoriteIds = if (show) favoriteIds else state.favoriteIds
            val filtered = applyFilters(state.allAtractivos, state.searchQuery, show, newFavoriteIds)
            state.copy(
                showOnlyFavorites = show,
                favoriteIds = newFavoriteIds,
                filteredAtractivos = filtered
            )
        }
    }

    /**
     * Aplica los filtros de búsqueda y favoritos
     */
    private fun applyFilters(
        all: List<AtractivoTuristico>,
        query: String,
        onlyFavorites: Boolean,
        favoriteIds: Set<String>
    ): List<AtractivoTuristico> {
        var result = all

        // Filtrar por favoritos si está activo
        if (onlyFavorites && favoriteIds.isNotEmpty()) {
            result = result.filter { it.id in favoriteIds }
        }

        // Filtrar por búsqueda
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

    /**
     * Limpia los filtros
     */
    fun clearFilters() {
        _uiState.update { state ->
            state.copy(
                searchQuery = "",
                showOnlyFavorites = false,
                filteredAtractivos = state.allAtractivos,
                focusAttraction = null,
                shouldAnimateCamera = false
            )
        }
    }

    /**
     * Foca la cámara en un atractivo específico por ID
     */
    fun focusOnAttraction(attractionId: String) {
        viewModelScope.launch {
            val atractivo = _uiState.value.allAtractivos.find { it.id == attractionId }
                ?: withContext(Dispatchers.IO) { repo.getAtractivoPorId(attractionId) }
            
            if (atractivo != null) {
                _uiState.update {
                    it.copy(
                        focusAttraction = atractivo,
                        selectedAttraction = atractivo,
                        shouldAnimateCamera = true
                    )
                }
            }
        }
    }

    /**
     * Foca la cámara en los resultados filtrados (si hay pocos)
     */
    fun focusOnFilteredResults() {
        val filtered = _uiState.value.filteredAtractivos
        if (filtered.size == 1) {
            // Si hay un solo resultado, enfocar en él
            _uiState.update {
                it.copy(
                    focusAttraction = filtered.first(),
                    shouldAnimateCamera = true
                )
            }
        } else if (filtered.size in 2..5) {
            // Si hay pocos resultados, calcular el centro
            val avgLat = filtered.map { it.latitud }.average()
            val avgLon = filtered.map { it.longitud }.average()
            // Crear un atractivo "virtual" para el centro
            _uiState.update {
                it.copy(
                    focusAttraction = filtered.first().copy(latitud = avgLat, longitud = avgLon),
                    shouldAnimateCamera = true
                )
            }
        }
    }

    /**
     * Marca que la animación de cámara ya se realizó
     */
    fun onCameraAnimationComplete() {
        _uiState.update {
            it.copy(shouldAnimateCamera = false)
        }
    }
}