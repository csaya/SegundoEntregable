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
import com.example.segundoentregable.data.firebase.FirestoreReviewService
import com.example.segundoentregable.data.local.AppDatabase
import java.util.concurrent.TimeUnit

/**
 * Worker para sincronizar reseñas con Firestore en segundo plano.
 * 
 * Estrategia:
 * 1. Subir reseñas locales no sincronizadas a Firestore
 * 2. Descargar reseñas nuevas de Firestore a Room
 * 3. Marcar reseñas como sincronizadas
 */
class ReviewSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val reviewDao = AppDatabase.getInstance(context).reviewDao()
    private val firestoreService = FirestoreReviewService()

    override suspend fun doWork(): Result {
        Log.d(TAG, "Iniciando sincronización de reseñas...")
        
        return try {
            // 1. Subir reseñas no sincronizadas
            val unsyncedReviews = reviewDao.getUnsyncedReviews()
            Log.d(TAG, "Reseñas pendientes de subir: ${unsyncedReviews.size}")
            
            if (unsyncedReviews.isNotEmpty()) {
                val uploadResult = firestoreService.uploadReviews(unsyncedReviews)
                uploadResult.onSuccess { syncedIds ->
                    // Marcar como sincronizadas
                    reviewDao.markMultipleAsSynced(syncedIds)
                    Log.d(TAG, "Subidas y marcadas: ${syncedIds.size} reseñas")
                }.onFailure { error ->
                    Log.e(TAG, "Error subiendo reseñas: ${error.message}")
                }
            }
            
            // 2. Descargar reseñas recientes de Firestore
            val downloadResult = firestoreService.getAllRecentReviews(limit = 50)
            downloadResult.onSuccess { remoteReviews ->
                // Insertar en Room (REPLACE evita duplicados)
                reviewDao.insertReviews(remoteReviews)
                Log.d(TAG, "Descargadas: ${remoteReviews.size} reseñas")
            }.onFailure { error ->
                Log.e(TAG, "Error descargando reseñas: ${error.message}")
            }
            
            Log.d(TAG, "Sincronización completada")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error en sincronización: ${e.message}")
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "ReviewSyncWorker"
        private const val WORK_NAME = "review_sync_work"
        
        /**
         * Programar sincronización periódica (cada 15 minutos cuando hay conexión)
         */
        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val periodicWork = PeriodicWorkRequestBuilder<ReviewSyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWork
            )
            
            Log.d(TAG, "Sincronización periódica programada")
        }
        
        /**
         * Ejecutar sincronización inmediata (cuando hay conexión)
         */
        fun syncNow(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val oneTimeWork = OneTimeWorkRequestBuilder<ReviewSyncWorker>()
                .setConstraints(constraints)
                .build()
            
            WorkManager.getInstance(context).enqueue(oneTimeWork)
            Log.d(TAG, "Sincronización inmediata encolada")
        }
        
        /**
         * Cancelar sincronización periódica
         */
        fun cancelPeriodicSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
