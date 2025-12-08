package com.example.segundoentregable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.segundoentregable.data.local.entity.ActividadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActividadDao {

    @Query("SELECT * FROM actividades WHERE atractivoId = :atractivoId")
    fun getActividadesByAtractivoId(atractivoId: String): Flow<List<ActividadEntity>>

    @Query("SELECT * FROM actividades WHERE atractivoId = :atractivoId")
    suspend fun getActividadesByAtractivoIdSync(atractivoId: String): List<ActividadEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(actividades: List<ActividadEntity>)

    @Query("DELETE FROM actividades")
    suspend fun deleteAll()
}
