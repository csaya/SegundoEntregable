package com.example.segundoentregable.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "favoritos",
    foreignKeys = [
        // Solo FK a atractivo - usuario viene de Firebase Auth
        ForeignKey(
            entity = AtractivoEntity::class,
            parentColumns = ["id"],
            childColumns = ["attractionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("userEmail"),
        Index("attractionId")
    ]
)
data class FavoritoEntity(
    @PrimaryKey
    val id: String,
    val userEmail: String,
    val attractionId: String
)
