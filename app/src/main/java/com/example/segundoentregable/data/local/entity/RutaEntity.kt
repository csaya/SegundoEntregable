package com.example.segundoentregable.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad unificada para rutas turísticas.
 * Soporta tanto rutas predefinidas (editoriales) como rutas creadas por el usuario.
 * 
 * @property tipo "predefinida" para rutas editoriales, "usuario" para rutas personalizadas
 * @property userId ID del usuario dueño (null para rutas predefinidas)
 */
@Entity(
    tableName = "rutas",
    indices = [
        Index("categoria"),
        Index("tipo"),
        Index("userId")
    ]
)
data class RutaEntity(
    @PrimaryKey
    val id: String,
    val nombre: String,
    val descripcion: String,
    val categoria: String = "",
    val duracionEstimada: String = "",
    val distanciaTotal: Float = 0f,
    val dificultad: String = "",
    val imagenPrincipal: String = "",
    val puntoInicio: String = "",
    val puntoFin: String = "",
    val recomendaciones: String = "",
    val orden: Int = 0,
    // Campos para rutas de usuario
    val tipo: String = "predefinida",  // "predefinida" o "usuario"
    val userId: String? = null,         // Email del usuario dueño
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val tiempoEstimadoMinutos: Int = 0
) {
    companion object {
        const val TIPO_PREDEFINIDA = "predefinida"
        const val TIPO_USUARIO = "usuario"
    }
}
