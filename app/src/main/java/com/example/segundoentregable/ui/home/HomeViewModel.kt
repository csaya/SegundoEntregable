package com.example.segundoentregable.ui.home

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

data class HomeUiState(
    val recomendaciones: List<AtractivoTuristico> = emptyList(),
    val cercanos: List<AtractivoTuristico> = emptyList(),
    val isLoading: Boolean = false
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AttractionRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        cargarDatosDeInicio()
    }

    private fun cargarDatosDeInicio() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                withContext(Dispatchers.IO) {
                    repo.initializeData()
                }
                val recomendaciones = withContext(Dispatchers.IO) {
                    repo.getRecomendaciones()
                }
                val cercanos = withContext(Dispatchers.IO) {
                    repo.getCercanos()
                }
                _uiState.update {
                    it.copy(
                        recomendaciones = recomendaciones,
                        cercanos = cercanos,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        // Lógica de búsqueda
    }
}