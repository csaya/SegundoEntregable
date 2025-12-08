package com.example.segundoentregable.data.firebase

import com.example.segundoentregable.data.local.entity.ReviewEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/**
 * Servicio para sincronizar reseñas con Firestore.
 * 
 * Estructura en Firestore:
 * reviews/
 *   {reviewId}/
 *     - id: String
 *     - attractionId: String
 *     - userName: String
 *     - userEmail: String
 *     - date: String
 *     - rating: Float
 *     - comment: String
 *     - likes: Int
 *     - dislikes: Int
 *     - createdAt: Long
 */
class FirestoreReviewService {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val reviewsCollection = firestore.collection("reviews")
    
    /**
     * Subir una reseña a Firestore
     */
    suspend fun uploadReview(review: ReviewEntity): Result<Unit> {
        return try {
            val reviewData = hashMapOf(
                "id" to review.id,
                "attractionId" to review.attractionId,
                "userName" to review.userName,
                "userEmail" to review.userEmail,
                "date" to review.date,
                "rating" to review.rating,
                "comment" to review.comment,
                "likes" to review.likes,
                "dislikes" to review.dislikes,
                "createdAt" to review.createdAt
            )
            
            reviewsCollection.document(review.id).set(reviewData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Subir múltiples reseñas en batch
     */
    suspend fun uploadReviews(reviews: List<ReviewEntity>): Result<List<String>> {
        if (reviews.isEmpty()) return Result.success(emptyList())
        
        return try {
            val batch = firestore.batch()
            val successIds = mutableListOf<String>()
            
            reviews.forEach { review ->
                val docRef = reviewsCollection.document(review.id)
                val reviewData = hashMapOf(
                    "id" to review.id,
                    "attractionId" to review.attractionId,
                    "userName" to review.userName,
                    "userEmail" to review.userEmail,
                    "date" to review.date,
                    "rating" to review.rating,
                    "comment" to review.comment,
                    "likes" to review.likes,
                    "dislikes" to review.dislikes,
                    "createdAt" to review.createdAt
                )
                batch.set(docRef, reviewData)
                successIds.add(review.id)
            }
            
            batch.commit().await()
            Result.success(successIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtener reseñas de un atractivo desde Firestore
     */
    suspend fun getReviewsForAttraction(attractionId: String): Result<List<ReviewEntity>> {
        return try {
            val snapshot = reviewsCollection
                .whereEqualTo("attractionId", attractionId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val reviews = snapshot.documents.mapNotNull { doc ->
                try {
                    ReviewEntity(
                        id = doc.getString("id") ?: return@mapNotNull null,
                        attractionId = doc.getString("attractionId") ?: return@mapNotNull null,
                        userName = doc.getString("userName") ?: "",
                        userEmail = doc.getString("userEmail") ?: "",
                        date = doc.getString("date") ?: "",
                        rating = doc.getDouble("rating")?.toFloat() ?: 0f,
                        comment = doc.getString("comment") ?: "",
                        likes = doc.getLong("likes")?.toInt() ?: 0,
                        dislikes = doc.getLong("dislikes")?.toInt() ?: 0,
                        isSynced = true,
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    null
                }
            }
            
            Result.success(reviews)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtener todas las reseñas recientes (para sincronización inicial)
     */
    suspend fun getAllRecentReviews(limit: Long = 100): Result<List<ReviewEntity>> {
        return try {
            val snapshot = reviewsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            
            val reviews = snapshot.documents.mapNotNull { doc ->
                try {
                    ReviewEntity(
                        id = doc.getString("id") ?: return@mapNotNull null,
                        attractionId = doc.getString("attractionId") ?: return@mapNotNull null,
                        userName = doc.getString("userName") ?: "",
                        userEmail = doc.getString("userEmail") ?: "",
                        date = doc.getString("date") ?: "",
                        rating = doc.getDouble("rating")?.toFloat() ?: 0f,
                        comment = doc.getString("comment") ?: "",
                        likes = doc.getLong("likes")?.toInt() ?: 0,
                        dislikes = doc.getLong("dislikes")?.toInt() ?: 0,
                        isSynced = true,
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    null
                }
            }
            
            Result.success(reviews)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
