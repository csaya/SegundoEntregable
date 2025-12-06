package com.example.segundoentregable.data.repository

import com.example.segundoentregable.data.local.dao.FavoritoDao
import com.example.segundoentregable.data.local.entity.FavoritoEntity
import java.util.UUID

// RECIBE DAO, NO CONTEXTO
class FavoriteRepository(private val favoritoDao: FavoritoDao) {

    suspend fun toggleFavorito(userEmail: String, attractionId: String) {
        val count = favoritoDao.isFavorito(userEmail, attractionId)
        if (count > 0) {
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