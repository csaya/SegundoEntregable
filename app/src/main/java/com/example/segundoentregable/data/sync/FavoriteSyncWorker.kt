package com.example.segundoentregable.data.sync

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.segundoentregable.data.firebase.FirestoreFavoriteService
import com.example.segundoentregable.data.local.AppDatabase
import com.example.segundoentregable.data.local.SharedPrefManager
import com.example.segundoentregable.data.local.entity.FavoritoEntity
import java.util.concurrent.TimeUnit

/**
 * Worker para sincronizar favoritos con Firestore en segundo plano.
 * 
 * Estrategia offline-first:
 * 1. Los favoritos se guardan localmente inmediatamente
 * 2. Cuando hay conexión, se sincronizan con Firestore
 * 3. Se descargan favoritos remotos para merge
 */
class FavoriteSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val database = AppDatabase.getInstance(applicationContext)
    private val favoritoDao = database.favoritoDao()
    private val prefs = SharedPrefManager.getInstance(applicationContext)
    private val firestoreService = FirestoreFavoriteService()

    override suspend fun doWork(): Result {
        Log.d(TAG, "Iniciando sincronización de favoritos...")
        
        return try {
            // Obtener email del usuario logueado
            val userEmail = prefs.getCurrentUserEmail()
            if (userEmail == null) {
                Log.d(TAG, "No hay usuario logueado, saltando sincronización")
                return Result.success()
            }
            
            // 1. Subir favoritos no sincronizados
            val unsyncedFavoritos = favoritoDao.getUnsyncedFavoritos()
            Log.d(TAG, "Favoritos pendientes de subir: ${unsyncedFavoritos.size}")
            
            if (unsyncedFavoritos.isNotEmpty()) {
                val uploadResult = firestoreService.syncFavorites(unsyncedFavoritos)
                uploadResult.onSuccess { count ->
                    // Marcar como sincronizados
                    val ids = unsyncedFavoritos.map { it.id }
                    favoritoDao.markMultipleAsSynced(ids)
                    Log.d(TAG, "Subidos y marcados: $count favoritos")
                }.onFailure { error ->
                    Log.e(TAG, "Error subiendo favoritos: ${error.message}")
                }
            }
            
            // 2. Descargar favoritos del usuario desde Firebase
            Log.d(TAG, "Descargando favoritos desde Firebase para $userEmail...")
            val downloadResult = firestoreService.getFavoritesForUser(userEmail)
            downloadResult.onSuccess { remoteFavorites ->
                Log.d(TAG, "Favoritos remotos recibidos: ${remoteFavorites.size}")
                
                // Obtener IDs locales
                val localIds = favoritoDao.getFavoritoIdsByUser(userEmail)
                
                // Insertar favoritos remotos que no existan localmente
                remoteFavorites.forEach { remoteFav ->
                    if (remoteFav.id !in localIds) {
                        val localFav = FavoritoEntity(
                            id = remoteFav.id,
                            userEmail = remoteFav.userEmail,
                            attractionId = remoteFav.attractionId,
                            addedAt = remoteFav.addedAt,
                            isSynced = true // Ya está sincronizado
                        )
                        favoritoDao.insertFavorito(localFav)
                        Log.d(TAG, "Favorito descargado: ${remoteFav.attractionId}")
                    }
                }
            }.onFailure { error ->
                Log.e(TAG, "Error descargando favoritos: ${error.message}")
            }
            
            Log.d(TAG, "Sincronización de favoritos completada")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error en sincronización de favoritos: ${e.message}")
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "FavoriteSyncWorker"
        private const val WORK_NAME = "favorite_sync_work"
        
        /**
         * Programar sincronización periódica (cada 15 minutos cuando hay conexión)
         */
        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val periodicWork = PeriodicWorkRequestBuilder<FavoriteSyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWork
            )
            
            Log.d(TAG, "Sincronización periódica de favoritos programada")
        }
        
        /**
         * Ejecutar sincronización inmediata (cuando hay conexión)
         */
        fun syncNow(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val oneTimeWork = OneTimeWorkRequestBuilder<FavoriteSyncWorker>()
                .setConstraints(constraints)
                .build()
            
            WorkManager.getInstance(context).enqueue(oneTimeWork)
            Log.d(TAG, "Sincronización inmediata de favoritos encolada")
        }
        
        /**
         * Cancelar sincronización periódica
         */
        fun cancelPeriodicSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
