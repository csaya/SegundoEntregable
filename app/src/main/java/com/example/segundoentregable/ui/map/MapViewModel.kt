package com.example.segundoentregable.ui.map

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

data class MapUiState(
    val atractivos: List<AtractivoTuristico> = emptyList(),
    val selectedAttraction: AtractivoTuristico? = null,
    val isLoading: Boolean = false
)

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AttractionRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

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
                val atractivos = withContext(Dispatchers.IO) {
                    repo.getTodosLosAtractivos()
                }
                _uiState.update {
                    it.copy(atractivos = atractivos, isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
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
}