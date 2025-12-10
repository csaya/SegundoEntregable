package com.example.segundoentregable.ui.home

import android.Manifest
import android.app.Application
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.segundoentregable.AppApplication
import com.example.segundoentregable.data.local.entity.RutaEntity
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.ui.components.AppBottomBar
import com.example.segundoentregable.ui.components.CercanoItemRow
import com.example.segundoentregable.ui.components.RecomendacionCard
import com.example.segundoentregable.ui.components.SearchBar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController
) {
    // 1. Obtener contexto y Application para la Factory
    val context = LocalContext.current
    val application = context.applicationContext as Application

    // 2. Inyectar el ViewModel usando la Factory
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(application)
    )

    val uiState by homeViewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // Obtener ProximityService para notificaciones
    val app = application as AppApplication

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (isGranted) {
            // Actualizar ubicación y activar notificaciones de proximidad
            homeViewModel.updateLocation()
            app.proximityService.startMonitoring()
        }
    }

    // Al iniciar la pantalla, pedimos permiso si no lo tenemos
    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    Scaffold(
        topBar = { HomeTopBar() },
        bottomBar = { AppBottomBar(navController = navController) }
    ) { innerPadding ->

        if (uiState.isLoading) {
            // Mostrar loading centrado mientras se cargan los datos
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("Cargando atractivos...", color = Color.Gray)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(16.dp))

                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearchClicked = {
                        // Navegamos a la lista pasando el query para filtrar
                        if (searchQuery.isNotEmpty()) {
                            navController.navigate("list?query=$searchQuery")
                        } else {
                            navController.navigate("list")
                        }
                    },
                    placeholder = "Buscar atractivos, restaurantes..."
                )

                Spacer(Modifier.height(24.dp))

                // RECOMENDACIONES
                if (uiState.recomendaciones.isNotEmpty()) {
                    RecomendacionesSection(
                        lista = uiState.recomendaciones,
                        onAtractivoClicked = { atractivoId ->
                            // ✅ CAMBIO: De "detail/$atractivoId" a esto:
                            navController.navigate("detail/$atractivoId?origin=home")
                        }
                    )
                    Spacer(Modifier.height(24.dp))
                }

                // CERCANOS
                if (uiState.cercanos.isNotEmpty()) {
                    CercanosSection(
                        lista = uiState.cercanos,
                        onAtractivoClicked = { atractivoId ->
                            // ✅ CAMBIO: De "detail/$atractivoId" a esto:
                            navController.navigate("detail/$atractivoId?origin=home")
                        }
                    )
                    Spacer(Modifier.height(24.dp))
                }

                // MI RUTA PERSONAL (Acceso al Planner)
                MiRutaSection(
                    onVerMiRuta = { navController.navigate("planner") }
                )

                Spacer(Modifier.height(16.dp))

                // RUTAS TURÍSTICAS DESTACADAS
                RutasDestacadasSection(
                    rutas = uiState.rutasDestacadas,
                    onRutaClicked = { rutaId -> 
                        navController.navigate("ruta_detalle/$rutaId") 
                    },
                    onVerTodas = { navController.navigate("rutas") }
                )

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar() {
    TopAppBar(
        title = {
            Text(
                "Arequipa",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        navigationIcon = {
            IconButton(onClick = { /* TODO: Abrir Drawer */ }) {
                Icon(Icons.Filled.Menu, contentDescription = "Menú")
            }
        },
        actions = {
            IconButton(onClick = { /* TODO: Nav a Perfil */ }) {
                Icon(Icons.Filled.Person, contentDescription = "Perfil")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
private fun RecomendacionesSection(
    lista: List<AtractivoTuristico>,
    onAtractivoClicked: (String) -> Unit
) {
    Column {
        Text("Recomendaciones", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(lista) { atractivo ->
                RecomendacionCard(
                    atractivo,
                    onClick = {
                        // ✅ CAMBIO AQUÍ: Agregar ?origin=home
                        onAtractivoClicked(atractivo.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun CercanosSection(
    lista: List<AtractivoTuristico>,
    onAtractivoClicked: (String) -> Unit
) {
    if (lista.isNotEmpty()) {
        Column {
            Text("Cercanos a ti", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                lista.forEach { atractivo ->
                    CercanoItemRow(
                        atractivo,
                        onClick = {
                            // ✅ CAMBIO AQUÍ: Agregar ?origin=home
                            onAtractivoClicked(atractivo.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RutasDestacadasSection(
    rutas: List<RutaEntity>,
    onRutaClicked: (String) -> Unit,
    onVerTodas: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Rutas Turísticas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onVerTodas) {
                Text("Ver todas")
            }
        }
        
        Spacer(Modifier.height(12.dp))
        
        if (rutas.isEmpty()) {
            // Mostrar placeholder si no hay rutas
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "🗺️",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Descubre rutas curadas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Explora las mejores rutas turísticas de Arequipa",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onVerTodas) {
                        Text("Explorar rutas")
                    }
                }
            }
        } else {
            // Mostrar carrusel de rutas
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(rutas) { ruta ->
                    RutaCard(
                        ruta = ruta,
                        onClick = { onRutaClicked(ruta.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RutaCard(
    ruta: RutaEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Imagen de la ruta
            AsyncImage(
                model = ruta.imagenPrincipal.ifEmpty { 
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0e/Arequipa%2C_Plaza_de_Armas_and_Volc%C3%A1n_Misti_-_panoramio.jpg/1280px-Arequipa%2C_Plaza_de_Armas_and_Volc%C3%A1n_Misti_-_panoramio.jpg"
                },
                contentDescription = ruta.nombre,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop
            )
            
            Column(modifier = Modifier.padding(12.dp)) {
                // Categoría chip
                AssistChip(
                    onClick = { },
                    label = { 
                        Text(
                            ruta.categoria.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall
                        ) 
                    },
                    modifier = Modifier.height(24.dp)
                )
                
                Spacer(Modifier.height(4.dp))
                
                Text(
                    ruta.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                
                Spacer(Modifier.height(4.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "⏱️ ${ruta.duracionEstimada}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        "📍 ${ruta.dificultad}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun MiRutaSection(
    onVerMiRuta: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "📍 Mi Ruta de Hoy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Planifica y optimiza tu recorrido personal",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Button(onClick = onVerMiRuta) {
                Text("Ver ruta")
            }
        }
    }
}