package com.example.segundoentregable.data.firebase

import android.util.Log
import com.example.segundoentregable.data.local.entity.FavoritoEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreFavoriteService"

/**
 * Servicio para sincronizar favoritos con Firestore.
 */
class FirestoreFavoriteService {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val favoritesCollection = firestore.collection("favorites")
    
    /**
     * Subir un favorito a Firestore
     */
    suspend fun uploadFavorite(favorite: FavoritoEntity): Result<Unit> {
        return try {
            val favoriteData = hashMapOf(
                "id" to favorite.id,
                "attractionId" to favorite.attractionId,
                "userEmail" to favorite.userEmail,
                "addedAt" to System.currentTimeMillis()
            )
            
            favoritesCollection.document(favorite.id).set(favoriteData).await()
            Log.d(TAG, "Favorito sincronizado: ${favorite.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al subir favorito: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Eliminar un favorito de Firestore
     */
    suspend fun deleteFavorite(favoriteId: String): Result<Unit> {
        return try {
            favoritesCollection.document(favoriteId).delete().await()
            Log.d(TAG, "Favorito eliminado de Firestore: $favoriteId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar favorito: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Obtener favoritos de un usuario desde Firestore
     */
    suspend fun getFavoritesForUser(userEmail: String): Result<List<FavoritoEntity>> {
        return try {
            val snapshot = favoritesCollection
                .whereEqualTo("userEmail", userEmail)
                .get()
                .await()
            
            val favorites = snapshot.documents.mapNotNull { doc ->
                try {
                    FavoritoEntity(
                        id = doc.getString("id") ?: return@mapNotNull null,
                        attractionId = doc.getString("attractionId") ?: return@mapNotNull null,
                        userEmail = doc.getString("userEmail") ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }
            
            Result.success(favorites)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener favoritos: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Sincronizar favoritos locales con Firestore
     */
    suspend fun syncFavorites(localFavorites: List<FavoritoEntity>): Result<Int> {
        if (localFavorites.isEmpty()) return Result.success(0)
        
        return try {
            val batch = firestore.batch()
            var count = 0
            
            localFavorites.forEach { favorite ->
                val docRef = favoritesCollection.document(favorite.id)
                val favoriteData = hashMapOf(
                    "id" to favorite.id,
                    "attractionId" to favorite.attractionId,
                    "userEmail" to favorite.userEmail,
                    "addedAt" to System.currentTimeMillis()
                )
                batch.set(docRef, favoriteData)
                count++
            }
            
            batch.commit().await()
            Log.d(TAG, "Sincronizados $count favoritos")
            Result.success(count)
        } catch (e: Exception) {
            Log.e(TAG, "Error en sincronizacion batch: ${e.message}")
            Result.failure(e)
        }
    }
}
