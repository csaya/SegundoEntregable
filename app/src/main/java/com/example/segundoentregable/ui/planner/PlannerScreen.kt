package com.example.segundoentregable.ui.planner

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.segundoentregable.AppApplication
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.ui.components.AttractionImage
import com.example.segundoentregable.ui.components.ConnectivityBanner
import com.example.segundoentregable.ui.components.rememberConnectivityState
import com.example.segundoentregable.utils.NavigationUtils

/**
 * Pantalla del Planificador de Rutas Personales.
 * Muestra los lugares que el usuario ha añadido desde DetailScreen (tipo "carrito").
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val app = context.applicationContext as AppApplication
    
    val viewModel: PlannerViewModel = viewModel(
        factory = PlannerViewModelFactory(app)
    )
    
    val uiState by viewModel.uiState.collectAsState()
    val isConnected = rememberConnectivityState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Ruta de Hoy", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    if (!uiState.isEmpty) {
                        IconButton(onClick = { viewModel.clearRoute() }) {
                            Icon(Icons.Filled.DeleteSweep, contentDescription = "Limpiar ruta")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            if (uiState.routeAtractivos.size >= 2 && !uiState.isOptimized) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.optimizeRoute() },
                    icon = { Icon(Icons.Filled.Route, contentDescription = null) },
                    text = { Text("Optimizar Orden") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ConnectivityBanner()
            
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.isEmpty -> {
                    EmptyRouteView(
                        onExplore = { navController.navigate("home") }
                    )
                }
                else -> {
                    // Header con métricas
                    RouteHeader(
                        itemCount = uiState.routeAtractivos.size,
                        totalDistance = uiState.totalDistance,
                        estimatedTime = uiState.estimatedTime,
                        isOptimized = uiState.isOptimized
                    )
                    
                    // Lista de lugares en la ruta
                    RouteItemsList(
                        atractivos = if (uiState.isOptimized) uiState.optimizedRoute else uiState.routeAtractivos,
                        onRemove = { viewModel.removeFromRoute(it.id) },
                        onNavigate = { atractivo ->
                            NavigationUtils.openGoogleMapsNavigation(
                                context = context,
                                latitude = atractivo.latitud,
                                longitude = atractivo.longitud,
                                label = atractivo.nombre
                            )
                        },
                        onViewDetail = { atractivo ->
                            navController.navigate("detail/${atractivo.id}")
                        },
                        isConnected = isConnected
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyRouteView(onExplore: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.AddLocationAlt,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Tu ruta está vacía",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Explora los atractivos y toca el botón 'Mi Ruta' para añadirlos aquí",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onExplore) {
            Icon(Icons.Filled.Explore, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Explorar Atractivos")
        }
    }
}

@Composable
private fun RouteHeader(
    itemCount: Int,
    totalDistance: String,
    estimatedTime: String,
    isOptimized: Boolean
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isOptimized) "Ruta Optimizada ✓" else "Mi Ruta",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isOptimized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                if (!isOptimized && itemCount >= 2) {
                    Text(
                        text = "Toca 'Optimizar' para ordenar",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InfoChip(icon = Icons.Filled.Place, text = "$itemCount paradas")
                if (isOptimized && totalDistance.isNotEmpty()) {
                    InfoChip(icon = Icons.AutoMirrored.Filled.DirectionsWalk, text = totalDistance)
                    InfoChip(icon = Icons.Filled.AccessTime, text = estimatedTime)
                }
            }
        }
    }
}

@Composable
private fun InfoChip(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun RouteItemsList(
    atractivos: List<AtractivoTuristico>,
    onRemove: (AtractivoTuristico) -> Unit,
    onNavigate: (AtractivoTuristico) -> Unit,
    onViewDetail: (AtractivoTuristico) -> Unit,
    isConnected: Boolean
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(atractivos, key = { _, item -> item.id }) { index, atractivo ->
            RouteItemCard(
                numero = index + 1,
                atractivo = atractivo,
                isLast = index == atractivos.lastIndex,
                onRemove = { onRemove(atractivo) },
                onNavigate = { onNavigate(atractivo) },
                onViewDetail = { onViewDetail(atractivo) },
                isConnected = isConnected
            )
        }
        
        // Botón para iniciar navegación
        if (atractivos.isNotEmpty()) {
            item {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { atractivos.firstOrNull()?.let { onNavigate(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isConnected
                ) {
                    Icon(Icons.Filled.Navigation, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Iniciar Navegación al Primer Punto")
                }
            }
        }
    }
}

@Composable
private fun RouteItemCard(
    numero: Int,
    atractivo: AtractivoTuristico,
    isLast: Boolean,
    onRemove: () -> Unit,
    onNavigate: () -> Unit,
    onViewDetail: () -> Unit,
    isConnected: Boolean
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // Timeline con número
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
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
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(70.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                )
            }
        }
        
        Spacer(Modifier.width(12.dp))
        
        // Card con info y acciones
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AttractionImage(
                    imageUrl = atractivo.imagenPrincipal,
                    contentDescription = atractivo.nombre,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = atractivo.nombre,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    Text(
                        text = atractivo.categoria,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                // Botón eliminar
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Eliminar",
                        tint = Color.Gray
                    )
                }
                
                // Botón navegar
                IconButton(
                    onClick = onNavigate,
                    enabled = isConnected
                ) {
                    Icon(
                        Icons.Filled.Navigation,
                        contentDescription = "Navegar",
                        tint = if (isConnected) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }
        }
    }
}
