package com.example.segundoentregable.ui.map.clustering

import com.example.segundoentregable.data.model.AtractivoTuristico
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

/**
 * ClusterItem personalizado para los atractivos turísticos.
 * Permite agrupar markers cuando hay muchos en una zona pequeña.
 */
data class AttractionClusterItem(
    val attraction: AtractivoTuristico,
    private val position: LatLng
) : ClusterItem {
    
    override fun getPosition(): LatLng = position
    
    override fun getTitle(): String = attraction.nombre
    
    override fun getSnippet(): String = attraction.categoria
    
    override fun getZIndex(): Float = when(attraction.categoria.lowercase()) {
        "aventura" -> 3f
        "cultural" -> 2f
        "natural" -> 1f
        else -> 0f
    }
    
    fun getMarkerColor(): Float = when(attraction.categoria.lowercase()) {
        "aventura" -> 30f  // HUE_ORANGE
        "cultural" -> 270f  // HUE_VIOLET
        "natural" -> 120f   // HUE_GREEN
        "gastronomía" -> 60f // HUE_YELLOW
        else -> 0f          // HUE_RED
    }
}
