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
    // ✅ Nuevo campo para saber si estamos en modo "focus único"
    val focusedAttractionId: String? = null
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
            // ✅ Si hay búsqueda, limpiar el focusedAttractionId
            val filtered = applyFilters(
                state.allAtractivos,
                query,
                state.showOnlyFavorites,
                state.favoriteIds,
                null // Sin focus cuando hay búsqueda
            )
            state.copy(
                searchQuery = query,
                filteredAtractivos = filtered,
                focusedAttractionId = null // Limpiar focus
            )
        }
    }

    /**
     * Activa/desactiva el modo de solo favoritos
     */
    fun setShowOnlyFavorites(show: Boolean, favoriteIds: Set<String> = emptySet()) {
        _uiState.update { state ->
            val newFavoriteIds = if (show) favoriteIds else state.favoriteIds
            val filtered = applyFilters(
                state.allAtractivos,
                state.searchQuery,
                show,
                newFavoriteIds,
                null // Sin focus cuando hay filtro de favoritos
            )
            state.copy(
                showOnlyFavorites = show,
                favoriteIds = newFavoriteIds,
                filteredAtractivos = filtered,
                focusedAttractionId = null // Limpiar focus
            )
        }
    }

    /**
     * Aplica los filtros de búsqueda, favoritos y focus único
     */
    private fun applyFilters(
        all: List<AtractivoTuristico>,
        query: String,
        onlyFavorites: Boolean,
        favoriteIds: Set<String>,
        focusedId: String? = null
    ): List<AtractivoTuristico> {
        var result = all

        // ✅ Filtrar por focus único (prioridad máxima)
        if (!focusedId.isNullOrBlank()) {
            return result.filter { it.id == focusedId }
        }

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
                shouldAnimateCamera = false,
                focusedAttractionId = null // ✅ Limpiar focus
            )
        }
    }

    /**
     * ✅ Foca la cámara en un atractivo específico por ID
     * Y filtra para mostrar SOLO ese atractivo
     */
    fun focusOnAttraction(attractionId: String) {
        viewModelScope.launch {
            val atractivo = _uiState.value.allAtractivos.find { it.id == attractionId }
                ?: withContext(Dispatchers.IO) { repo.getAtractivoPorId(attractionId) }

            if (atractivo != null) {
                // ✅ Filtrar para mostrar solo este atractivo
                val filtered = listOf(atractivo)

                _uiState.update {
                    it.copy(
                        focusAttraction = atractivo,
                        selectedAttraction = atractivo,
                        shouldAnimateCamera = true,
                        focusedAttractionId = attractionId, // ✅ Guardar el ID enfocado
                        filteredAtractivos = filtered // ✅ Solo mostrar este
                    )
                }
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
