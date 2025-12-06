package com.example.segundoentregable.ui.home

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

data class HomeUiState(
    val recomendaciones: List<AtractivoTuristico> = emptyList(),
    val cercanos: List<AtractivoTuristico> = emptyList(),
    val isLoading: Boolean = false
)

class HomeViewModel(
    private val repo: AttractionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        cargarDatosDeInicio()
    }

    private fun cargarDatosDeInicio() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Aseguramos que haya datos iniciales (Data Seeding)
                // Usamos Dispatchers.IO para no bloquear la UI
                withContext(Dispatchers.IO) {
                    repo.initializeData()
                }

                // Cargamos las listas usando las funciones del repositorio
                val recomendaciones = repo.getRecomendaciones() // Ya son suspend functions
                val cercanos = repo.getCercanos()               // Ya son suspend functions

                _uiState.update {
                    it.copy(
                        recomendaciones = recomendaciones,
                        cercanos = cercanos,
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
        // Lógica futura para búsqueda
    }
}