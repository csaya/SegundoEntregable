package com.example.segundoentregable.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad para almacenar los atractivos que el usuario ha añadido a su ruta personal.
 * Funciona como un "carrito de compras" temporal para planificar visitas.
 */
@Entity(
    tableName = "user_route_items",
    indices = [Index(value = ["atractivoId"], unique = true)]
)
data class UserRouteItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val atractivoId: String,
    val addedAt: Long = System.currentTimeMillis(),
    val orderIndex: Int = 0 // Para mantener el orden después de optimizar
)
