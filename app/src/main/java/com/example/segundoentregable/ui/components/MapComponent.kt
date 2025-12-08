package com.example.segundoentregable.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.clustering.Clustering
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.google.maps.android.clustering.ClusterItem

/**
 * Clase que representa un item de cluster para el mapa.
 */
data class AttractionClusterItem(
    val atractivo: AtractivoTuristico
) : ClusterItem {
    override fun getPosition(): LatLng = LatLng(atractivo.latitud, atractivo.longitud)
    override fun getTitle(): String = atractivo.nombre
    override fun getSnippet(): String = atractivo.descripcionCorta
    override fun getZIndex(): Float = 0f
}

/**
 * Componente de mapa que muestra marcadores de atracciones turísticas con clustering.
 * Centro inicial: Arequipa, Perú
 * 
 * Características:
 * - Clustering automático cuando hay muchos marcadores cercanos
 * - Al hacer zoom, los clusters se expanden
 * - Click en cluster muestra el primer atractivo del grupo
 */
@OptIn(MapsComposeExperimentalApi::class)
@SuppressLint("UnrememberedMutableState")
@Composable
fun AttractionMapView(
    atractivos: List<AtractivoTuristico>,
    onMarkerClick: (AtractivoTuristico) -> Unit = {},
    modifier: Modifier = Modifier.fillMaxSize()
) {
    // Centro de Arequipa
    val arequipaCenter = LatLng(-16.3989, -71.5349)
    
    // Crear cameraPositionState una sola vez (memoizado)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(arequipaCenter, 11f)
    }
    
    // Convertir atractivos a ClusterItems
    val clusterItems = remember(atractivos) {
        atractivos
            .filter { it.latitud != 0.0 && it.longitud != 0.0 }
            .map { AttractionClusterItem(it) }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState
    ) {
        // Usar Clustering para agrupar marcadores cercanos
        Clustering(
            items = clusterItems,
            onClusterClick = { cluster ->
                // Al hacer click en un cluster, mostrar el primer atractivo
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
