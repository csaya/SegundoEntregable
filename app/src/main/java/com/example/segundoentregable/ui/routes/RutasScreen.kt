package com.example.segundoentregable.ui.routes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.segundoentregable.AppApplication
import com.example.segundoentregable.data.local.entity.RutaEntity
import com.example.segundoentregable.ui.components.AppBottomBar
import com.example.segundoentregable.ui.components.AttractionImage
import com.example.segundoentregable.ui.components.ConnectivityBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RutasScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val app = context.applicationContext as AppApplication
    
    val viewModel: RutasViewModel = viewModel(
        factory = RutasViewModelFactory(app)
    )
    
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rutas Turísticas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = { AppBottomBar(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Banner de conectividad
            ConnectivityBanner()
            
            // Card para crear ruta personal
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { navController.navigate("planner") },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Crear Mi Ruta",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Selecciona lugares y optimiza tu recorrido",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null
                    )
                }
            }
            
            // Filtro por categoría
            if (uiState.categorias.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedCategoria == null,
                            onClick = { viewModel.onCategoriaSelected(null) },
                            label = { Text("Todas") }
                        )
                    }
                    items(uiState.categorias) { categoria ->
                        FilterChip(
                            selected = uiState.selectedCategoria == categoria,
                            onClick = { viewModel.onCategoriaSelected(categoria) },
                            label = { Text(categoria.replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }
            
            // Lista de rutas
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.rutas.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay rutas disponibles", color = Color.Gray)
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(uiState.rutas, key = { it.id }) { ruta ->
                            RutaCard(
                                ruta = ruta,
                                onClick = { navController.navigate("ruta_detalle/${ruta.id}") }
                            )
                        }
                    }
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
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Imagen
            AttractionImage(
                imageUrl = ruta.imagenPrincipal,
                contentDescription = ruta.nombre,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop
            )
            
            // Contenido
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Categoría badge
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = ruta.categoria.replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Spacer(Modifier.height(8.dp))
                
                // Nombre
                Text(
                    text = ruta.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.height(4.dp))
                
                // Descripción
                Text(
                    text = ruta.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(Modifier.height(12.dp))
                
                // Info row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Duración
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.Gray
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = ruta.duracionEstimada,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    
                    // Distancia
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Filled.DirectionsWalk,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.Gray
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${ruta.distanciaTotal} km",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    
                    // Dificultad
                    DificultadChip(dificultad = ruta.dificultad)
                }
            }
        }
    }
}

@Composable
private fun DificultadChip(dificultad: String) {
    val (color, text) = when (dificultad.lowercase()) {
        "facil" -> Pair(Color(0xFF4CAF50), "Fácil")
        "moderada" -> Pair(Color(0xFFFF9800), "Moderada")
        "dificil" -> Pair(Color(0xFFF44336), "Difícil")
        else -> Pair(Color.Gray, dificultad)
    }
    
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
