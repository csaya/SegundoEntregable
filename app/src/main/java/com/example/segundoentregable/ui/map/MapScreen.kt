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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.ui.components.AppBottomBar
import com.example.segundoentregable.ui.components.AttractionMapView
import com.example.segundoentregable.ui.components.AttractionImage
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    showOnlyFavorites: Boolean = false,
    favoriteIds: List<String> = emptyList(),
    focusAttractionId: String? = null
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val mapViewModel: MapViewModel = viewModel(
        factory = MapViewModelFactory(application)
    )

    val uiState by mapViewModel.uiState.collectAsState()
    val arequipaCenter = remember { LatLng(-16.3989, -71.5349) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(arequipaCenter, 11f)
    }

    LaunchedEffect(showOnlyFavorites, favoriteIds) {
        if (showOnlyFavorites && favoriteIds.isNotEmpty()) {
            mapViewModel.setShowOnlyFavorites(true, favoriteIds.toSet())
        }
    }

    LaunchedEffect(focusAttractionId) {
        if (!focusAttractionId.isNullOrBlank()) {
            mapViewModel.focusOnAttraction(focusAttractionId)
        }
    }

    LaunchedEffect(uiState.shouldAnimateCamera, uiState.focusAttraction) {
        if (uiState.shouldAnimateCamera && uiState.focusAttraction != null) {
            val attr = uiState.focusAttraction!!
            if (attr.latitud != 0.0 && attr.longitud != 0.0) {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(attr.latitud, attr.longitud),
                        16f
                    ),
                    durationMs = 1000
                )
            }
            mapViewModel.onCameraAnimationComplete()
        }
    }

    LaunchedEffect(uiState.filteredAtractivos.size, uiState.searchQuery) {
        if (uiState.searchQuery.length > 2 &&
            uiState.filteredAtractivos.size in 1..3) {

            val boundsBuilder = com.google.android.gms.maps.model.LatLngBounds.builder()
            var validPoints = 0
            uiState.filteredAtractivos.forEach {
                if (it.latitud != 0.0) {
                    boundsBuilder.include(LatLng(it.latitud, it.longitud))
                    validPoints++
                }
            }

            if (validPoints > 0) {
                try {
                    val bounds = boundsBuilder.build()
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                } catch (e: Exception) { }
            }
        }
    }

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Hidden,
        skipHiddenState = false
    )
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState
    )

    LaunchedEffect(uiState.selectedAttraction) {
        if (uiState.selectedAttraction != null) {
            sheetState.expand()
        } else {
            sheetState.hide()
        }
    }

    Scaffold(
        bottomBar = { AppBottomBar(navController = navController) }
    ) { paddingValues ->
        val bottomNavHeight = paddingValues.calculateBottomPadding()

        BottomSheetScaffold(
            modifier = Modifier.fillMaxSize(),
            scaffoldState = scaffoldState,
            sheetPeekHeight = 0.dp,
            sheetContainerColor = MaterialTheme.colorScheme.surface,
            sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
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
                        onVerMasClicked = { navController.navigate("detail/${atractivo.id}") },
                        bottomPadding = bottomNavHeight
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        bottom = bottomNavHeight
                    )
            ) {
                AttractionMapView(
                    atractivos = uiState.filteredAtractivos,
                    onMarkerClick = { atractivo -> mapViewModel.selectAttraction(atractivo) },
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                )

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
    onVerMasClicked: () -> Unit,
    bottomPadding: Dp
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = bottomPadding)
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
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                Text(
                    atractivo.ubicacion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 1
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
}
