package com.example.segundoentregable.data.local.entity

import androidx.room.*

@Entity(
    tableName = "actividades",
    foreignKeys = [ForeignKey(
        entity = AtractivoEntity::class,
        parentColumns = ["id"],
        childColumns = ["atractivoId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("atractivoId")]
)
data class ActividadEntity(
    @PrimaryKey val id: String,
    val atractivoId: String,
    val nombre: String,
    val categoria: String,
    val icono: String
)
