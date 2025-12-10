package com.example.segundoentregable.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad para rutas personalizadas guardadas por el usuario.
 * Permite guardar rutas con nombre para cargarlas posteriormente.
 */
@Entity(
    tableName = "saved_routes",
    indices = [Index("createdAt")]
)
data class SavedRouteEntity(
    @PrimaryKey
    val id: String,
    val nombre: String,
    val descripcion: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val totalDistance: Float = 0f,
    val estimatedTime: Int = 0,
    val itemCount: Int = 0
)
