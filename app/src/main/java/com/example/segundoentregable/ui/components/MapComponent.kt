package com.example.segundoentregable.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.clustering.Clustering
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.google.maps.android.clustering.ClusterItem

/**
 * Clase que representa un item de cluster para el mapa.
 * Implementa equals/hashCode basado en ID para optimizar comparaciones.
 */
data class AttractionClusterItem(
    val atractivo: AtractivoTuristico
) : ClusterItem {
    private val cachedPosition = LatLng(atractivo.latitud, atractivo.longitud)
    
    override fun getPosition(): LatLng = cachedPosition
    override fun getTitle(): String = atractivo.nombre
    override fun getSnippet(): String = atractivo.descripcionCorta
    override fun getZIndex(): Float = 0f
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AttractionClusterItem) return false
        return atractivo.id == other.atractivo.id
    }
    
    override fun hashCode(): Int = atractivo.id.hashCode()
}

/**
 * Componente de mapa optimizado con clustering.
 * 
 * Optimizaciones:
 * - ClusterItems memoizados con derivedStateOf
 * - Posiciones cacheadas en ClusterItem
 * - MapProperties y UiSettings memoizados
 * - Lite mode deshabilitado para mejor interactividad
 */
@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun AttractionMapView(
    atractivos: List<AtractivoTuristico>,
    onMarkerClick: (AtractivoTuristico) -> Unit = {},
    modifier: Modifier = Modifier.fillMaxSize(),
    cameraPositionState: CameraPositionState? = null
) {
    // Centro de Arequipa - constante
    val arequipaCenter = remember { LatLng(-16.3989, -71.5349) }
    
    // Camera position - usar el proporcionado o crear uno nuevo
    val actualCameraState = cameraPositionState ?: rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(arequipaCenter, 11f)
    }
    
    // Propiedades del mapa memoizadas
    val mapProperties = remember {
        MapProperties(
            isMyLocationEnabled = false,
            isBuildingEnabled = false,
            isIndoorEnabled = false,
            isTrafficEnabled = false
        )
    }
    
    // UI Settings memoizados
    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
            compassEnabled = true,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false,
            rotationGesturesEnabled = true,
            scrollGesturesEnabled = true,
            tiltGesturesEnabled = false,
            zoomGesturesEnabled = true
        )
    }
    
    // Convertir atractivos a ClusterItems - solo recalcular si cambia la lista
    val clusterItems by remember(atractivos) {
        derivedStateOf {
            atractivos
                .filter { it.latitud != 0.0 && it.longitud != 0.0 }
                .map { AttractionClusterItem(it) }
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = actualCameraState,
        properties = mapProperties,
        uiSettings = uiSettings
    ) {
        if (clusterItems.isNotEmpty()) {
            Clustering(
                items = clusterItems,
                onClusterClick = { cluster ->
                    cluster.items.firstOrNull()?.let { item ->
                        onMarkerClick(item.atractivo)
                    }
                    false
                },
                onClusterItemClick = { item ->
                    onMarkerClick(item.atractivo)
                    false
                }
            )
        }
    }
}
