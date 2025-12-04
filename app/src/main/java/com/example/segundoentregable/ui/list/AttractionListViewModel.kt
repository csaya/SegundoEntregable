package com.example.segundoentregable.ui.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
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

class AttractionListViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AttractionRepository(application.applicationContext)

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
                withContext(Dispatchers.IO) {
                    repo.initializeData()
                }
                todosLosAtractivos = withContext(Dispatchers.IO) {
                    repo.getTodosLosAtractivos()
                }
                _uiState.update {
                    it.copy(
                        listaFiltrada = todosLosAtractivos,
                        categoriasDisponibles = todosLosAtractivos.map { a -> a.categoria }.distinct(),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
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