package com.example.segundoentregable.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/**
 * Utilidades para navegación externa (Google Maps, etc.)
 */
object NavigationUtils {

    /**
     * Abre Google Maps con navegación hacia las coordenadas especificadas.
     */
    fun openGoogleMapsNavigation(
        context: Context,
        latitude: Double,
        longitude: Double,
        label: String = ""
    ): Boolean {
        val connectivityObserver = NetworkConnectivityObserver(context)
        if (!connectivityObserver.isCurrentlyConnected()) {
            Toast.makeText(
                context,
                "Se requiere conexión a internet para la navegación",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        val navigationUri = Uri.parse("google.navigation:q=$latitude,$longitude")
        val mapIntent = Intent(Intent.ACTION_VIEW, navigationUri).apply {
            setPackage("com.google.android.apps.maps")
        }

        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            openInBrowser(context, latitude, longitude, label)
        }

        return true
    }

    /**
     * ✅ NUEVO: Abre Google Maps con navegación a múltiples puntos (waypoints).
     * Google Maps permite hasta 9 waypoints en la URL.
     *
     * @param context Contexto de Android
     * @param destinations Lista de destinos (lat, lng, nombre)
     * @param startFromCurrentLocation Si true, inicia desde la ubicación actual del usuario
     * @return true si se abrió correctamente
     */
    fun openGoogleMapsWithWaypoints(
        context: Context,
        destinations: List<Destination>,
        startFromCurrentLocation: Boolean = true
    ): Boolean {
        val connectivityObserver = NetworkConnectivityObserver(context)
        if (!connectivityObserver.isCurrentlyConnected()) {
            Toast.makeText(
                context,
                "Se requiere conexión a internet para la navegación",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (destinations.isEmpty()) {
            Toast.makeText(context, "No hay destinos en la ruta", Toast.LENGTH_SHORT).show()
            return false
        }

        // Google Maps tiene límite de 9 waypoints
        val maxWaypoints = 9
        val limitedDestinations = if (destinations.size > maxWaypoints + 1) {
            Toast.makeText(
                context,
                "Google Maps permite hasta 10 puntos. Se mostrarán los primeros 10.",
                Toast.LENGTH_LONG
            ).show()
            destinations.take(maxWaypoints + 1)
        } else {
            destinations
        }

        // Construir URL para Google Maps con waypoints
        val url = buildGoogleMapsWaypointsUrl(limitedDestinations, startFromCurrentLocation)

        // Intentar abrir en la app de Google Maps
        val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            setPackage("com.google.android.apps.maps")
        }

        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            // Fallback: abrir en navegador
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(browserIntent)
        }

        return true
    }

    /**
     * Construye la URL de Google Maps con waypoints.
     * Formato: https://www.google.com/maps/dir/?api=1&origin=current+location&destination=lat,lng&waypoints=lat1,lng1|lat2,lng2|...&travelmode=walking
     */
    private fun buildGoogleMapsWaypointsUrl(
        destinations: List<Destination>,
        startFromCurrentLocation: Boolean
    ): String {
        val baseUrl = "https://www.google.com/maps/dir/?api=1"

        // Origen
        val origin = if (startFromCurrentLocation) {
            "current+location"
        } else {
            "${destinations.first().latitude},${destinations.first().longitude}"
        }

        // Destino final (último punto)
        val finalDestination = destinations.last()
        val destination = "${finalDestination.latitude},${finalDestination.longitude}"

        // Waypoints intermedios (puntos 2 hasta n-1)
        val waypoints = if (destinations.size > 2) {
            val intermediatePoints = if (startFromCurrentLocation) {
                destinations.dropLast(1) // Del 1 al n-1
            } else {
                destinations.drop(1).dropLast(1) // Del 2 al n-1
            }

            intermediatePoints.joinToString("|") {
                "${it.latitude},${it.longitude}"
            }
        } else if (destinations.size == 2 && startFromCurrentLocation) {
            // Solo hay 2 puntos y empezamos desde ubicación actual
            "${destinations.first().latitude},${destinations.first().longitude}"
        } else {
            ""
        }

        // Construir URL completa
        val urlBuilder = StringBuilder(baseUrl)
        urlBuilder.append("&origin=").append(origin)
        urlBuilder.append("&destination=").append(destination)

        if (waypoints.isNotEmpty()) {
            urlBuilder.append("&waypoints=").append(waypoints)
        }

        urlBuilder.append("&travelmode=walking") // Modo caminata para turismo

        return urlBuilder.toString()
    }

    /**
     * Abre Google Maps mostrando el lugar (sin navegación).
     */
    fun openGoogleMapsLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
        label: String = ""
    ) {
        val encodedLabel = Uri.encode(label)
        val gmmIntentUri = if (label.isNotEmpty()) {
            Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($encodedLabel)")
        } else {
            Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
        }

        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage("com.google.android.apps.maps")
        }

        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            openInBrowser(context, latitude, longitude, label)
        }
    }

    private fun openInBrowser(
        context: Context,
        latitude: Double,
        longitude: Double,
        label: String
    ) {
        val encodedLabel = Uri.encode(label)
        val url = if (label.isNotEmpty()) {
            "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude&query_place_id=$encodedLabel"
        } else {
            "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
        }

        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(browserIntent)
    }

    fun openGoogleMapsDirections(
        context: Context,
        destinationLat: Double,
        destinationLng: Double,
        destinationLabel: String = "",
        travelMode: TravelMode = TravelMode.DRIVING
    ) {
        val mode = when (travelMode) {
            TravelMode.DRIVING -> "d"
            TravelMode.WALKING -> "w"
            TravelMode.BICYCLING -> "b"
            TravelMode.TRANSIT -> "r"
        }

        val uri = Uri.parse("google.navigation:q=$destinationLat,$destinationLng&mode=$mode")
        val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }

        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            val webUrl = "https://www.google.com/maps/dir/?api=1&destination=$destinationLat,$destinationLng&travelmode=${travelMode.name.lowercase()}"
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
            context.startActivity(browserIntent)
        }
    }

    enum class TravelMode {
        DRIVING,
        WALKING,
        BICYCLING,
        TRANSIT
    }

    /**
     * ✅ NUEVO: Clase de datos para representar un destino
     */
    data class Destination(
        val latitude: Double,
        val longitude: Double,
        val name: String = ""
    )
}
