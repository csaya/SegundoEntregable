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
import androidx.compose.material.icons.automirrored.filled.List
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
import com.example.segundoentregable.data.local.entity.RutaEntity
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.ui.components.AttractionImage
import com.example.segundoentregable.ui.components.ConnectivityBanner
import com.example.segundoentregable.ui.components.rememberConnectivityState
import com.example.segundoentregable.utils.NavigationUtils
import com.example.segundoentregable.utils.RouteOptimizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla del Planificador de Rutas Personales.
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

    // Diálogos
    if (uiState.showSaveDialog) {
        SaveRouteDialog(
            onDismiss = { viewModel.hideSaveDialog() },
            onSave = { nombre, descripcion ->
                viewModel.saveCurrentRoute(nombre, descripcion)
            }
        )
    }

    if (uiState.showLoadDialog) {
        LoadRouteDialog(
            savedRoutes = uiState.savedRoutes,
            onDismiss = { viewModel.hideLoadDialog() },
            onLoad = { routeId -> viewModel.loadSavedRoute(routeId) },
            onDelete = { routeId -> viewModel.deleteSavedRoute(routeId) },
            onEdit = { routeId, name, desc -> viewModel.editSavedRoute(routeId, name, desc) }
        )
    }

    // Snackbar host state
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Mostrar snackbar en éxito
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar(
                message = "Ruta guardada correctamente",
                duration = SnackbarDuration.Short
            )
            viewModel.clearSaveSuccess()
        }
    }

    // Mostrar snackbar en error
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Mi Ruta de Hoy", fontWeight = FontWeight.Bold)
                        if (uiState.loadedRouteName != null) {
                            Text(
                                text = uiState.loadedRouteName!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    // Botón cargar rutas guardadas (solo si hay rutas)
                    if (uiState.savedRoutes.isNotEmpty()) {
                        IconButton(onClick = { viewModel.showLoadDialog() }) {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Cargar ruta")
                        }
                    }
                    // Botón guardar ruta actual (mostrar siempre si hay ruta)
                    if (!uiState.isEmpty) {
                        IconButton(
                            onClick = { 
                                if (uiState.isLoggedIn) {
                                    viewModel.showSaveDialog() 
                                } else {
                                    navController.navigate("login")
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Save, contentDescription = "Guardar ruta")
                        }
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
                        isConnected = isConnected,
                        context = context,
                        navController = navController
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
    isConnected: Boolean,
    context: android.content.Context,
    navController: NavController
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

        // ✅ Botón para iniciar navegación CON TODOS LOS PUNTOS
        if (atractivos.isNotEmpty()) {
            item {
                Spacer(Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        // Construir string de IDs separados por coma
                        val routeIds = atractivos.joinToString(",") { it.id }
                        navController.navigate("mapa?routeIds=$routeIds")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Map, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Ver Ruta en Mapa")
                }

                Spacer(Modifier.height(8.dp))

                // Texto informativo
                Text(
                    text = if (atractivos.size > 10) {
                        "Google Maps permite hasta 10 puntos. Se navegará por los primeros 10."
                    } else {
                        "Se navegará por todos los ${atractivos.size} puntos en orden."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        // ✅ Convertir atractivos a Destination
                        val destinations = atractivos.map {
                            NavigationUtils.Destination(
                                latitude = it.latitud,
                                longitude = it.longitud,
                                name = it.nombre
                            )
                        }

                        // ✅ Abrir con todos los waypoints
                        NavigationUtils.openGoogleMapsWithWaypoints(
                            context = context, // ✅ Ahora funciona
                            destinations = destinations,
                            startFromCurrentLocation = true
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isConnected
                ) {
                    Icon(Icons.Filled.Navigation, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Iniciar Navegación Completa (${atractivos.size} puntos)")
                }

                Spacer(Modifier.height(80.dp))
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

@Composable
private fun SaveRouteDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Guardar Ruta", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Guarda tu ruta actual para usarla después",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la ruta *") },
                    placeholder = { Text("Ej: Ruta Centro Histórico") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (opcional)") },
                    placeholder = { Text("Ej: Mi recorrido favorito por el centro") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(nombre, descripcion) },
                enabled = nombre.isNotBlank()
            ) {
                Icon(Icons.Filled.Save, contentDescription = null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun LoadRouteDialog(
    savedRoutes: List<RutaEntity>,
    onDismiss: () -> Unit,
    onLoad: (String) -> Unit,
    onDelete: (String) -> Unit,
    onEdit: (String, String, String) -> Unit
) {
    var routeToDelete by remember { mutableStateOf<RutaEntity?>(null) }
    var routeToEdit by remember { mutableStateOf<RutaEntity?>(null) }

    // Diálogo de confirmación para eliminar
    routeToDelete?.let { route ->
        AlertDialog(
            onDismissRequest = { routeToDelete = null },
            title = { Text("Eliminar ruta") },
            text = { Text("¿Eliminar \"${route.nombre}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(route.id)
                        routeToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { routeToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo para editar
    routeToEdit?.let { route ->
        EditRouteDialog(
            route = route,
            onDismiss = { routeToEdit = null },
            onSave = { newName, newDesc ->
                onEdit(route.id, newName, newDesc)
                routeToEdit = null
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mis Rutas Guardadas", fontWeight = FontWeight.Bold) },
        text = {
            if (savedRoutes.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("No tienes rutas guardadas", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(savedRoutes) { _, route ->
                        SavedRouteItem(
                            route = route,
                            onLoad = { onLoad(route.id) },
                            onEdit = { routeToEdit = route },
                            onDelete = { routeToDelete = route }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
private fun EditRouteDialog(
    route: RutaEntity,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var nombre by remember { mutableStateOf(route.nombre) }
    var descripcion by remember { mutableStateOf(route.descripcion) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Ruta", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (opcional)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(nombre, descripcion) },
                enabled = nombre.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun SavedRouteItem(
    route: RutaEntity,
    onLoad: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onLoad
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = route.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                if (route.descripcion.isNotBlank()) {
                    Text(
                        text = route.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    if (route.distanciaTotal > 0) {
                        Text(
                            text = RouteOptimizer.formatDistance(route.distanciaTotal.toDouble()),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                    Text(
                        text = dateFormat.format(Date(route.createdAt)),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "Editar",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Eliminar",
                    tint = Color.Gray
                )
            }
        }
    }
}
