package com.example.segundoentregable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.segundoentregable.data.local.entity.ReviewVoteEntity

@Dao
interface ReviewVoteDao {
    
    @Query("SELECT * FROM review_votes WHERE reviewId = :reviewId AND userEmail = :userEmail")
    suspend fun getVote(reviewId: String, userEmail: String): ReviewVoteEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVote(vote: ReviewVoteEntity)
    
    @Query("DELETE FROM review_votes WHERE reviewId = :reviewId AND userEmail = :userEmail")
    suspend fun deleteVote(reviewId: String, userEmail: String)
    
    @Query("SELECT isLike FROM review_votes WHERE reviewId = :reviewId AND userEmail = :userEmail")
    suspend fun getUserVoteType(reviewId: String, userEmail: String): Boolean?
    
    @Query("SELECT COUNT(*) FROM review_votes WHERE reviewId = :reviewId AND isLike = 1")
    suspend fun getLikeCount(reviewId: String): Int
    
    @Query("SELECT COUNT(*) FROM review_votes WHERE reviewId = :reviewId AND isLike = 0")
    suspend fun getDislikeCount(reviewId: String): Int
}
