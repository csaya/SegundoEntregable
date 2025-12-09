package com.example.segundoentregable.ui.map

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
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

    // Centro de Arequipa
    val arequipaCenter = remember { LatLng(-16.3989, -71.5349) }

    // Estado de cámara controlado
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(arequipaCenter, 11f)
    }

    // ✅ Aplicar filtro de favoritos si viene de FavoritesScreen
    LaunchedEffect(showOnlyFavorites, favoriteIds) {
        if (showOnlyFavorites && favoriteIds.isNotEmpty()) {
            mapViewModel.setShowOnlyFavorites(true, favoriteIds.toSet())
        }
    }

    // ✅ Foco en atractivo específico (desde DetailScreen con focusId)
    LaunchedEffect(focusAttractionId) {
        if (!focusAttractionId.isNullOrBlank()) {
            mapViewModel.focusOnAttraction(focusAttractionId)
        }
    }

    // ✅ Animar cámara cuando hay foco específico (desde Detail)
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

    // ✅ Foco automático en resultados de búsqueda (cuando hay pocos resultados)
    LaunchedEffect(uiState.filteredAtractivos, uiState.searchQuery) {
        // Activar solo si hay búsqueda activa Y pocos resultados
        if (uiState.searchQuery.length >= 3 &&
            uiState.filteredAtractivos.size in 1..5 &&
            uiState.filteredAtractivos.isNotEmpty()) {

            val boundsBuilder = com.google.android.gms.maps.model.LatLngBounds.builder()
            var validPoints = 0

            uiState.filteredAtractivos.forEach {
                if (it.latitud != 0.0 && it.longitud != 0.0) {
                    boundsBuilder.include(LatLng(it.latitud, it.longitud))
                    validPoints++
                }
            }

            if (validPoints > 0) {
                try {
                    val bounds = boundsBuilder.build()
                    // Padding de 150px para que se vean bien todos los marcadores
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngBounds(bounds, 150),
                        durationMs = 800
                    )
                } catch (e: Exception) {
                    // Si bounds es inválido (ej: un solo punto muy cercano), enfocar directo
                    if (validPoints == 1) {
                        val single = uiState.filteredAtractivos.first()
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(single.latitud, single.longitud),
                                15f
                            ),
                            durationMs = 800
                        )
                    }
                }
            }
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

    // ✅ Efecto para abrir/cerrar el sheet
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
                    showingFocusedOnly = !uiState.focusedAttractionId.isNullOrBlank(), // ✅ Nuevo
                    onClearFilters = { mapViewModel.clearFilters() }
                )
            },
            sheetContent = {
                uiState.selectedAttraction?.let { atractivo ->
                    AttractionDetailSheet(
                        atractivo = atractivo,
                        onVerMasClicked = {
                            navController.navigate("detail/${atractivo.id}?origin=mapa")
                        },
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

                // Badge de resultados
                if ((uiState.searchQuery.isNotBlank() || uiState.showOnlyFavorites) &&
                    uiState.focusedAttractionId.isNullOrBlank()) {
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
    onClearFilters: () -> Unit,
    // ✅ Nuevo parámetro
    showingFocusedOnly: Boolean = false
) {
    Column {
        TopAppBar(
            title = {
                Text(
                    when {
                        showingFocusedOnly -> "Mapa - Vista detalle"
                        showingFavorites -> "Mapa - Favoritos"
                        else -> "Mapa"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                // ✅ Botón para limpiar filtros (favoritos o focus)
                if (showingFavorites || showingFocusedOnly) {
                    TextButton(onClick = onClearFilters) {
                        Text("Ver todos")
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White.copy(alpha = 0.9f))
        )

        // Barra de búsqueda (solo si NO estamos en modo focus único)
        if (!showingFocusedOnly) {
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
