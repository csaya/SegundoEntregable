package com.example.segundoentregable.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "atractivos")
data class AtractivoEntity(
    @PrimaryKey
    val id: String,
    val nombre: String,
    val descripcionCorta: String,
    val descripcionLarga: String,
    val ubicacion: String,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val categoria: String,
    val precio: Double = 0.0,
    val horario: String = "",
    val rating: Float = 5.0f,
    val idImagen: String,
    val distanciaTexto: String = ""
)
