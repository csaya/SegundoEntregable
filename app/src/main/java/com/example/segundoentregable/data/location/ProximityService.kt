package com.example.segundoentregable.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.segundoentregable.MainActivity
import com.example.segundoentregable.R
import com.example.segundoentregable.data.local.AppDatabase
import com.example.segundoentregable.data.local.entity.AtractivoEntity
import com.example.segundoentregable.utils.RouteOptimizer
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Servicio de proximidad que detecta atractivos turísticos cercanos
 * y envía notificaciones locales al usuario.
 *
 * Configuración:
 * - Radio de detección: 5 km
 * - Cooldown entre notificaciones: 1 minuto
 * - Actualización de ubicación: cada 60 segundos (mínimo 30s)
 * - Distancia mínima para actualización: 100 metros
 */
class ProximityService(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val database = AppDatabase.getInstance(context)
    private val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private var locationCallback: LocationCallback? = null
    private var isMonitoring = false

    companion object {
        private const val TAG = "ProximityService"
        private const val CHANNEL_ID = "proximity_notifications"
        private const val CHANNEL_NAME = "Lugares Cercanos"
        private const val NOTIFICATION_ID_BASE = 1000

        private const val PROXIMITY_RADIUS_METERS = 500.0
        private const val NOTIFICATION_COOLDOWN_MS = 3600000L

        private const val PREFS_NAME = "proximity_prefs"
        private const val KEY_LAST_NOTIFIED = "last_notified_"

        private const val LOCATION_UPDATE_INTERVAL = 60000L
        private const val LOCATION_MIN_UPDATE_INTERVAL = 30000L
        private const val LOCATION_MIN_DISTANCE = 100f
    }

    init {
        createNotificationChannel()
    }

    /**
     * Iniciar monitoreo de ubicación
     */
    fun startMonitoring() {
        if (isMonitoring) {
            restartMonitoring()
            return
        }

        if (!hasLocationPermission()) {
            Log.e(TAG, "Permiso de ubicación no concedido")
            return
        }

        setupLocationCallback()
        requestLocationUpdates()
        checkLastKnownLocation()

        isMonitoring = true
        Log.d(TAG, "Monitoreo de proximidad iniciado")
    }

    /**
     * Detener monitoreo de ubicación
     */
    fun stopMonitoring() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        locationCallback = null
        isMonitoring = false
        Log.d(TAG, "Monitoreo de proximidad detenido")
    }

    /**
     * Limpiar cooldowns de notificaciones
     */
    fun clearCooldowns() {
        sharedPrefs.edit().clear().apply()
        Log.d(TAG, "Cooldowns limpiados")
    }

    // ========== MÉTODOS PRIVADOS ==========

    private fun restartMonitoring() {
        Log.d(TAG, "Reiniciando servicio de monitoreo")
        stopMonitoring()
        CoroutineScope(Dispatchers.Main).launch {
            delay(100)
            startMonitoring()
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    checkNearbyAttractions(location)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        if (!hasLocationPermission()) return

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            LOCATION_UPDATE_INTERVAL
        ).apply {
            setMinUpdateIntervalMillis(LOCATION_MIN_UPDATE_INTERVAL)
            setMinUpdateDistanceMeters(LOCATION_MIN_DISTANCE)
        }.build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Error al solicitar actualizaciones de ubicación", e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkLastKnownLocation() {
        if (!hasLocationPermission()) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                checkNearbyAttractions(it)
            }
        }
    }

    /**
     * Verificar atractivos cercanos a la ubicación actual
     */
    private fun checkNearbyAttractions(userLocation: Location) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val atractivos = database.atractivoDao().getAllAtractivos()
                val nearbyAttractions = atractivos.filter { atractivo ->
                    val distance = calculateDistance(userLocation, atractivo)
                    distance <= PROXIMITY_RADIUS_METERS
                }

                nearbyAttractions.forEach { atractivo ->
                    val distance = calculateDistance(userLocation, atractivo).toInt()

                    if (shouldNotify(atractivo.id)) {
                        withContext(Dispatchers.Main) {
                            sendProximityNotification(atractivo, distance)
                        }
                        markAsNotified(atractivo.id)
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error al verificar atractivos cercanos", e)
            }
        }
    }

    private fun calculateDistance(userLocation: Location, atractivo: AtractivoEntity): Double {
        return RouteOptimizer.calculateDistance(
            userLocation.latitude,
            userLocation.longitude,
            atractivo.latitud,
            atractivo.longitud
        ) * 1000 // Convertir km a metros
    }

    private fun shouldNotify(atractivoId: String): Boolean {
        val lastNotified = sharedPrefs.getLong(KEY_LAST_NOTIFIED + atractivoId, 0)
        return System.currentTimeMillis() - lastNotified > NOTIFICATION_COOLDOWN_MS
    }

    private fun markAsNotified(atractivoId: String) {
        sharedPrefs.edit()
            .putLong(KEY_LAST_NOTIFIED + atractivoId, System.currentTimeMillis())
            .apply()
    }

    /**
     * Enviar notificación de proximidad
     */
    @SuppressLint("MissingPermission")
    private fun sendProximityNotification(atractivo: AtractivoEntity, distanceMeters: Int) {
        if (!hasNotificationPermission()) {
            Log.w(TAG, "Permiso de notificaciones no concedido")
            return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("📍 Estás cerca de ${atractivo.nombre}")
            .setContentText("A solo ${distanceMeters}m. ¡Descúbrelo!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${atractivo.descripcionCorta}\n\n⭐ Rating: ${atractivo.rating} | 📍 ${distanceMeters}m")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(createPendingIntent(atractivo.id))
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_BASE + atractivo.id.hashCode(),
                notification
            )
            Log.d(TAG, "Notificación enviada: ${atractivo.nombre}")
        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar notificación", e)
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun createPendingIntent(attractionId: String): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("attraction_id", attractionId)
        }

        return PendingIntent.getActivity(
            context,
            attractionId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Crear canal de notificaciones (Android 8+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones cuando estás cerca de un lugar turístico"
            }

            context.getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }
}
