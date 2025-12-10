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
import com.example.segundoentregable.data.firebase.FirestoreRutaService
import com.example.segundoentregable.data.local.AppDatabase
import java.util.concurrent.TimeUnit

/**
 * Worker para sincronizar rutas de usuario con Firestore en segundo plano.
 */
class RutaSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val rutaDao = AppDatabase.getInstance(context).rutaDao()
    private val firestoreService = FirestoreRutaService()

    override suspend fun doWork(): Result {
        Log.d(TAG, "Iniciando sincronización de rutas...")
        
        return try {
            // Subir rutas no sincronizadas
            val unsyncedRoutes = rutaDao.getUnsyncedUserRoutes()
            Log.d(TAG, "Rutas pendientes de subir: ${unsyncedRoutes.size}")
            
            unsyncedRoutes.forEach { ruta ->
                val paradas = rutaDao.getParadasByRuta(ruta.id)
                val uploadResult = firestoreService.uploadRuta(ruta, paradas)
                
                uploadResult.onSuccess {
                    rutaDao.markAsSynced(ruta.id)
                    Log.d(TAG, "Ruta sincronizada: ${ruta.id}")
                }.onFailure { error ->
                    Log.e(TAG, "Error subiendo ruta ${ruta.id}: ${error.message}")
                }
            }
            
            Log.d(TAG, "Sincronización de rutas completada")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error en sincronización de rutas: ${e.message}")
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "RutaSyncWorker"
        private const val WORK_NAME = "ruta_sync_work"
        
        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val periodicWork = PeriodicWorkRequestBuilder<RutaSyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWork
            )
            
            Log.d(TAG, "Sincronización periódica de rutas programada")
        }
        
        fun syncNow(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val oneTimeWork = OneTimeWorkRequestBuilder<RutaSyncWorker>()
                .setConstraints(constraints)
                .build()
            
            WorkManager.getInstance(context).enqueue(oneTimeWork)
            Log.d(TAG, "Sincronización inmediata de rutas encolada")
        }
    }
}
