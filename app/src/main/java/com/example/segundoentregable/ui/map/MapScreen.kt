package com.example.segundoentregable.ui.map

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
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
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.ui.components.AppBottomBar
import com.example.segundoentregable.ui.components.AttractionMapView
import com.example.segundoentregable.ui.components.AttractionImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    showOnlyFavorites: Boolean = false,
    favoriteIds: List<String> = emptyList()
) {
    // 1. Configuración del Factory
    val context = LocalContext.current
    val application = context.applicationContext as Application

    // 2. Inyección del ViewModel
    val mapViewModel: MapViewModel = viewModel(
        factory = MapViewModelFactory(application)
    )

    val uiState by mapViewModel.uiState.collectAsState()

    // Aplicar filtro de favoritos si viene de FavoritesScreen
    LaunchedEffect(showOnlyFavorites, favoriteIds) {
        if (showOnlyFavorites && favoriteIds.isNotEmpty()) {
            mapViewModel.setShowOnlyFavorites(true, favoriteIds.toSet())
        }
    }

    // Configuración del BottomSheet
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Hidden,
        skipHiddenState = false
    )
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState
    )

    // Efecto para abrir/cerrar el sheet
    LaunchedEffect(uiState.selectedAttraction) {
        if (uiState.selectedAttraction != null) {
            sheetState.expand()
        } else {
            sheetState.hide()
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        topBar = {
            MapTopBar(
                query = uiState.searchQuery,
                onQueryChange = { mapViewModel.onSearchQueryChange(it) },
                showingFavorites = uiState.showOnlyFavorites,
                onClearFilters = { mapViewModel.clearFilters() }
            )
        },
        sheetContent = {
            uiState.selectedAttraction?.let { atractivo ->
                AttractionDetailSheet(
                    atractivo = atractivo,
                    onVerMasClicked = {
                        navController.navigate("detail/${atractivo.id}")
                    }
                )
            }
        },
        sheetPeekHeight = 0.dp,
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)

    ) { topLevelPadding ->

        Scaffold(
            bottomBar = { AppBottomBar(navController = navController) }
        ) { innerContentPadding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = topLevelPadding.calculateTopPadding(),
                        bottom = innerContentPadding.calculateBottomPadding()
                    )
            ) {
                // Mapa con atractivos filtrados
                AttractionMapView(
                    atractivos = uiState.filteredAtractivos,
                    onMarkerClick = { atractivo ->
                        mapViewModel.selectAttraction(atractivo)
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                // Indicador de resultados de búsqueda
                if (uiState.searchQuery.isNotBlank() || uiState.showOnlyFavorites) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${uiState.filteredAtractivos.size} resultados",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    showingFavorites: Boolean,
    onClearFilters: () -> Unit
) {
    Column {
        TopAppBar(
            title = { 
                Text(
                    if (showingFavorites) "Mapa - Favoritos" else "Mapa", 
                    fontWeight = FontWeight.Bold
                ) 
            },
            actions = {
                if (showingFavorites) {
                    TextButton(onClick = onClearFilters) {
                        Text("Ver todos")
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White.copy(alpha = 0.9f))
        )
        
        // Barra de búsqueda
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Buscar atractivos...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Limpiar")
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }
}

@Composable
private fun AttractionDetailSheet(
    atractivo: AtractivoTuristico,
    onVerMasClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = "Rating",
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "${atractivo.rating}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                atractivo.nombre,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                atractivo.ubicacion,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(Modifier.height(8.dp))

            Button(onClick = onVerMasClicked) {
                Text("Ver más")
            }
        }

        Spacer(Modifier.width(16.dp))

        AttractionImage(
            imageUrl = atractivo.imagenPrincipal,
            contentDescription = atractivo.nombre,
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
    }
}