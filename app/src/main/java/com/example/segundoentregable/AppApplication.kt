package com.example.segundoentregable

import android.app.Application
import android.util.Log
import coil.Coil
import coil.ImageLoader
import com.example.segundoentregable.data.local.AppDatabase
import com.example.segundoentregable.data.repository.AttractionRepository
import com.example.segundoentregable.data.repository.FavoriteRepository
import com.example.segundoentregable.data.repository.RutaRepository
import com.example.segundoentregable.data.repository.UserRepository
import com.example.segundoentregable.data.repository.UserRouteRepository
import com.example.segundoentregable.data.location.LocationService
import com.example.segundoentregable.data.location.ProximityService
import com.example.segundoentregable.data.sync.FavoriteSyncWorker
import com.example.segundoentregable.data.sync.ReviewSyncWorker
import com.example.segundoentregable.data.sync.RutaSyncWorker
import com.example.segundoentregable.utils.DataImporter
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

private const val TAG = "AppApplication"
private const val PREFS_NAME = "app_prefs"
private const val KEY_DATA_VERSION = "data_version_v5" // Incrementar cuando cambie el esquema

class AppApplication : Application() {

    // Estado de inicialización de datos - los ViewModels pueden observar esto
    private val _isDataReady = MutableStateFlow(false)
    val isDataReady: StateFlow<Boolean> = _isDataReady.asStateFlow()

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
        FavoriteRepository(database.favoritoDao(), this)
    }

    val rutaRepository by lazy {
        RutaRepository(database.rutaDao(), this)
    }

    val userRouteRepository by lazy {
        UserRouteRepository(database.userRouteDao())
    }

    val locationService by lazy {
        LocationService(this)
    }

    val proximityService by lazy {
        ProximityService(this)
    }

    override fun onCreate() {
        super.onCreate()

        // Inicializar Firebase
        FirebaseApp.initializeApp(this)
        Log.d(TAG, "Firebase inicializado")

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

        // Programar sincronización periódica
        ReviewSyncWorker.schedulePeriodicSync(this)
        FavoriteSyncWorker.schedulePeriodicSync(this)
        RutaSyncWorker.schedulePeriodicSync(this)
        Log.d(TAG, "WorkManager configurado para sincronización")
    }

    private fun importDataIfNeeded() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val currentVersion = 5 // Incrementar cuando cambie el esquema de datos
        val savedVersion = prefs.getInt(KEY_DATA_VERSION, 0)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Verificar si la BD tiene datos (más confiable que SharedPrefs)
                val atractivosCount = database.atractivoDao().getCount()

                if (savedVersion < currentVersion || atractivosCount == 0) {
                    Log.d(TAG, "Iniciando importación de datos (version: $savedVersion -> $currentVersion, count: $atractivosCount)")
                    DataImporter.importDataFromAssets(this@AppApplication, database)
                    prefs.edit().putInt(KEY_DATA_VERSION, currentVersion).apply()
                    Log.d(TAG, "Datos importados exitosamente")
                } else {
                    Log.d(TAG, "Datos ya existen (version: $savedVersion, count: $atractivosCount)")
                }

                // Marcar datos como listos
                _isDataReady.value = true
                Log.d(TAG, "Datos listos para consumir")

            } catch (e: Exception) {
                Log.e(TAG, "Error al importar datos", e)
                // Aún así marcamos como listo para que la UI no se quede colgada
                _isDataReady.value = true
            }
        }
    }
}