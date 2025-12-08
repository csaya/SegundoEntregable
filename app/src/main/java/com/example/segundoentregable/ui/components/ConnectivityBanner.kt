package com.example.segundoentregable.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.segundoentregable.utils.NetworkConnectivityObserver

/**
 * Banner que muestra el estado de conexión a internet.
 * Se oculta automáticamente cuando hay conexión.
 */
@Composable
fun ConnectivityBanner(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val connectivityObserver = remember { NetworkConnectivityObserver(context) }
    val isConnected by connectivityObserver.isConnected.collectAsState(initial = true)

    AnimatedVisibility(
        visible = !isConnected,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(Color(0xFFE53935))
                .padding(vertical = 8.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CloudOff,
                    contentDescription = "Sin conexión",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Sin conexión a internet",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Composable que provee el estado de conexión para uso en lógica.
 */
@Composable
fun rememberConnectivityState(): Boolean {
    val context = LocalContext.current
    val connectivityObserver = remember { NetworkConnectivityObserver(context) }
    val isConnected by connectivityObserver.isConnected.collectAsState(initial = true)
    return isConnected
}
