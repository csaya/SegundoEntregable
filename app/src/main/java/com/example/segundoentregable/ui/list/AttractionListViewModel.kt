package com.example.segundoentregable.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.data.repository.FakeAttractionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado de la UI: Guarda la búsqueda, los filtros y la lista resultante
data class AttractionListUiState(
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    // (Por ahora no usaremos distancia/precio para simplificar la lógica falsa)
    val categoriasDisponibles: List<String> = emptyList(),
    val listaFiltrada: List<AtractivoTuristico> = emptyList()
)

class AttractionListViewModel : ViewModel() {

    private val repo = FakeAttractionRepository

    // Estado interno
    private val _uiState = MutableStateFlow(AttractionListUiState())
    val uiState: StateFlow<AttractionListUiState> = _uiState.asStateFlow()

    // Lista completa de atractivos (nuestra "base de datos")
    private val todosLosAtractivos = repo.getTodosLosAtractivos()

    init {
        // Cargar los datos iniciales
        _uiState.update {
            it.copy(
                listaFiltrada = todosLosAtractivos,
                // Simulamos obtener las categorías del repo
                categoriasDisponibles = todosLosAtractivos.map { a -> a.categoria }.distinct()
            )
        }
    }

    // --- LÓGICA DE CRITERIO 2 ---

    // Llamado cuando el usuario escribe en la barra de búsqueda
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        filterList()
    }

    // Llamado cuando el usuario selecciona un filtro de categoría
    fun onCategorySelected(category: String?) {
        val newCategory = if (category == _uiState.value.selectedCategory) null else category
        _uiState.update { it.copy(selectedCategory = newCategory) }
        filterList()
    }

    // Lógica de filtrado (simulada)
    private fun filterList() {
        val state = _uiState.value

        val newList = todosLosAtractivos.filter { atractivo ->
            // 1. Filtro por Búsqueda (nombre)
            val matchQuery = atractivo.nombre.contains(state.searchQuery, ignoreCase = true)

            // 2. Filtro por Categoría
            val matchCategory = if (state.selectedCategory != null) {
                atractivo.categoria == state.selectedCategory
            } else {
                true // Si no hay filtro, todos coinciden
            }

            // Resultado
            matchQuery && matchCategory
        }

        _uiState.update { it.copy(listaFiltrada = newList) }
    }
}