package com.example.segundoentregable.utils

import com.example.segundoentregable.data.model.AtractivoTuristico
import kotlin.math.*

/**
 * Optimizador de rutas usando el algoritmo del Vecino más Cercano.
 * 
 * El algoritmo:
 * 1. Comienza desde la ubicación actual del usuario
 * 2. Encuentra el punto más cercano no visitado
 * 3. Se mueve a ese punto y lo marca como visitado
 * 4. Repite hasta visitar todos los puntos
 */
object RouteOptimizer {

    /**
     * Ordena una lista de atractivos usando el algoritmo del Vecino más Cercano.
     * 
     * @param atractivos Lista de atractivos a ordenar
     * @param startLat Latitud inicial (ubicación del usuario)
     * @param startLng Longitud inicial (ubicación del usuario)
     * @return Lista ordenada de atractivos en el orden óptimo de visita
     */
    fun optimizeRoute(
        atractivos: List<AtractivoTuristico>,
        startLat: Double,
        startLng: Double
    ): List<AtractivoTuristico> {
        if (atractivos.isEmpty()) return emptyList()
        if (atractivos.size == 1) return atractivos
        
        val remaining = atractivos.toMutableList()
        val ordered = mutableListOf<AtractivoTuristico>()
        
        var currentLat = startLat
        var currentLng = startLng
        
        while (remaining.isNotEmpty()) {
            // Encontrar el más cercano
            val nearest = remaining.minByOrNull { 
                calculateDistance(currentLat, currentLng, it.latitud, it.longitud) 
            }!!
            
            ordered.add(nearest)
            remaining.remove(nearest)
            
            // Actualizar posición actual
            currentLat = nearest.latitud
            currentLng = nearest.longitud
        }
        
        return ordered
    }
    
    /**
     * Calcula la distancia total de una ruta.
     * 
     * @param atractivos Lista ordenada de atractivos
     * @param startLat Latitud inicial
     * @param startLng Longitud inicial
     * @return Distancia total en kilómetros
     */
    fun calculateTotalDistance(
        atractivos: List<AtractivoTuristico>,
        startLat: Double,
        startLng: Double
    ): Double {
        if (atractivos.isEmpty()) return 0.0
        
        var totalDistance = 0.0
        var currentLat = startLat
        var currentLng = startLng
        
        for (atractivo in atractivos) {
            totalDistance += calculateDistance(currentLat, currentLng, atractivo.latitud, atractivo.longitud)
            currentLat = atractivo.latitud
            currentLng = atractivo.longitud
        }
        
        return totalDistance
    }
    
    /**
     * Estima el tiempo total de la ruta.
     * Asume velocidad promedio de caminata: 5 km/h
     * Más tiempo de visita por atractivo: 30 min promedio
     * 
     * @param atractivos Lista de atractivos
     * @param startLat Latitud inicial
     * @param startLng Longitud inicial
     * @return Tiempo estimado en minutos
     */
    fun estimateTotalTime(
        atractivos: List<AtractivoTuristico>,
        startLat: Double,
        startLng: Double
    ): Int {
        val distanceKm = calculateTotalDistance(atractivos, startLat, startLng)
        val walkingTimeMinutes = (distanceKm / 5.0) * 60 // 5 km/h
        val visitTimeMinutes = atractivos.size * 30 // 30 min por atractivo
        
        return (walkingTimeMinutes + visitTimeMinutes).toInt()
    }
    
    /**
     * Calcula la distancia entre dos puntos usando la fórmula de Haversine.
     * 
     * @return Distancia en kilómetros
     */
    fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val earthRadius = 6371.0 // Radio de la Tierra en km
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        
        val a = sin(dLat / 2).pow(2) + 
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * 
                sin(dLng / 2).pow(2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return earthRadius * c
    }
    
    /**
     * Formatea la distancia para mostrar al usuario.
     */
    fun formatDistance(distanceKm: Double): String {
        return if (distanceKm < 1) {
            "${(distanceKm * 1000).toInt()} m"
        } else {
            String.format("%.1f km", distanceKm)
        }
    }
    
    /**
     * Formatea el tiempo para mostrar al usuario.
     */
    fun formatTime(minutes: Int): String {
        return if (minutes < 60) {
            "$minutes min"
        } else {
            val hours = minutes / 60
            val mins = minutes % 60
            if (mins > 0) "$hours h $mins min" else "$hours h"
        }
    }
}
