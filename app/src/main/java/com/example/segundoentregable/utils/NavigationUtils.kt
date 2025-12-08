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
     * Valida conexión a internet antes de abrir.
     * Si Google Maps no está instalado, abre en el navegador.
     * 
     * @param context Contexto de Android
     * @param latitude Latitud del destino
     * @param longitude Longitud del destino
     * @param label Nombre del lugar (opcional, para mostrar en el marcador)
     * @return true si se abrió correctamente, false si no hay conexión
     */
    fun openGoogleMapsNavigation(
        context: Context,
        latitude: Double,
        longitude: Double,
        label: String = ""
    ): Boolean {
        // Validar conexión antes de abrir
        val connectivityObserver = NetworkConnectivityObserver(context)
        if (!connectivityObserver.isCurrentlyConnected()) {
            Toast.makeText(
                context,
                "Se requiere conexión a internet para la navegación",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        // URI para navegación en Google Maps
        // Formato: google.navigation:q=lat,lng
        val navigationUri = Uri.parse("google.navigation:q=$latitude,$longitude")
        
        val mapIntent = Intent(Intent.ACTION_VIEW, navigationUri).apply {
            setPackage("com.google.android.apps.maps")
        }

        // Verificar si Google Maps está instalado
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            // Fallback: abrir en navegador web
            openInBrowser(context, latitude, longitude, label)
        }
        return true
    }

    /**
     * Abre Google Maps mostrando el lugar (sin navegación).
     * Útil para ver la ubicación sin iniciar navegación.
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

    /**
     * Fallback: abre Google Maps en el navegador web.
     */
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

    /**
     * Abre Google Maps con direcciones desde la ubicación actual.
     */
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
            // Fallback a web
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
}
