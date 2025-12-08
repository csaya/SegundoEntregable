package com.example.segundoentregable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.segundoentregable.data.local.entity.AtractivoEntity

@Dao
interface AtractivoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAtractivo(atractivo: AtractivoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAtractivos(atractivos: List<AtractivoEntity>)

    @Query("SELECT * FROM atractivos WHERE id = :id")
    suspend fun getAtractivoById(id: String): AtractivoEntity?

    @Query("SELECT * FROM atractivos")
    suspend fun getAllAtractivos(): List<AtractivoEntity>

    @Query("SELECT * FROM atractivos WHERE nombre LIKE '%' || :query || '%'")
    suspend fun searchAtractivos(query: String): List<AtractivoEntity>

    @Query("SELECT * FROM atractivos WHERE categoria = :categoria")
    suspend fun getAtractivosByCategoria(categoria: String): List<AtractivoEntity>

    @Query("DELETE FROM atractivos")
    suspend fun deleteAllAtractivos()

    @Query("SELECT COUNT(*) FROM atractivos")
    suspend fun getCount(): Int
}
