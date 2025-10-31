package com.example.segundoentregable.ui.list

import androidx.lifecycle.ViewModel
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.data.repository.FakeAttractionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AttractionListUiState(
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val categoriasDisponibles: List<String> = emptyList(),
    val listaFiltrada: List<AtractivoTuristico> = emptyList()
)

class AttractionListViewModel : ViewModel() {

    private val repo = FakeAttractionRepository

    private val _uiState = MutableStateFlow(AttractionListUiState())
    val uiState: StateFlow<AttractionListUiState> = _uiState.asStateFlow()

    private val todosLosAtractivos = repo.getTodosLosAtractivos()

    init {
        _uiState.update {
            it.copy(
                listaFiltrada = todosLosAtractivos,
                // Simulamos obtener las categorías del repo
                categoriasDisponibles = todosLosAtractivos.map { a -> a.categoria }.distinct()
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        filterList()
    }

    fun onCategorySelected(category: String?) {
        val newCategory = if (category == _uiState.value.selectedCategory) null else category
        _uiState.update { it.copy(selectedCategory = newCategory) }
        filterList()
    }

    private fun filterList() {
        val state = _uiState.value

        val newList = todosLosAtractivos.filter { atractivo ->
            val matchQuery = atractivo.nombre.contains(state.searchQuery, ignoreCase = true)

            val matchCategory = if (state.selectedCategory != null) {
                atractivo.categoria == state.selectedCategory
            } else {
                true
            }

            matchQuery && matchCategory
        }

        _uiState.update { it.copy(listaFiltrada = newList) }
    }
}