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

    @Query("SELECT * FROM favoritos WHERE userEmail = :userEmail AND attractionId = :attractionId")
    suspend fun getFavorito(userEmail: String, attractionId: String): FavoritoEntity?

    @Query("SELECT attractionId FROM favoritos WHERE userEmail = :userEmail")
    suspend fun getFavoritosByUser(userEmail: String): List<String>

    @Query("DELETE FROM favoritos WHERE userEmail = :userEmail AND attractionId = :attractionId")
    suspend fun deleteFavorito(userEmail: String, attractionId: String)

    @Query("SELECT COUNT(*) FROM favoritos WHERE userEmail = :userEmail AND attractionId = :attractionId")
    suspend fun isFavorito(userEmail: String, attractionId: String): Int
}
