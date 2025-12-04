package com.example.segundoentregable.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "reviews",
    foreignKeys = [
        ForeignKey(
            entity = AtractivoEntity::class,
            parentColumns = ["id"],
            childColumns = ["attractionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ReviewEntity(
    @PrimaryKey
    val id: String,
    val attractionId: String,
    val userName: String,
    val date: String,
    val rating: Float,
    val comment: String,
    val likes: Int,
    val dislikes: Int
)
