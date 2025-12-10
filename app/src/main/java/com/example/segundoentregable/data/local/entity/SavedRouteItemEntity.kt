package com.example.segundoentregable.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad para los items de una ruta guardada.
 * Relaciona un atractivo con una ruta guardada, manteniendo el orden.
 */
@Entity(
    tableName = "saved_route_items",
    foreignKeys = [
        ForeignKey(
            entity = SavedRouteEntity::class,
            parentColumns = ["id"],
            childColumns = ["routeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("routeId"),
        Index("atractivoId")
    ]
)
data class SavedRouteItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routeId: String,
    val atractivoId: String,
    val orderIndex: Int = 0
)
