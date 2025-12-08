package com.example.segundoentregable.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Size

/**
 * Componente optimizado para cargar imágenes con Coil.
 * 
 * Optimizaciones aplicadas:
 * - Caché de disco y memoria habilitado
 * - Crossfade para transiciones suaves
 * - Tamaño fijo para evitar recomposiciones
 * - Request memoizado con remember
 * - Sin logs en producción
 */
@Composable
fun AttractionImage(
    imageUrl: String,
    contentDescription: String? = null,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(300.dp),
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    
    // Memoizar el request para evitar recrearlo en cada recomposición
    val imageRequest = remember(imageUrl) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(300)
            .size(Size.ORIGINAL) // Usar tamaño original, Coil lo escala automáticamente
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .addHeader("User-Agent", "Mozilla/5.0")
            .build()
    }

    SubcomposeAsyncImage(
        model = imageRequest,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier,
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 2.dp
                )
            }
        },
        error = {
            FallbackPlaceholder()
        }
    )
}

@Composable
private fun FallbackPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.PhotoCamera,
            contentDescription = "Imagen no disponible",
            modifier = Modifier.height(50.dp),
            tint = Color.Gray
        )
    }
}