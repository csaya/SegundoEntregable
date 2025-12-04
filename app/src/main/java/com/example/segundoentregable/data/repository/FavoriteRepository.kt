package com.example.segundoentregable.data.repository

import android.content.Context
import com.example.segundoentregable.data.local.AppDatabase
import com.example.segundoentregable.data.local.entity.FavoritoEntity
import java.util.UUID

class FavoriteRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val favoritoDao = db.favoritoDao()

    suspend fun toggleFavorito(userEmail: String, attractionId: String) {
        val exists = favoritoDao.isFavorito(userEmail, attractionId) > 0
        if (exists) {
            favoritoDao.deleteFavorito(userEmail, attractionId)
        } else {
            favoritoDao.insertFavorito(
                FavoritoEntity(
                    id = UUID.randomUUID().toString(),
                    userEmail = userEmail,
                    attractionId = attractionId
                )
            )
        }
    }

    suspend fun isFavorito(userEmail: String, attractionId: String): Boolean {
        return favoritoDao.isFavorito(userEmail, attractionId) > 0
    }

    suspend fun getFavoritosByUser(userEmail: String): List<String> {
        return favoritoDao.getFavoritosByUser(userEmail)
    }
}
