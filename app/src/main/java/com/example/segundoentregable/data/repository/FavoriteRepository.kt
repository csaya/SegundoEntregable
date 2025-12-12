package com.example.segundoentregable.data.repository

import android.content.Context
import android.util.Log
import com.example.segundoentregable.data.firebase.FirestoreFavoriteService
import com.example.segundoentregable.data.local.dao.FavoritoDao
import com.example.segundoentregable.data.local.entity.FavoritoEntity
import com.example.segundoentregable.data.sync.FavoriteSyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "FavoriteRepository"

/**
 * Repositorio para favoritos con soporte offline-first.
 */
class FavoriteRepository(
    private val favoritoDao: FavoritoDao,
    private val context: Context
) {
    private val firestoreService = FirestoreFavoriteService()

    /**
     * Genera un ID determinístico para evitar duplicados
     */
    private fun generateFavoriteId(userEmail: String, attractionId: String): String {
        return "${userEmail}_$attractionId"
    }

    suspend fun toggleFavorito(userEmail: String, attractionId: String): Boolean {
        if (userEmail.isBlank()) {
            Log.w(TAG, "⚠️ Intento de toggle favorito sin usuario logueado")
            return false
        }

        val count = favoritoDao.isFavorito(userEmail, attractionId)
        val wasAdded: Boolean

        if (count > 0) {
            // Eliminar favorito
            val favoriteId = generateFavoriteId(userEmail, attractionId)
            favoritoDao.deleteFavorito(userEmail, attractionId)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    firestoreService.deleteFavorite(favoriteId)
                    Log.d(TAG, "Favorito eliminado de Firebase: $favoriteId")
                } catch (e: Exception) {
                    Log.e(TAG, "Error eliminando de Firebase: ${e.message}")
                }
            }
            wasAdded = false
        } else {
            // Agregar favorito con ID determinístico
            val favoriteId = generateFavoriteId(userEmail, attractionId)
            favoritoDao.insertFavorito(
                FavoritoEntity(
                    id = favoriteId,
                    userEmail = userEmail,
                    attractionId = attractionId,
                    isSynced = false,
                    addedAt = System.currentTimeMillis()
                )
            )
            wasAdded = true
        }

        // Disparar sincronización inmediata
        FavoriteSyncWorker.syncNow(context)
        return wasAdded
    }

    suspend fun isFavorito(userEmail: String, attractionId: String): Boolean {
        if (userEmail.isBlank()) return false
        return favoritoDao.isFavorito(userEmail, attractionId) > 0
    }

    suspend fun getFavoritosByUser(userEmail: String): List<String> {
        if (userEmail.isBlank()) return emptyList()
        return favoritoDao.getFavoritosByUser(userEmail)
    }

    suspend fun getAllFavoritosByUser(userEmail: String): List<FavoritoEntity> {
        if (userEmail.isBlank()) return emptyList()
        return favoritoDao.getAllFavoritosByUser(userEmail)
    }
}