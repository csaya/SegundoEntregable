package com.example.segundoentregable.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

class LocationService(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission") // Controlaremos el permiso antes de llamar a esto
    suspend fun getCurrentLocation(): Location? {
        return try {
            // Intentamos obtener la ubicación precisa con alta prioridad
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).await()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Calcula distancia en metros entre dos puntos
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }
}