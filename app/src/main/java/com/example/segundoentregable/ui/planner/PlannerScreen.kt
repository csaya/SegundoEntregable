package com.example.segundoentregable.ui.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
                title = { Text("Mi Ruta Personal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    if (uiState.selectedAtractivos.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            if (uiState.selectedAtractivos.size >= 2 && !uiState.showOptimizedRoute) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.optimizeRoute() },
                    icon = { Icon(Icons.Filled.Route, contentDescription = null) },
                    text = { Text("Optimizar Ruta") }
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
            
            // Header con selección
            SelectionHeader(
                selectedCount = uiState.selectedAtractivos.size,
                totalDistance = uiState.totalDistance,
                estimatedTime = uiState.estimatedTime,
                showOptimized = uiState.showOptimizedRoute
            )
            
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.showOptimizedRoute -> {
                    // Mostrar ruta optimizada
                    OptimizedRouteView(
                        atractivos = uiState.optimizedRoute,
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
                else -> {
                    // Selección de atractivos
                    AttractivoSelectionList(
                        atractivos = uiState.allAtractivos,
                        isSelected = { viewModel.isSelected(it) },
                        onToggle = { viewModel.toggleAtractivoSelection(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectionHeader(
    selectedCount: Int,
    totalDistance: String,
    estimatedTime: String,
    showOptimized: Boolean
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (showOptimized) {
                Text(
                    text = "Ruta Optimizada",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InfoChip(icon = Icons.Filled.Place, text = "$selectedCount paradas")
                    InfoChip(icon = Icons.AutoMirrored.Filled.DirectionsWalk, text = totalDistance)
                    InfoChip(icon = Icons.Filled.AccessTime, text = estimatedTime)
                }
            } else {
                Text(
                    text = if (selectedCount == 0) 
                        "Selecciona los lugares que quieres visitar" 
                    else 
                        "$selectedCount lugares seleccionados",
                    style = MaterialTheme.typography.bodyLarge
                )
                if (selectedCount >= 2) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Toca 'Optimizar Ruta' para ordenar tu recorrido",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun AttractivoSelectionList(
    atractivos: List<AtractivoTuristico>,
    isSelected: (String) -> Boolean,
    onToggle: (AtractivoTuristico) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(atractivos, key = { it.id }) { atractivo ->
            SelectableAtractivoCard(
                atractivo = atractivo,
                isSelected = isSelected(atractivo.id),
                onToggle = { onToggle(atractivo) }
            )
        }
    }
}

@Composable
private fun SelectableAtractivoCard(
    atractivo: AtractivoTuristico,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .then(
                if (isSelected) Modifier.border(
                    2.dp,
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(12.dp)
                ) else Modifier
            ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() }
            )
            
            Spacer(Modifier.width(8.dp))
            
            // Imagen
            AttractionImage(
                imageUrl = atractivo.imagenPrincipal,
                contentDescription = atractivo.nombre,
                modifier = Modifier
                    .size(50.dp)
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
        }
    }
}

@Composable
private fun OptimizedRouteView(
    atractivos: List<AtractivoTuristico>,
    onNavigate: (AtractivoTuristico) -> Unit,
    onViewDetail: (AtractivoTuristico) -> Unit,
    isConnected: Boolean
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(atractivos) { index, atractivo ->
            RouteStopCard(
                numero = index + 1,
                atractivo = atractivo,
                isLast = index == atractivos.lastIndex,
                onNavigate = { onNavigate(atractivo) },
                onViewDetail = { onViewDetail(atractivo) },
                isConnected = isConnected
            )
        }
        
        // Botón para iniciar navegación completa
        item {
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = { atractivos.firstOrNull()?.let { onNavigate(it) } },
                modifier = Modifier.fillMaxWidth(),
                enabled = isConnected && atractivos.isNotEmpty()
            ) {
                Icon(Icons.Filled.Navigation, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Iniciar Navegación")
            }
        }
    }
}

@Composable
private fun RouteStopCard(
    numero: Int,
    atractivo: AtractivoTuristico,
    isLast: Boolean,
    onNavigate: () -> Unit,
    onViewDetail: () -> Unit,
    isConnected: Boolean
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // Timeline
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
                        .height(60.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                )
            }
        }
        
        Spacer(Modifier.width(12.dp))
        
        // Card
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
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = atractivo.nombre,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = atractivo.ubicacion,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                IconButton(onClick = onViewDetail) {
                    Icon(Icons.Filled.Info, contentDescription = "Ver detalle")
                }
                
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
