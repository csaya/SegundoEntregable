package com.example.segundoentregable.data.location

import android.Manifest
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
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.utils.RouteOptimizer
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Servicio de proximidad que detecta cuando el usuario está cerca de un atractivo
 * y envía notificaciones locales.
 * 
 * Radio de detección: 500 metros
 */
class ProximityService(
    private val context: Context
) {
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
        private const val PREFS_NAME = "proximity_prefs"
        private const val KEY_LAST_NOTIFIED = "last_notified_"
        private const val NOTIFICATION_COOLDOWN_MS = 3600000L // 1 hora
    }
    
    init {
        createNotificationChannel()
    }
    
    /**
     * Iniciar monitoreo de ubicación para detectar atractivos cercanos
     */
    fun startMonitoring() {
        if (isMonitoring) return
        
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Permiso de ubicación no concedido")
            return
        }
        
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            60000L // Cada 1 minuto
        ).apply {
            setMinUpdateIntervalMillis(30000L) // Mínimo 30 segundos
            setMinUpdateDistanceMeters(100f) // Mínimo 100 metros de movimiento
        }.build()
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    checkNearbyAttractions(location)
                }
            }
        }
        
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )
        
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
     * Verificar si hay atractivos cercanos y enviar notificación
     */
    private fun checkNearbyAttractions(userLocation: Location) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val atractivos = database.atractivoDao().getAllAtractivos()
                
                for (atractivo in atractivos) {
                    val distance = RouteOptimizer.calculateDistance(
                        userLocation.latitude,
                        userLocation.longitude,
                        atractivo.latitud,
                        atractivo.longitud
                    ) * 1000 // Convertir a metros
                    
                    if (distance <= PROXIMITY_RADIUS_METERS) {
                        // Verificar cooldown
                        if (shouldNotify(atractivo.id)) {
                            withContext(Dispatchers.Main) {
                                sendProximityNotification(
                                    AtractivoTuristico(
                                        id = atractivo.id,
                                        codigoMincetur = atractivo.codigoMincetur,
                                        nombre = atractivo.nombre,
                                        descripcionCorta = atractivo.descripcionCorta,
                                        descripcionLarga = atractivo.descripcionLarga,
                                        ubicacion = atractivo.ubicacion,
                                        latitud = atractivo.latitud,
                                        longitud = atractivo.longitud,
                                        departamento = atractivo.departamento,
                                        provincia = atractivo.provincia,
                                        distrito = atractivo.distrito,
                                        altitud = atractivo.altitud,
                                        categoria = atractivo.categoria,
                                        tipo = atractivo.tipo,
                                        subtipo = atractivo.subtipo,
                                        jerarquia = atractivo.jerarquia,
                                        precio = atractivo.precio,
                                        precioDetalle = atractivo.precioDetalle,
                                        horario = atractivo.horario,
                                        horarioDetallado = atractivo.horarioDetallado,
                                        epocaVisita = atractivo.epocaVisita,
                                        tiempoVisitaSugerido = atractivo.tiempoVisitaSugerido,
                                        estadoActual = atractivo.estadoActual,
                                        observaciones = atractivo.observaciones,
                                        tieneAccesibilidad = atractivo.tieneAccesibilidad,
                                        imagenPrincipal = atractivo.imagenPrincipal,
                                        rating = atractivo.rating,
                                        distanciaTexto = "${distance.toInt()}m"
                                    ),
                                    distance.toInt()
                                )
                            }
                            markAsNotified(atractivo.id)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error verificando atractivos cercanos: ${e.message}")
            }
        }
    }
    
    /**
     * Verificar si debemos notificar (cooldown de 1 hora)
     */
    private fun shouldNotify(atractivoId: String): Boolean {
        val lastNotified = sharedPrefs.getLong(KEY_LAST_NOTIFIED + atractivoId, 0)
        return System.currentTimeMillis() - lastNotified > NOTIFICATION_COOLDOWN_MS
    }
    
    /**
     * Marcar atractivo como notificado
     */
    private fun markAsNotified(atractivoId: String) {
        sharedPrefs.edit()
            .putLong(KEY_LAST_NOTIFIED + atractivoId, System.currentTimeMillis())
            .apply()
    }
    
    /**
     * Enviar notificación de proximidad
     */
    private fun sendProximityNotification(atractivo: AtractivoTuristico, distanceMeters: Int) {
        // Intent para abrir el detalle del atractivo
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("attraction_id", atractivo.id)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            atractivo.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("📍 Estás cerca de ${atractivo.nombre}")
            .setContentText("A solo ${distanceMeters}m. ¡Descúbrelo!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("${atractivo.descripcionCorta}\n\n⭐ Rating: ${atractivo.rating} | 📍 ${distanceMeters}m"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        ) {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_BASE + atractivo.id.hashCode(),
                notification
            )
            Log.d(TAG, "Notificación enviada para: ${atractivo.nombre}")
        }
    }
    
    /**
     * Crear canal de notificaciones (requerido para Android 8+)
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
            
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
