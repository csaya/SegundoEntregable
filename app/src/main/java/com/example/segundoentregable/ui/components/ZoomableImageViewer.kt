package com.example.segundoentregable.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch

/**
 * Visor de imágenes a pantalla completa con zoom y paginación
 */
@Composable
fun ZoomableImageViewer(
    images: List<String>,
    initialPage: Int = 0,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()

    if (images.isEmpty()) {
        onDismiss()
        return
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            val pagerState = rememberPagerState(
                initialPage = initialPage.coerceIn(0, images.lastIndex),
                pageCount = { images.size }
            )

            // ✅ Estado compartido de zoom para todas las páginas
            // Mapa de zoom por página (key = índice de página)
            val zoomStates = remember { mutableStateMapOf<Int, Float>() }
            val currentPageZoom = zoomStates[pagerState.currentPage] ?: 1f

            // Pager de imágenes
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                // ✅ Deshabilitar swipe cuando hay zoom activo
                userScrollEnabled = currentPageZoom <= 1f
            ) { page ->
                ZoomableImage(
                    imageUrl = images[page],
                    contentDescription = "Imagen ${page + 1}",
                    // ✅ Pasar callback para actualizar el zoom
                    onZoomChange = { newZoom ->
                        zoomStates[page] = newZoom
                    }
                )
            }

            // Botón cerrar
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Cerrar",
                    tint = Color.White
                )
            }

            if (images.size > 1 && currentPageZoom <= 1f) {
                // Botón anterior
                if (pagerState.currentPage > 0) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Anterior",
                            tint = Color.White
                        )
                    }
                }

                // Botón siguiente
                if (pagerState.currentPage < images.lastIndex) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Siguiente",
                            tint = Color.White
                        )
                    }
                }
            }

            // Indicador de página
            if (images.size > 1) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1} / ${images.size}",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Indicadores de puntos
            if (images.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 64.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(images.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (isSelected) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Color.White
                                    else Color.White.copy(alpha = 0.5f)
                                )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Imagen con soporte de zoom mediante gestos
 */
@Composable
private fun ZoomableImage(
    imageUrl: String,
    contentDescription: String,
    onZoomChange: (Float) -> Unit = {} // ✅ Callback para reportar zoom
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // ✅ Reportar cambios de zoom
    LaunchedEffect(scale) {
        onZoomChange(scale)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 5f)

                    if (scale > 1f) {
                        val maxX = (size.width * (scale - 1)) / 2
                        val maxY = (size.height * (scale - 1)) / 2
                        offsetX = (offsetX + pan.x).coerceIn(-maxX, maxX)
                        offsetY = (offsetY + pan.y).coerceIn(-maxY, maxY)
                    } else {
                        offsetX = 0f
                        offsetY = 0f
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        // Reset zoom on double tap
                        if (scale > 1f) {
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        } else {
                            scale = 2.5f
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        AttractionImage(
            imageUrl = imageUrl,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                ),
            contentScale = ContentScale.Fit
        )
    }
}
