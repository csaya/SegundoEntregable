package com.example.segundoentregable

import android.app.Application
import android.util.Log
import coil.Coil
import coil.ImageLoader
import com.example.segundoentregable.data.local.AppDatabase
import com.example.segundoentregable.data.repository.AttractionRepository
import com.example.segundoentregable.data.repository.FavoriteRepository
import com.example.segundoentregable.data.repository.UserRepository
import com.example.segundoentregable.data.location.LocationService
import com.example.segundoentregable.utils.DataImporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

private const val TAG = "AppApplication"
private const val PREFS_NAME = "app_prefs"
private const val KEY_DATA_IMPORTED = "data_imported_v2"

class AppApplication : Application() {

    val database by lazy { AppDatabase.getInstance(this) }

    val userRepository by lazy {
        UserRepository(database.userDao(), this)
    }

    val attractionRepository by lazy {
        AttractionRepository(
            database.atractivoDao(),
            database.reviewDao(),
            database.galeriaFotoDao(),
            database.actividadDao(),
            database.favoritoDao()
        )
    }

    val favoriteRepository by lazy {
        FavoriteRepository(database.favoritoDao())
    }

    val locationService by lazy {
        LocationService(this)
    }

    override fun onCreate() {
        super.onCreate()

        // Configurar Coil con caché de disco y OkHttp
        val imageLoader = ImageLoader.Builder(this)
            .okHttpClient {
                OkHttpClient.Builder()
                    .build()
            }
            .build()

        Coil.setImageLoader(imageLoader)

        // Importar datos desde assets si no se ha hecho antes
        importDataIfNeeded()
    }

    private fun importDataIfNeeded() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val dataImported = prefs.getBoolean(KEY_DATA_IMPORTED, false)

        if (!dataImported) {
            Log.d(TAG, "Iniciando importación de datos desde assets...")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    DataImporter.importDataFromAssets(this@AppApplication, database)
                    prefs.edit().putBoolean(KEY_DATA_IMPORTED, true).apply()
                    Log.d(TAG, "Datos importados exitosamente")
                } catch (e: Exception) {
                    Log.e(TAG, "Error al importar datos", e)
                }
            }
        } else {
            Log.d(TAG, "Datos ya fueron importados previamente")
        }
    }
}