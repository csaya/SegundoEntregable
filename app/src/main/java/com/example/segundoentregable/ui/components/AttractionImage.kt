package com.example.segundoentregable.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage

/**
 * Componente reutilizable para cargar imágenes usando Coil 2.7.0.
 * Usa SubcomposeAsyncImage para permitir Composables complejos en loading/error.
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
    SubcomposeAsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier,
        // 'loading' recibe un Composable scope
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        },
        // 'error' recibe un Composable scope
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