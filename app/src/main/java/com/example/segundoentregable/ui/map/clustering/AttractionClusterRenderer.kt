package com.example.segundoentregable.ui.map.clustering

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.core.content.ContextCompat
import com.example.segundoentregable.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import kotlin.math.sqrt

/**
 * Renderer personalizado para clusters y markers individuales con diseño profesional.
 */
class AttractionClusterRenderer(
    private val context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<AttractionClusterItem>
) : DefaultClusterRenderer<AttractionClusterItem>(context, map, clusterManager) {
    
    private val iconGenerator = IconGenerator(context)
    private val clusterIconGenerator = IconGenerator(context)
    private val markerIconCache = LruCache<String, BitmapDescriptor>(20)
    
    init {
        // Configurar generadores de íconos con diseño moderno
        setupClusterIconGenerator()
    }
    
    private fun setupClusterIconGenerator() {
        // Crear un drawable con fondo circular para clusters
        clusterIconGenerator.setBackground(createClusterBackground())
        clusterIconGenerator.setTextAppearance(android.R.style.TextAppearance_Material_Medium)
    }
    
    private fun createClusterBackground(): Drawable {
        // Crear un círculo con gradiente para los clusters
        return object : Drawable() {
            private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            
            override fun draw(canvas: Canvas) {
                val bounds = bounds
                val centerX = bounds.exactCenterX()
                val centerY = bounds.exactCenterY()
                val radius = bounds.width() / 2f
                
                // Sombra
                paint.setShadowLayer(4f, 0f, 2f, Color.argb(80, 0, 0, 0))
                
                // Gradiente radial
                val gradient = RadialGradient(
                    centerX, centerY, radius,
                    intArrayOf(
                        Color.parseColor("#FF6B35"),  // Naranja brillante centro
                        Color.parseColor("#F7931E")   // Naranja oscuro borde
                    ),
                    floatArrayOf(0f, 1f),
                    Shader.TileMode.CLAMP
                )
                paint.shader = gradient
                
                // Dibujar círculo
                canvas.drawCircle(centerX, centerY, radius - 2f, paint)
                
                // Borde blanco
                paint.shader = null
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 4f
                paint.color = Color.WHITE
                canvas.drawCircle(centerX, centerY, radius - 2f, paint)
            }
            
            override fun setAlpha(alpha: Int) {}
            override fun setColorFilter(colorFilter: ColorFilter?) {}
            override fun getOpacity(): Int = PixelFormat.OPAQUE
        }
    }
    
    override fun onBeforeClusterItemRendered(
        item: AttractionClusterItem,
        markerOptions: MarkerOptions
    ) {
        // Crear marker personalizado según categoría
        val category = item.attraction.categoria.lowercase()
        val cacheKey = "marker_$category"
        
        var descriptor = markerIconCache.get(cacheKey)
        if (descriptor == null) {
            descriptor = createCustomMarker(category)
            markerIconCache.put(cacheKey, descriptor)
        }
        
        markerOptions
            .icon(descriptor)
            .title(item.attraction.nombre)
            .snippet("${item.attraction.categoria} - ${item.attraction.rating} ⭐")
    }
    
    override fun onClusterItemUpdated(item: AttractionClusterItem, marker: Marker) {
        // Actualizar marker si es necesario
        marker.title = item.attraction.nombre
        marker.snippet = "${item.attraction.categoria} - ${item.attraction.rating} ⭐"
    }
    
    override fun onBeforeClusterRendered(
        cluster: Cluster<AttractionClusterItem>,
        markerOptions: MarkerOptions
    ) {
        // Crear ícono de cluster con número
        val size = cluster.size
        val descriptor = createClusterIcon(size)
        
        markerOptions
            .icon(descriptor)
            .title("$size atractivos")
            .snippet("Haz zoom para ver más")
    }
    
    private fun createCustomMarker(category: String): BitmapDescriptor {
        val size = 120 // Tamaño del marker
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        
        // Color según categoría
        val color = when(category) {
            "aventura" -> Color.parseColor("#FF5722")     // Deep Orange
            "cultural" -> Color.parseColor("#9C27B0")     // Purple
            "natural" -> Color.parseColor("#4CAF50")      // Green
            "gastronomía" -> Color.parseColor("#FFC107")  // Amber
            else -> Color.parseColor("#2196F3")           // Blue
        }
        
        // Dibujar pin personalizado
        drawCustomPin(canvas, paint, color, category)
        
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
    
    private fun drawCustomPin(canvas: Canvas, paint: Paint, color: Int, category: String) {
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()
        
        // Path para forma de pin
        val path = Path()
        val radius = width * 0.35f
        val centerX = width / 2f
        val centerY = height * 0.35f
        
        // Crear forma de pin con curvas suaves
        path.addCircle(centerX, centerY, radius, Path.Direction.CW)
        path.moveTo(centerX - radius * 0.7f, centerY + radius * 0.5f)
        path.quadTo(centerX, height * 0.9f, centerX + radius * 0.7f, centerY + radius * 0.5f)
        
        // Sombra
        paint.setShadowLayer(6f, 0f, 3f, Color.argb(100, 0, 0, 0))
        
        // Relleno con gradiente
        val gradient = LinearGradient(
            0f, 0f, 0f, height,
            color,
            adjustColorBrightness(color, 0.7f),
            Shader.TileMode.CLAMP
        )
        paint.shader = gradient
        paint.style = Paint.Style.FILL
        canvas.drawPath(path, paint)
        
        // Borde
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = Color.WHITE
        canvas.drawPath(path, paint)
        
        // Ícono central según categoría
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        paint.textSize = radius * 0.8f
        paint.textAlign = Paint.Align.CENTER
        
        val icon = when(category) {
            "aventura" -> "🏔"
            "cultural" -> "🏛"
            "natural" -> "🌳"
            "gastronomía" -> "🍽"
            else -> "📍"
        }
        
        canvas.drawText(icon, centerX, centerY + radius * 0.3f, paint)
    }
    
    private fun createClusterIcon(clusterSize: Int): BitmapDescriptor {
        // Determinar tamaño del cluster basado en cantidad
        val dimension = when {
            clusterSize < 10 -> 80
            clusterSize < 50 -> 100
            clusterSize < 100 -> 120
            else -> 140
        }
        
        val bitmap = Bitmap.createBitmap(dimension, dimension, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        
        // Color del cluster basado en tamaño
        val color = when {
            clusterSize < 10 -> Color.parseColor("#4CAF50")   // Verde
            clusterSize < 50 -> Color.parseColor("#FF9800")   // Naranja
            clusterSize < 100 -> Color.parseColor("#FF5722")  // Naranja oscuro
            else -> Color.parseColor("#F44336")                // Rojo
        }
        
        // Dibujar círculo con efecto de elevación
        val centerX = dimension / 2f
        val centerY = dimension / 2f
        val radius = dimension / 2f - 8f
        
        // Sombra externa
        paint.setShadowLayer(8f, 0f, 4f, Color.argb(60, 0, 0, 0))
        
        // Círculo principal con gradiente
        val gradient = RadialGradient(
            centerX, centerY, radius,
            intArrayOf(
                adjustColorBrightness(color, 1.3f),
                color,
                adjustColorBrightness(color, 0.8f)
            ),
            floatArrayOf(0f, 0.7f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = gradient
        canvas.drawCircle(centerX, centerY, radius, paint)
        
        // Borde blanco
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        paint.color = Color.WHITE
        canvas.drawCircle(centerX, centerY, radius, paint)
        
        // Número con sombra
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        paint.textSize = radius * 0.7f
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.setShadowLayer(2f, 0f, 1f, Color.argb(100, 0, 0, 0))
        
        // Centrar texto verticalmente
        val textBounds = Rect()
        val text = if (clusterSize > 999) "999+" else clusterSize.toString()
        paint.getTextBounds(text, 0, text.length, textBounds)
        val textY = centerY - (textBounds.top + textBounds.bottom) / 2f
        
        canvas.drawText(text, centerX, textY, paint)
        
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
    
    private fun adjustColorBrightness(color: Int, factor: Float): Int {
        val r = ((Color.red(color) * factor).toInt().coerceIn(0, 255))
        val g = ((Color.green(color) * factor).toInt().coerceIn(0, 255))
        val b = ((Color.blue(color) * factor).toInt().coerceIn(0, 255))
        return Color.argb(Color.alpha(color), r, g, b)
    }
    
    override fun shouldRenderAsCluster(cluster: Cluster<AttractionClusterItem>): Boolean {
        // Renderizar como cluster si hay más de 3 items
        return cluster.size > 3
    }
    
    override fun getColor(clusterSize: Int): Int {
        // Color del texto del cluster
        return Color.WHITE
    }
}
