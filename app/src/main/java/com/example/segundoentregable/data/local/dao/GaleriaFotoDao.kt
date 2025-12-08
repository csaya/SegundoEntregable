package com.example.segundoentregable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.segundoentregable.data.local.entity.GaleriaFotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GaleriaFotoDao {

    @Query("SELECT * FROM galeria_fotos WHERE atractivoId = :atractivoId ORDER BY orden ASC")
    fun getGaleriaByAtractivoId(atractivoId: String): Flow<List<GaleriaFotoEntity>>

    @Query("SELECT * FROM galeria_fotos WHERE atractivoId = :atractivoId ORDER BY orden ASC")
    suspend fun getGaleriaByAtractivoIdSync(atractivoId: String): List<GaleriaFotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(galerias: List<GaleriaFotoEntity>)

    @Query("DELETE FROM galeria_fotos")
    suspend fun deleteAll()
}
