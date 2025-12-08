package com.example.segundoentregable.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
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
    ],
    indices = [
        Index("attractionId"),
        Index("isSynced")
    ]
)
data class ReviewEntity(
    @PrimaryKey
    val id: String,
    val attractionId: String,
    val userName: String,
    val userEmail: String = "",
    val date: String,
    val rating: Float,
    val comment: String,
    val likes: Int = 0,
    val dislikes: Int = 0,
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
