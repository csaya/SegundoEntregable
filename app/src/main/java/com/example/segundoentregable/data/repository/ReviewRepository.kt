package com.example.segundoentregable.data.repository

import android.content.Context
import android.util.Log
import com.example.segundoentregable.data.firebase.FirestoreReviewService
import com.example.segundoentregable.data.local.dao.ReviewDao
import com.example.segundoentregable.data.local.dao.ReviewVoteDao
import com.example.segundoentregable.data.local.entity.ReviewEntity
import com.example.segundoentregable.data.local.entity.ReviewVoteEntity
import com.example.segundoentregable.data.model.Review
import com.example.segundoentregable.data.sync.ReviewSyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "ReviewRepository"

class ReviewRepository(
    private val reviewDao: ReviewDao,
    private val reviewVoteDao: ReviewVoteDao,
    private val context: Context
) {
    private val firestoreService = FirestoreReviewService()

    /**
     * Obtener reviews de un atractivo
     */
    suspend fun getReviewsForAttraction(attractionId: String): List<Review> {
        return reviewDao.getReviewsByAttraction(attractionId).map { it.toModel() }
    }

    /**
     * Calcular rating promedio
     */
    suspend fun calculateAverageRating(attractionId: String): Float {
        return withContext(Dispatchers.IO) {
            val reviews = reviewDao.getReviewsByAttraction(attractionId)
            if (reviews.isEmpty()) {
                5.0f
            } else {
                reviews.map { it.rating }.average().toFloat()
            }
        }
    }

    /**
     * Agregar nueva review
     */
    suspend fun addReview(
        attractionId: String,
        userEmail: String?,
        userName: String,
        rating: Float,
        comment: String
    ) {
        if (userEmail == null) return
        val newReview = ReviewEntity(
            id = UUID.randomUUID().toString(),
            attractionId = attractionId,
            userName = userName,
            userEmail = userEmail,
            date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
            rating = rating,
            comment = comment,
            likes = 0,
            dislikes = 0,
            isSynced = false,
            createdAt = System.currentTimeMillis()
        )

        // 1. Guardar localmente
        reviewDao.insertReview(newReview)

        // 2. ✅ Sincronizar en segundo plano sin esperar
        CoroutineScope(Dispatchers.IO).launch {
            try {
                firestoreService.uploadReviews(listOf(newReview))
                reviewDao.markAsSynced(newReview.id)
                Log.d(TAG, "Review sincronizada inmediatamente: ${newReview.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error sincronizando review inmediatamente, se reintentará", e)
            }
        }

        // 3. Disparar WorkManager para sincronización posterior
        ReviewSyncWorker.syncNow(context)
    }

    /**
     * Actualizar review existente
     */
    suspend fun updateReview(reviewId: String, rating: Float, comment: String) {
        reviewDao.updateReview(reviewId, rating, comment)

        // ✅ Sincronizar cambios
        ReviewSyncWorker.syncNow(context)
    }

    /**
     * Eliminar review
     */
    suspend fun deleteReview(reviewId: String) {
        reviewDao.deleteReview(reviewId)

        // ✅ Sincronizar eliminación
        withContext(Dispatchers.IO) {
            try {
                firestoreService.deleteReview(reviewId)
                Log.d(TAG, "Review eliminada de Firebase: $reviewId")
            } catch (e: Exception) {
                Log.e(TAG, "Error eliminando review de Firebase", e)
            }
        }
    }

    // ========== VOTOS ==========

    suspend fun getUserVote(reviewId: String, userEmail: String): Boolean? {
        return reviewVoteDao.getUserVoteType(reviewId, userEmail)
    }

    suspend fun toggleLikeReview(reviewId: String, userEmail: String): Boolean? {
        val currentVote = reviewVoteDao.getVote(reviewId, userEmail)

        return when {
            currentVote?.isLike == true -> {
                reviewVoteDao.deleteVote(reviewId, userEmail)
                reviewDao.decrementLikes(reviewId)
                false
            }
            currentVote?.isLike == false -> {
                reviewVoteDao.insertVote(ReviewVoteEntity(reviewId, userEmail, isLike = true))
                reviewDao.decrementDislikes(reviewId)
                reviewDao.incrementLikes(reviewId)
                true
            }
            else -> {
                reviewVoteDao.insertVote(ReviewVoteEntity(reviewId, userEmail, isLike = true))
                reviewDao.incrementLikes(reviewId)
                true
            }
        }
    }

    suspend fun toggleDislikeReview(reviewId: String, userEmail: String): Boolean? {
        val currentVote = reviewVoteDao.getVote(reviewId, userEmail)

        return when {
            currentVote?.isLike == false -> {
                reviewVoteDao.deleteVote(reviewId, userEmail)
                reviewDao.decrementDislikes(reviewId)
                false
            }
            currentVote?.isLike == true -> {
                reviewVoteDao.insertVote(ReviewVoteEntity(reviewId, userEmail, isLike = false))
                reviewDao.decrementLikes(reviewId)
                reviewDao.incrementDislikes(reviewId)
                true
            }
            else -> {
                reviewVoteDao.insertVote(ReviewVoteEntity(reviewId, userEmail, isLike = false))
                reviewDao.incrementDislikes(reviewId)
                true
            }
        }
    }

    private fun ReviewEntity.toModel(): Review {
        return Review(
            id = id,
            userName = userName,
            userEmail = userEmail,
            date = date,
            rating = rating,
            comment = comment,
            likes = likes,
            dislikes = dislikes,
            createdAt = createdAt
        )
    }
}
