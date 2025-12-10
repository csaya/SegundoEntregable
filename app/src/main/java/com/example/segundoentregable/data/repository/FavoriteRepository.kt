package com.example.segundoentregable.data.repository

import android.content.Context
import com.example.segundoentregable.data.local.dao.FavoritoDao
import com.example.segundoentregable.data.local.entity.FavoritoEntity
import com.example.segundoentregable.data.sync.FavoriteSyncWorker
import java.util.UUID

/**
 * Repositorio para favoritos con soporte offline-first.
 * Los favoritos se guardan localmente y se sincronizan con Firebase cuando hay conexión.
 */
class FavoriteRepository(
    private val favoritoDao: FavoritoDao,
    private val context: Context
) {

    suspend fun toggleFavorito(userEmail: String, attractionId: String) {
        val count = favoritoDao.isFavorito(userEmail, attractionId)
        if (count > 0) {
            favoritoDao.deleteFavorito(userEmail, attractionId)
        } else {
            favoritoDao.insertFavorito(
                FavoritoEntity(
                    id = UUID.randomUUID().toString(),
                    userEmail = userEmail,
                    attractionId = attractionId,
                    isSynced = false,
                    addedAt = System.currentTimeMillis()
                )
            )
        }
        // Disparar sincronización inmediata
        FavoriteSyncWorker.syncNow(context)
    }

    suspend fun isFavorito(userEmail: String, attractionId: String): Boolean {
        return favoritoDao.isFavorito(userEmail, attractionId) > 0
    }

    suspend fun getFavoritosByUser(userEmail: String): List<String> {
        return favoritoDao.getFavoritosByUser(userEmail)
    }
    
    suspend fun getAllFavoritosByUser(userEmail: String): List<FavoritoEntity> {
        return favoritoDao.getAllFavoritosByUser(userEmail)
    }
}