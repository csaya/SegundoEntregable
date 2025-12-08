package com.example.segundoentregable.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad para las paradas de una ruta.
 * Relaciona una ruta con los atractivos que la componen, en orden.
 */
@Entity(
    tableName = "ruta_paradas",
    foreignKeys = [
        ForeignKey(
            entity = RutaEntity::class,
            parentColumns = ["id"],
            childColumns = ["rutaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AtractivoEntity::class,
            parentColumns = ["id"],
            childColumns = ["atractivoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("rutaId"),
        Index("atractivoId")
    ]
)
data class RutaParadaEntity(
    @PrimaryKey
    val id: String,
    val rutaId: String,
    val atractivoId: String,
    val orden: Int,                  // Orden de la parada en la ruta
    val tiempoSugerido: String,      // "30 min", "1 hora", etc.
    val notas: String = ""           // Notas específicas para esta parada
)
