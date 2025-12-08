package com.example.segundoentregable.ui.list

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

private const val TAG = "AttractionListViewModel"

/**
 * Opciones de filtro por precio
 */
enum class PriceFilter {
    ALL,      // Todos
    FREE,     // Gratis (precio = 0)
    PAID      // De pago (precio > 0)
}

/**
 * Opciones de ordenamiento
 */
enum class SortOption {
    DEFAULT,      // Sin ordenar
    RATING_DESC,  // Mayor rating primero
    RATING_ASC,   // Menor rating primero
    NAME_ASC,     // A-Z
    NAME_DESC,    // Z-A
    PRICE_ASC,    // Más barato primero
    PRICE_DESC    // Más caro primero
}

data class AttractionListUiState(
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val priceFilter: PriceFilter = PriceFilter.ALL,
    val minRating: Float = 0f,
    val sortOption: SortOption = SortOption.RATING_DESC,
    val categoriasDisponibles: List<String> = emptyList(),
    val listaFiltrada: List<AtractivoTuristico> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false
)

class AttractionListViewModel(
    private val repo: AttractionRepository,
    private val isDataReadyFlow: StateFlow<Boolean>
) : ViewModel() {

    private val _uiState = MutableStateFlow(AttractionListUiState())
    val uiState: StateFlow<AttractionListUiState> = _uiState.asStateFlow()

    private var todosLosAtractivos: List<AtractivoTuristico> = emptyList()

    init {
        esperarDatosYCargar()
    }

    private fun esperarDatosYCargar() {
        viewModelScope.launch {
            Log.d(TAG, "Esperando a que los datos estén listos...")
            isDataReadyFlow.first { it }
            Log.d(TAG, "Datos listos, cargando lista...")
            loadAtractivos()
        }
    }

    private suspend fun loadAtractivos() {
        _uiState.update { it.copy(isLoading = true) }
        try {
            todosLosAtractivos = withContext(Dispatchers.IO) {
                repo.getTodosLosAtractivos()
            }
            Log.d(TAG, "Cargados ${todosLosAtractivos.size} atractivos")

            _uiState.update {
                it.copy(
                    listaFiltrada = todosLosAtractivos,
                    categoriasDisponibles = todosLosAtractivos.map { a -> a.categoria }.distinct().sorted(),
                    isLoading = false
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cargando atractivos", e)
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        filterAndSortList()
    }

    fun onCategorySelected(category: String?) {
        // Si tocan la misma categoría, la deseleccionamos (toggle)
        val newCategory = if (category == _uiState.value.selectedCategory) null else category
        _uiState.update { it.copy(selectedCategory = newCategory) }
        filterAndSortList()
    }

    fun onPriceFilterChanged(filter: PriceFilter) {
        _uiState.update { it.copy(priceFilter = filter) }
        filterAndSortList()
    }

    fun onMinRatingChanged(rating: Float) {
        _uiState.update { it.copy(minRating = rating) }
        filterAndSortList()
    }

    fun onSortOptionChanged(option: SortOption) {
        _uiState.update { it.copy(sortOption = option) }
        filterAndSortList()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            try {
                todosLosAtractivos = withContext(Dispatchers.IO) {
                    repo.getTodosLosAtractivos()
                }
                filterAndSortList()
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    private fun filterAndSortList() {
        viewModelScope.launch(Dispatchers.Default) {
            val state = _uiState.value

            // 1. Filtrar
            val filtered = todosLosAtractivos.filter { atractivo ->
                // Búsqueda por nombre o descripción
                val matchQuery = state.searchQuery.isEmpty() ||
                    atractivo.nombre.contains(state.searchQuery, ignoreCase = true) ||
                    atractivo.descripcionCorta.contains(state.searchQuery, ignoreCase = true)

                // Categoría
                val matchCategory = state.selectedCategory == null ||
                    atractivo.categoria == state.selectedCategory

                // Precio
                val matchPrice = when (state.priceFilter) {
                    PriceFilter.ALL -> true
                    PriceFilter.FREE -> atractivo.precio == 0.0
                    PriceFilter.PAID -> atractivo.precio > 0.0
                }

                // Rating mínimo
                val matchRating = atractivo.rating >= state.minRating

                matchQuery && matchCategory && matchPrice && matchRating
            }

            // 2. Ordenar
            val sorted = when (state.sortOption) {
                SortOption.DEFAULT -> filtered
                SortOption.RATING_DESC -> filtered.sortedByDescending { it.rating }
                SortOption.RATING_ASC -> filtered.sortedBy { it.rating }
                SortOption.NAME_ASC -> filtered.sortedBy { it.nombre }
                SortOption.NAME_DESC -> filtered.sortedByDescending { it.nombre }
                SortOption.PRICE_ASC -> filtered.sortedBy { it.precio }
                SortOption.PRICE_DESC -> filtered.sortedByDescending { it.precio }
            }

            _uiState.update { it.copy(listaFiltrada = sorted) }
        }
    }

    private fun filterList() {
        filterAndSortList()
    }
}