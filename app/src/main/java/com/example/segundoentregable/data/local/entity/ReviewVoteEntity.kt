package com.example.segundoentregable.data.local.entity

import androidx.room.Entity
import androidx.room.Index

/**
 * Entidad para trackear los votos de usuarios en reseñas.
 * Un usuario solo puede votar una vez por reseña (like O dislike, no ambos).
 */
@Entity(
    tableName = "review_votes",
    primaryKeys = ["reviewId", "userEmail"],
    indices = [Index("userEmail"), Index("reviewId")]
)
data class ReviewVoteEntity(
    val reviewId: String,
    val userEmail: String,
    val isLike: Boolean,  // true = like, false = dislike
    val votedAt: Long = System.currentTimeMillis()
)
