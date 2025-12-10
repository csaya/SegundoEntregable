package com.example.segundoentregable.ui.home

import android.Manifest
import android.app.Application
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
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
import coil.compose.AsyncImage
import com.example.segundoentregable.AppApplication
import com.example.segundoentregable.data.local.entity.RutaEntity
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.ui.components.AppBottomBar
import com.example.segundoentregable.ui.components.CercanoItemRow
import com.example.segundoentregable.ui.components.RecomendacionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(application))
    val uiState by homeViewModel.uiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val app = application as AppApplication

    // Solicitar permisos de ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (isGranted) {
            homeViewModel.updateLocation()
            app.proximityService.startMonitoring()
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // ✅ BÚSQUEDA AUTOMÁTICA - Navegar cuando el usuario escribe
    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 3) {
            // Debounce de 500ms
            kotlinx.coroutines.delay(500)
            navController.navigate("list?query=$searchQuery")
        }
    }

    Scaffold(
        topBar = {
            ModernHomeTopBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it }
            )
        },
        bottomBar = { AppBottomBar(navController = navController) }
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Cargando experiencias...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(8.dp))

                // RECOMENDACIONES
                if (uiState.recomendaciones.isNotEmpty()) {
                    RecomendacionesSection(
                        lista = uiState.recomendaciones,
                        onAtractivoClicked = { atractivoId ->
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
                            navController.navigate("detail/$atractivoId?origin=home")
                        }
                    )
                    Spacer(Modifier.height(24.dp))
                }

                // PLANIFICADOR
                PlanificadorSection(
                    onNuevaRuta = { navController.navigate("planner") },
                    onMisRutas = { navController.navigate("mis_rutas") }
                )

                Spacer(Modifier.height(24.dp))

                // RUTAS DESTACADAS
                RutasDestacadasSection(
                    rutas = uiState.rutasDestacadas,
                    onRutaClicked = { rutaId ->
                        navController.navigate("ruta_detalle/$rutaId")
                    },
                    onVerTodas = { navController.navigate("rutas") }
                )

                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

// ✅ TOP BAR MODERNA CON BÚSQUEDA INTEGRADA
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernHomeTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    Column {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(16.dp))

                Text(
                    "Descubre Arequipa",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    "La Ciudad Blanca te espera",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                // Barra de búsqueda moderna
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            "Buscar atractivos, restaurantes...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Limpiar",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                )

                Spacer(Modifier.height(16.dp))
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

@Composable
private fun RecomendacionesSection(
    lista: List<AtractivoTuristico>,
    onAtractivoClicked: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Recomendados para ti",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(lista) { atractivo ->
                RecomendacionCard(
                    atractivo,
                    onClick = { onAtractivoClicked(atractivo.id) }
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
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                "Cerca de ti",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                lista.forEach { atractivo ->
                    CercanoItemRow(
                        atractivo,
                        onClick = { onAtractivoClicked(atractivo.id) }
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
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "🗺️ Rutas Turísticas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onVerTodas) {
                Text("Ver todas")
            }
        }

        Spacer(Modifier.height(12.dp))

        if (rutas.isEmpty()) {
            EmptyRoutesPlaceholder(onVerTodas)
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
private fun EmptyRoutesPlaceholder(onExplore: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "🗺️",
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Descubre rutas curadas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Explora las mejores rutas turísticas de Arequipa",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            FilledTonalButton(onClick = onExplore) {
                Text("Explorar rutas")
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = ruta.imagenPrincipal.ifEmpty {
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0e/Arequipa%2C_Plaza_de_Armas_and_Volc%C3%A1n_Misti_-_panoramio.jpg/1280px-Arequipa%2C_Plaza_de_Armas_and_Volc%C3%A1n_Misti_-_panoramio.jpg"
                },
                contentDescription = ruta.nombre,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            ruta.categoria.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    modifier = Modifier.height(28.dp)
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    ruta.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )

                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "⏱️ ${ruta.duracionEstimada}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "📍 ${ruta.dificultad}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanificadorSection(
    onNuevaRuta: () -> Unit,
    onMisRutas: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            "Planifica tu Recorrido",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onNuevaRuta),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("➕", style = MaterialTheme.typography.displaySmall)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Nueva Ruta",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Crea tu recorrido",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onMisRutas),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📂", style = MaterialTheme.typography.displaySmall)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Mis Rutas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Ver guardadas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
