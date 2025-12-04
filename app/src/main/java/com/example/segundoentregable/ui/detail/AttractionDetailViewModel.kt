package com.example.segundoentregable.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.data.model.Review
import com.example.segundoentregable.data.repository.AttractionRepository
import com.example.segundoentregable.data.repository.FavoriteRepository
import com.example.segundoentregable.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DetailUiState(
    val atractivo: AtractivoTuristico? = null,
    val reviews: List<Review> = emptyList(),
    val isFavorito: Boolean = false,
    val isLoading: Boolean = false
)

class AttractionDetailViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val attractionRepo = AttractionRepository(application.applicationContext)
    private val favoriteRepo = FavoriteRepository(application.applicationContext)
    private val userRepo = UserRepository(application.applicationContext)

    private val attractionId: String = checkNotNull(savedStateHandle["attractionId"])

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val atractivo = withContext(Dispatchers.IO) {
                    attractionRepo.getAtractivoPorId(attractionId)
                }
                val reviews = withContext(Dispatchers.IO) {
                    attractionRepo.getReviewsForAttraction(attractionId)
                }
                val userEmail = userRepo.getCurrentUserEmail()
                val isFavorito = if (userEmail != null) {
                    withContext(Dispatchers.IO) {
                        favoriteRepo.isFavorito(userEmail, attractionId)
                    }
                } else {
                    false
                }
                _uiState.update {
                    it.copy(
                        atractivo = atractivo,
                        reviews = reviews,
                        isFavorito = isFavorito,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onToggleFavorite() {
        val userEmail = userRepo.getCurrentUserEmail() ?: return
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    favoriteRepo.toggleFavorito(userEmail, attractionId)
                }
                val isFavorito = withContext(Dispatchers.IO) {
                    favoriteRepo.isFavorito(userEmail, attractionId)
                }
                _uiState.update {
                    it.copy(isFavorito = isFavorito)
                }
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
}