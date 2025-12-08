package com.example.segundoentregable.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad de atractivo turístico con índices optimizados para consultas frecuentes.
 */
@Entity(
    tableName = "atractivos",
    indices = [
        Index(value = ["categoria"]),
        Index(value = ["rating"]),
        Index(value = ["precio"]),
        Index(value = ["nombre"])
    ]
)
data class AtractivoEntity(
    @PrimaryKey val id: String,
    val codigoMincetur: String,
    val nombre: String,
    val descripcionCorta: String,
    val descripcionLarga: String,
    val ubicacion: String,
    val latitud: Double,
    val longitud: Double,
    val departamento: String,
    val provincia: String,
    val distrito: String,
    val altitud: Int,
    val categoria: String,
    val tipo: String,
    val subtipo: String,
    val jerarquia: Int,
    val precio: Double,
    val precioDetalle: String,
    val horario: String,
    val horarioDetallado: String,
    val epocaVisita: String,
    val tiempoVisitaSugerido: String,
    val estadoActual: String,
    val observaciones: String,
    val tieneAccesibilidad: Boolean,
    val imagenPrincipal: String,
    val rating: Float,
    val distanciaTexto: String
)
