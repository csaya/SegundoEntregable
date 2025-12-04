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

    @Query("SELECT * FROM reviews WHERE attractionId = :attractionId")
    suspend fun getReviewsByAttraction(attractionId: String): List<ReviewEntity>

    @Query("SELECT * FROM reviews WHERE id = :id")
    suspend fun getReviewById(id: String): ReviewEntity?

    @Query("DELETE FROM reviews WHERE attractionId = :attractionId")
    suspend fun deleteReviewsByAttraction(attractionId: String)
}
