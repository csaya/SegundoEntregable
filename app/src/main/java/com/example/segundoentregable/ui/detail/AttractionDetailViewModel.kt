package com.example.segundoentregable.ui.detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
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

private const val TAG = "DetailViewModel"

data class DetailUiState(
    val atractivo: AtractivoTuristico? = null,
    val reviews: List<Review> = emptyList(),
    val isFavorito: Boolean = false,
    val isLoading: Boolean = false,
    val isReviewDialogVisible: Boolean = false,
    val isSubmittingReview: Boolean = false
)

// Ahora hereda de ViewModel (no AndroidViewModel) porque ya inyectamos los repos
class AttractionDetailViewModel(
    private val attractionRepo: AttractionRepository,
    private val favoriteRepo: FavoriteRepository,
    private val userRepo: UserRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

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
                val userEmail = userRepo.getCurrentUserEmail() ?: "guest_user"

                // Cargar atractivo completo con galería, actividades y estado de favorito
                val atractivo = withContext(Dispatchers.IO) {
                    attractionRepo.getAtractivoCompletoSync(attractionId, userEmail)
                }
                val reviews = withContext(Dispatchers.IO) {
                    attractionRepo.getReviewsForAttraction(attractionId)
                }

                _uiState.update {
                    it.copy(
                        atractivo = atractivo,
                        reviews = reviews,
                        isFavorito = atractivo?.isFavorito ?: false,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando datos", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onToggleFavorite() {
        viewModelScope.launch {
            val userEmail = userRepo.getCurrentUserEmail() ?: "guest_user"

            try {
                // 1. Ejecutar el cambio en base de datos
                favoriteRepo.toggleFavorito(userEmail, attractionId)

                // 2. Leer el nuevo estado
                val nuevoEstado = favoriteRepo.isFavorito(userEmail, attractionId)

                // 3. Actualizar la UI
                _uiState.update { it.copy(isFavorito = nuevoEstado) }

                Log.d(TAG, "Favorito actualizado: $nuevoEstado para $attractionId")

            } catch (e: Exception) {
                Log.e(TAG, "Error al cambiar favorito", e)
            }
        }
    }

    fun showReviewDialog() {
        _uiState.update { it.copy(isReviewDialogVisible = true) }
    }

    fun hideReviewDialog() {
        _uiState.update { it.copy(isReviewDialogVisible = false) }
    }

    fun submitReview(rating: Float, comment: String) {
        viewModelScope.launch {
            val userEmail = userRepo.getCurrentUserEmail() ?: return@launch
            // Obtenemos el nombre del usuario (idealmente esto vendría del objeto User,
            // aquí simulamos una llamada rápida o usamos el email si no hay nombre cargado)
            val user = userRepo.getUser(userEmail)
            val userName = user?.name ?: "Usuario"

            _uiState.update { it.copy(isSubmittingReview = true) }

            try {
                // Guardamos en BD
                attractionRepo.addReview(
                    attractionId = attractionId,
                    userEmail = userEmail, // Aunque ReviewEntity no guarda email, lo usamos para lógica
                    userName = userName,
                    rating = rating,
                    comment = comment
                )

                // Recargamos los datos para ver la nueva review
                loadData()

                // Cerramos diálogo
                _uiState.update { it.copy(isReviewDialogVisible = false) }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uiState.update { it.copy(isSubmittingReview = false) }
            }
        }
    }
}