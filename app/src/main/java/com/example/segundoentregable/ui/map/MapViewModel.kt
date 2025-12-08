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
    val atractivos: List<AtractivoTuristico> = emptyList(),
    val selectedAttraction: AtractivoTuristico? = null,
    val isLoading: Boolean = true // Iniciar en true
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
                it.copy(atractivos = atractivos, isLoading = false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cargando atractivos", e)
            _uiState.update { it.copy(isLoading = false) }
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