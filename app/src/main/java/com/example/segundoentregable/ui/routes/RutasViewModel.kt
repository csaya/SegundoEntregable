package com.example.segundoentregable.ui.routes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.segundoentregable.AppApplication
import com.example.segundoentregable.data.local.entity.RutaEntity
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.data.repository.RutaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "RutasViewModel"

data class RutasUiState(
    val rutas: List<RutaEntity> = emptyList(),
    val categorias: List<String> = emptyList(),
    val selectedCategoria: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

data class RutaDetalleUiState(
    val ruta: RutaEntity? = null,
    val atractivos: List<AtractivoTuristico> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class RutasViewModel(
    private val rutaRepository: RutaRepository,
    private val isDataReadyFlow: StateFlow<Boolean>
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RutasUiState())
    val uiState: StateFlow<RutasUiState> = _uiState.asStateFlow()
    
    private val _detalleState = MutableStateFlow(RutaDetalleUiState())
    val detalleState: StateFlow<RutaDetalleUiState> = _detalleState.asStateFlow()
    
    init {
        esperarDatosYCargar()
    }

    private fun esperarDatosYCargar() {
        viewModelScope.launch {
            Log.d(TAG, "Esperando a que los datos estén listos...")
            isDataReadyFlow.first { it }
            Log.d(TAG, "Datos listos, cargando rutas...")
            loadRutas()
        }
    }
    
    private suspend fun loadRutas() {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val rutas = rutaRepository.getAllRutasList()
            val categorias = rutaRepository.getCategorias()
            Log.d(TAG, "Cargadas ${rutas.size} rutas")
            _uiState.update { 
                it.copy(
                    rutas = rutas,
                    categorias = categorias,
                    isLoading = false
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cargando rutas", e)
            _uiState.update { 
                it.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }
    
    fun onCategoriaSelected(categoria: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedCategoria = categoria, isLoading = true) }
            try {
                val rutas = if (categoria == null) {
                    rutaRepository.getAllRutasList()
                } else {
                    rutaRepository.getRutasByCategoria(categoria)
                }
                _uiState.update { it.copy(rutas = rutas, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
    
    fun loadRutaDetalle(rutaId: String) {
        viewModelScope.launch {
            _detalleState.update { it.copy(isLoading = true) }
            try {
                val ruta = rutaRepository.getRutaById(rutaId)
                val atractivos = rutaRepository.getAtractivosByRuta(rutaId)
                _detalleState.update {
                    it.copy(
                        ruta = ruta,
                        atractivos = atractivos,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _detalleState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}

class RutasViewModelFactory(
    private val app: AppApplication
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RutasViewModel::class.java)) {
            return RutasViewModel(
                rutaRepository = app.rutaRepository,
                isDataReadyFlow = app.isDataReady
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
