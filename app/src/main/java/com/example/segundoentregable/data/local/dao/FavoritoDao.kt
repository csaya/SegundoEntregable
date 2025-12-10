package com.example.segundoentregable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.segundoentregable.data.local.entity.FavoritoEntity

@Dao
interface FavoritoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorito(favorito: FavoritoEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoritos(favoritos: List<FavoritoEntity>)

    @Query("SELECT * FROM favoritos WHERE userEmail = :userEmail AND attractionId = :attractionId")
    suspend fun getFavorito(userEmail: String, attractionId: String): FavoritoEntity?

    @Query("SELECT attractionId FROM favoritos WHERE userEmail = :userEmail")
    suspend fun getFavoritosByUser(userEmail: String): List<String>
    
    @Query("SELECT id FROM favoritos WHERE userEmail = :userEmail")
    suspend fun getFavoritoIdsByUser(userEmail: String): List<String>
    
    @Query("SELECT * FROM favoritos WHERE userEmail = :userEmail")
    suspend fun getAllFavoritosByUser(userEmail: String): List<FavoritoEntity>

    @Query("DELETE FROM favoritos WHERE userEmail = :userEmail AND attractionId = :attractionId")
    suspend fun deleteFavorito(userEmail: String, attractionId: String)

    @Query("SELECT COUNT(*) FROM favoritos WHERE userEmail = :userEmail AND attractionId = :attractionId")
    suspend fun isFavorito(userEmail: String, attractionId: String): Int
    
    // Métodos para sincronización offline
    @Query("SELECT * FROM favoritos WHERE isSynced = 0")
    suspend fun getUnsyncedFavoritos(): List<FavoritoEntity>
    
    @Query("UPDATE favoritos SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
    
    @Query("UPDATE favoritos SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markMultipleAsSynced(ids: List<String>)
}
