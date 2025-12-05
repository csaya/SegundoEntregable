package com.example.segundoentregable.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.example.segundoentregable.data.model.AtractivoTuristico

/**
 * Componente de mapa que muestra marcadores de atracciones turísticas.
 * Centro inicial: Arequipa, Perú
 */
@Composable
fun AttractionMapView(
    atractivos: List<AtractivoTuristico>,
    onMarkerClick: (AtractivoTuristico) -> Unit = {},
    modifier: Modifier = Modifier.fillMaxSize()
) {
    // Centro de Arequipa
    val arequipaCenter = LatLng(-16.3989, -71.5349)
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(arequipaCenter, 12f)
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState
    ) {
        // Agregar marcadores para cada atractivo
        atractivos.forEach { atractivo ->
            if (atractivo.latitud != 0.0 && atractivo.longitud != 0.0) {
                Marker(
                    state = MarkerState(
                        position = LatLng(atractivo.latitud, atractivo.longitud)
                    ),
                    title = atractivo.nombre,
                    snippet = atractivo.descripcionCorta,
                    onClick = {
                        onMarkerClick(atractivo)
                        false
                    }
                )
            }
        }
    }
}
