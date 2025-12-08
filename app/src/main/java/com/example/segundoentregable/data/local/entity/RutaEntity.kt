package com.example.segundoentregable.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad para rutas turísticas curadas.
 * Representa rutas predefinidas como "Ruta del Sillar", "Ruta Centro Histórico", etc.
 */
@Entity(
    tableName = "rutas",
    indices = [Index("categoria")]
)
data class RutaEntity(
    @PrimaryKey
    val id: String,
    val nombre: String,
    val descripcion: String,
    val categoria: String,           // "cultural", "gastronomica", "naturaleza", etc.
    val duracionEstimada: String,    // "3-4 horas", "Medio día", etc.
    val distanciaTotal: Float,       // En kilómetros
    val dificultad: String,          // "facil", "moderada", "dificil"
    val imagenPrincipal: String,
    val puntoInicio: String,         // Nombre del punto de inicio
    val puntoFin: String,            // Nombre del punto final
    val recomendaciones: String,     // Tips para el turista
    val orden: Int = 0               // Para ordenar en la lista
)
