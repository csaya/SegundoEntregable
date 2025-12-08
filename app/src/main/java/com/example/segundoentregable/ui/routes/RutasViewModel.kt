package com.example.segundoentregable.ui.routes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.segundoentregable.data.local.AppDatabase
import com.example.segundoentregable.data.local.entity.RutaEntity
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.data.repository.RutaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

class RutasViewModel(application: Application) : AndroidViewModel(application) {
    
    private val rutaRepository: RutaRepository
    
    private val _uiState = MutableStateFlow(RutasUiState())
    val uiState: StateFlow<RutasUiState> = _uiState.asStateFlow()
    
    private val _detalleState = MutableStateFlow(RutaDetalleUiState())
    val detalleState: StateFlow<RutaDetalleUiState> = _detalleState.asStateFlow()
    
    init {
        val database = AppDatabase.getInstance(application)
        rutaRepository = RutaRepository(database.rutaDao())
        loadRutas()
    }
    
    private fun loadRutas() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val rutas = rutaRepository.getAllRutasList()
                val categorias = rutaRepository.getCategorias()
                _uiState.update { 
                    it.copy(
                        rutas = rutas,
                        categorias = categorias,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
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
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RutasViewModel::class.java)) {
            return RutasViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
