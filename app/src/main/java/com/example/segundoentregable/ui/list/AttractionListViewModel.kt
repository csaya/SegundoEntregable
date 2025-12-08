package com.example.segundoentregable.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.data.repository.AttractionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AttractionListUiState(
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val categoriasDisponibles: List<String> = emptyList(),
    val listaFiltrada: List<AtractivoTuristico> = emptyList(),
    val isLoading: Boolean = false
)

class AttractionListViewModel(
    private val repo: AttractionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AttractionListUiState())
    val uiState: StateFlow<AttractionListUiState> = _uiState.asStateFlow()

    private var todosLosAtractivos: List<AtractivoTuristico> = emptyList()

    init {
        loadAtractivos()
    }

    private fun loadAtractivos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Los datos se cargan desde DataImporter en AppApplication
                // Cargamos todos los datos
                todosLosAtractivos = withContext(Dispatchers.IO) {
                    repo.getTodosLosAtractivos()
                }

                // Actualizamos la UI
                _uiState.update {
                    it.copy(
                        listaFiltrada = todosLosAtractivos,
                        // Extraemos las categorías únicas dinámicamente
                        categoriasDisponibles = todosLosAtractivos.map { a -> a.categoria }.distinct().sorted(),
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
        _uiState.update { it.copy(searchQuery = query) }
        filterList()
    }

    fun onCategorySelected(category: String?) {
        // Si tocan la misma categoría, la deseleccionamos (toggle)
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