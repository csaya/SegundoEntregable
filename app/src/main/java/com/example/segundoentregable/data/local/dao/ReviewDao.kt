package com.example.segundoentregable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.segundoentregable.data.local.entity.ReviewEntity

@Dao
interface ReviewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<ReviewEntity>)

    @Query("SELECT * FROM reviews WHERE attractionId = :attractionId ORDER BY createdAt DESC")
    suspend fun getReviewsByAttraction(attractionId: String): List<ReviewEntity>

    @Query("SELECT * FROM reviews WHERE id = :id")
    suspend fun getReviewById(id: String): ReviewEntity?

    @Query("DELETE FROM reviews WHERE attractionId = :attractionId")
    suspend fun deleteReviewsByAttraction(attractionId: String)
    
    // Métodos para sincronización
    @Query("SELECT * FROM reviews WHERE isSynced = 0")
    suspend fun getUnsyncedReviews(): List<ReviewEntity>
    
    @Query("UPDATE reviews SET isSynced = 1 WHERE id = :reviewId")
    suspend fun markAsSynced(reviewId: String)
    
    @Query("UPDATE reviews SET isSynced = 1 WHERE id IN (:reviewIds)")
    suspend fun markMultipleAsSynced(reviewIds: List<String>)
    
    @Query("SELECT COUNT(*) FROM reviews WHERE isSynced = 0")
    suspend fun getUnsyncedCount(): Int
    
    // Métodos para calificar reseñas
    @Query("UPDATE reviews SET likes = likes + 1 WHERE id = :reviewId")
    suspend fun incrementLikes(reviewId: String)
    
    @Query("UPDATE reviews SET dislikes = dislikes + 1 WHERE id = :reviewId")
    suspend fun incrementDislikes(reviewId: String)
    
    // Métodos para editar/eliminar reseñas
    @Query("UPDATE reviews SET rating = :rating, comment = :comment, isSynced = 0 WHERE id = :reviewId")
    suspend fun updateReview(reviewId: String, rating: Float, comment: String)
    
    @Query("DELETE FROM reviews WHERE id = :reviewId")
    suspend fun deleteReview(reviewId: String)
}
