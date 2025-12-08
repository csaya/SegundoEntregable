package com.example.segundoentregable.ui.routes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.segundoentregable.AppApplication
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.ui.components.AttractionImage
import com.example.segundoentregable.ui.components.ConnectivityBanner
import com.example.segundoentregable.ui.components.rememberConnectivityState
import com.example.segundoentregable.utils.NavigationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RutaDetalleScreen(
    navController: NavController,
    rutaId: String
) {
    val context = LocalContext.current
    val app = context.applicationContext as AppApplication
    
    val viewModel: RutasViewModel = viewModel(
        factory = RutasViewModelFactory(app)
    )
    
    val detalleState by viewModel.detalleState.collectAsState()
    val isConnected = rememberConnectivityState()
    
    LaunchedEffect(rutaId) {
        viewModel.loadRutaDetalle(rutaId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detalleState.ruta?.nombre ?: "Ruta", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        when {
            detalleState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            detalleState.ruta == null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Ruta no encontrada", color = Color.Gray)
                }
            }
            else -> {
                val ruta = detalleState.ruta!!
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Banner de conectividad
                    item {
                        ConnectivityBanner()
                    }
                    
                    // Imagen principal
                    item {
                        AttractionImage(
                            imageUrl = ruta.imagenPrincipal,
                            contentDescription = ruta.nombre,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    // Info de la ruta
                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = ruta.descripcion,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            Spacer(Modifier.height(16.dp))
                            
                            // Stats row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatItem(Icons.Filled.AccessTime, ruta.duracionEstimada, "Duración")
                                StatItem(Icons.AutoMirrored.Filled.DirectionsWalk, "${ruta.distanciaTotal} km", "Distancia")
                                StatItem(Icons.Filled.Terrain, ruta.dificultad.replaceFirstChar { it.uppercase() }, "Dificultad")
                            }
                            
                            Spacer(Modifier.height(16.dp))
                            
                            // Recomendaciones
                            if (ruta.recomendaciones.isNotEmpty()) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            Icons.Filled.Lightbulb,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = ruta.recomendaciones,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                            
                            Spacer(Modifier.height(24.dp))
                            
                            // Título paradas
                            Text(
                                text = "Paradas de la ruta",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                    
                    // Lista de paradas
                    itemsIndexed(detalleState.atractivos) { index, atractivo ->
                        ParadaItem(
                            numero = index + 1,
                            atractivo = atractivo,
                            isLast = index == detalleState.atractivos.lastIndex,
                            onVerDetalle = { navController.navigate("detail/${atractivo.id}") },
                            onComoLlegar = {
                                NavigationUtils.openGoogleMapsNavigation(
                                    context = context,
                                    latitude = atractivo.latitud,
                                    longitude = atractivo.longitud,
                                    label = atractivo.nombre
                                )
                            },
                            isConnected = isConnected
                        )
                    }
                    
                    // Botón iniciar ruta
                    item {
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                // Navegar al primer punto
                                detalleState.atractivos.firstOrNull()?.let { primer ->
                                    NavigationUtils.openGoogleMapsNavigation(
                                        context = context,
                                        latitude = primer.latitud,
                                        longitude = primer.longitud,
                                        label = primer.nombre
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            enabled = isConnected && detalleState.atractivos.isNotEmpty()
                        ) {
                            Icon(Icons.Filled.Navigation, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Iniciar Ruta")
                        }
                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
private fun ParadaItem(
    numero: Int,
    atractivo: AtractivoTuristico,
    isLast: Boolean,
    onVerDetalle: () -> Unit,
    onComoLlegar: () -> Unit,
    isConnected: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Timeline
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            // Número
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = numero.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Línea conectora
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(80.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                )
            }
        }
        
        Spacer(Modifier.width(12.dp))
        
        // Card del atractivo
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Imagen pequeña
                AttractionImage(
                    imageUrl = atractivo.imagenPrincipal,
                    contentDescription = atractivo.nombre,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(Modifier.width(12.dp))
                
                // Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = atractivo.nombre,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = atractivo.categoria,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                // Botones
                Column {
                    IconButton(onClick = onVerDetalle, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Info, contentDescription = "Ver detalle", modifier = Modifier.size(20.dp))
                    }
                    IconButton(
                        onClick = onComoLlegar,
                        modifier = Modifier.size(32.dp),
                        enabled = isConnected
                    ) {
                        Icon(
                            Icons.Filled.Navigation,
                            contentDescription = "Cómo llegar",
                            modifier = Modifier.size(20.dp),
                            tint = if (isConnected) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }
            }
        }
    }
    
    if (!isLast) {
        Spacer(Modifier.height(8.dp))
    }
}
