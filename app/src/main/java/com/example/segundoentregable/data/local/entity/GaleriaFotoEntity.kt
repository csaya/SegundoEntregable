package com.example.segundoentregable.data.local.entity

import androidx.room.*

@Entity(
    tableName = "galeria_fotos",
    foreignKeys = [ForeignKey(
        entity = AtractivoEntity::class,
        parentColumns = ["id"],
        childColumns = ["atractivoId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("atractivoId")]
)
data class GaleriaFotoEntity(
    @PrimaryKey val id: String,
    val atractivoId: String,
    val urlFoto: String,
    val orden: Int
)
