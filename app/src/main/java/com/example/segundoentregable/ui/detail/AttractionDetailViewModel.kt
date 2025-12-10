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
import com.example.segundoentregable.data.repository.UserRouteRepository
import com.example.segundoentregable.data.repository.ReviewRepository
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
    val isInRoute: Boolean = false,
    val isLoading: Boolean = false,
    val isReviewDialogVisible: Boolean = false,
    val isSubmittingReview: Boolean = false,
    val isLoggedIn: Boolean = false,
    val requiresLogin: Boolean = false,
    val currentUserEmail: String? = null,
    // Para edición/eliminación de reseñas
    val editingReview: Review? = null,
    val showDeleteConfirmation: Review? = null
)

// Ahora hereda de ViewModel (no AndroidViewModel) porque ya inyectamos los repos
class AttractionDetailViewModel(
    private val attractionRepo: AttractionRepository,
    private val favoriteRepo: FavoriteRepository,
    private val userRepo: UserRepository,
    private val userRouteRepo: UserRouteRepository,
    private val reviewRepo: ReviewRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val attractionId: String = checkNotNull(savedStateHandle["attractionId"])

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    // Evento único para mostrar Snackbar
    private val _snackbarEvent = MutableStateFlow<String?>(null)
    val snackbarEvent: StateFlow<String?> = _snackbarEvent.asStateFlow()

    init {
        loadData()
        observeRouteStatus()
    }

    private fun observeRouteStatus() {
        viewModelScope.launch {
            userRouteRepo.isInRouteFlow(attractionId).collect { isInRoute ->
                _uiState.update { it.copy(isInRoute = isInRoute) }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val userEmail = userRepo.getCurrentUserEmail()
                val isLoggedIn = userEmail != null

                // Cargar atractivo completo con galería, actividades y estado de favorito
                val atractivo = withContext(Dispatchers.IO) {
                    attractionRepo.getAtractivoCompletoSync(attractionId, userEmail ?: "")
                }
                val reviews = withContext(Dispatchers.IO) {
                    attractionRepo.getReviewsForAttraction(attractionId)
                }

                // Solo cargar favorito si está logueado
                val isFavorito = if (isLoggedIn) {
                    withContext(Dispatchers.IO) {
                        favoriteRepo.isFavorito(userEmail!!, attractionId)
                    }
                } else false

                _uiState.update {
                    it.copy(
                        atractivo = atractivo,
                        reviews = reviews,
                        isFavorito = isFavorito,
                        isLoggedIn = isLoggedIn,
                        isLoading = false,
                        currentUserEmail = userEmail
                    )
                }

                Log.d(TAG, "Current user email: $userEmail")
                reviews.forEach { review ->
                    Log.d(TAG, "Review by: ${review.userEmail}, Match: ${review.userEmail == userEmail}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando datos", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onToggleFavorite() {
        viewModelScope.launch {
            val userEmail = userRepo.getCurrentUserEmail()
            
            // Verificar si está logueado
            if (userEmail == null) {
                _uiState.update { it.copy(requiresLogin = true) }
                _snackbarEvent.value = "Inicia sesión para guardar favoritos"
                return@launch
            }

            try {
                // 1. Ejecutar el cambio en base de datos
                favoriteRepo.toggleFavorito(userEmail, attractionId)

                // 2. Leer el nuevo estado
                val nuevoEstado = favoriteRepo.isFavorito(userEmail, attractionId)

                // 3. Actualizar la UI
                _uiState.update { it.copy(isFavorito = nuevoEstado) }
                
                val msg = if (nuevoEstado) "Añadido a favoritos" else "Eliminado de favoritos"
                _snackbarEvent.value = msg

                Log.d(TAG, "Favorito actualizado: $nuevoEstado para $attractionId")

            } catch (e: Exception) {
                Log.e(TAG, "Error al cambiar favorito", e)
                _snackbarEvent.value = "Error al guardar favorito"
            }
        }
    }
    
    fun clearRequiresLogin() {
        _uiState.update { it.copy(requiresLogin = false) }
    }

    /**
     * Añade o quita el atractivo de la ruta del usuario
     */
    fun onToggleRoute() {
        viewModelScope.launch {
            val userEmail = userRepo.getCurrentUserEmail()
            
            // Verificar si está logueado
            if (userEmail == null) {
                _uiState.update { it.copy(requiresLogin = true) }
                _snackbarEvent.value = "Inicia sesión para crear rutas"
                return@launch
            }
            
            try {
                val wasAdded = userRouteRepo.toggleInRoute(attractionId)
                val message = if (wasAdded) "Añadido a tu ruta" else "Quitado de tu ruta"
                _snackbarEvent.value = message
                Log.d(TAG, "Ruta actualizada: $wasAdded para $attractionId")
            } catch (e: Exception) {
                Log.e(TAG, "Error al cambiar ruta", e)
                _snackbarEvent.value = "Error al modificar ruta"
            }
        }
    }

    fun clearSnackbarEvent() {
        _snackbarEvent.value = null
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

            val localUser = userRepo.getUser(userEmail)

            val firebaseDisplayName = userRepo.getFirebaseAuthService().getCurrentUserDisplayName()

            val userName = when {
                !localUser?.name.isNullOrBlank() -> localUser!!.name
                !firebaseDisplayName.isNullOrBlank() -> firebaseDisplayName
                else -> userEmail.split("@").first().replaceFirstChar { it.uppercase() }
            }

            _uiState.update { it.copy(isSubmittingReview = true) }

            try {
                reviewRepo.addReview(
                    attractionId = attractionId,
                    userEmail = userEmail,
                    userName = userName,
                    rating = rating,
                    comment = comment
                )

                val newRating = attractionRepo.calculateAverageRating(attractionId)
                _uiState.update { currentState ->
                    currentState.copy(
                        atractivo = currentState.atractivo?.copy(rating = newRating)
                    )
                }

                loadData()
                _uiState.update { it.copy(isReviewDialogVisible = false) }
                _snackbarEvent.value = "¡Reseña publicada!"
            } catch (e: Exception) {
                e.printStackTrace()
                _snackbarEvent.value = "Error al publicar reseña"
            } finally {
                _uiState.update { it.copy(isSubmittingReview = false) }
            }
        }
    }

    fun onLikeReview(reviewId: String) {
        viewModelScope.launch {
            val userEmail = userRepo.getCurrentUserEmail()
            if (userEmail == null) {
                _snackbarEvent.value = "Inicia sesión para votar"
                return@launch
            }
            
            try {
                val result = withContext(Dispatchers.IO) {
                    reviewRepo.toggleLikeReview(reviewId, userEmail)
                }
                loadData()
                
                when (result) {
                    true -> _snackbarEvent.value = "Te gustó esta reseña"
                    false -> _snackbarEvent.value = "Voto eliminado"
                    null -> _snackbarEvent.value = "Error al votar"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al dar like: ${e.message}")
                _snackbarEvent.value = "Error al votar"
            }
        }
    }
    
    fun onDislikeReview(reviewId: String) {
        viewModelScope.launch {
            val userEmail = userRepo.getCurrentUserEmail()
            if (userEmail == null) {
                _snackbarEvent.value = "Inicia sesión para votar"
                return@launch
            }
            
            try {
                val result = withContext(Dispatchers.IO) {
                    reviewRepo.toggleDislikeReview(reviewId, userEmail)
                }
                loadData()
                
                when (result) {
                    true -> _snackbarEvent.value = "No te gustó esta reseña"
                    false -> _snackbarEvent.value = "Voto eliminado"
                    null -> _snackbarEvent.value = "Error al votar"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al dar dislike: ${e.message}")
                _snackbarEvent.value = "Error al votar"
            }
        }
    }
    
    // ========== EDICIÓN DE RESEÑAS ==========
    
    fun startEditingReview(review: Review) {
        _uiState.update { it.copy(editingReview = review) }
    }
    
    fun cancelEditingReview() {
        _uiState.update { it.copy(editingReview = null) }
    }
    
    fun updateReview(reviewId: String, newRating: Float, newComment: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    reviewRepo.updateReview(reviewId, newRating, newComment)
                }
                _uiState.update { it.copy(editingReview = null) }
                _snackbarEvent.value = "Reseña actualizada"
                loadData()
            } catch (e: Exception) {
                Log.e(TAG, "Error al actualizar reseña: ${e.message}")
                _snackbarEvent.value = "Error al actualizar reseña"
            }
        }
    }
    
    // ========== ELIMINACIÓN DE RESEÑAS ==========
    
    fun showDeleteConfirmation(review: Review) {
        _uiState.update { it.copy(showDeleteConfirmation = review) }
    }
    
    fun cancelDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = null) }
    }
    
    fun confirmDeleteReview(reviewId: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    reviewRepo.deleteReview(reviewId)
                }
                _uiState.update { it.copy(showDeleteConfirmation = null) }
                _snackbarEvent.value = "Reseña eliminada"
                loadData()
            } catch (e: Exception) {
                Log.e(TAG, "Error al eliminar reseña: ${e.message}")
                _snackbarEvent.value = "Error al eliminar reseña"
            }
        }
    }
}