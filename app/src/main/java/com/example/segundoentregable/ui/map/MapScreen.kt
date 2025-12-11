package com.example.segundoentregable.ui.map

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.ui.components.AppBottomBar
import com.example.segundoentregable.ui.components.AttractionImage
import com.example.segundoentregable.ui.map.clustering.AttractionClusterItem
import com.example.segundoentregable.ui.map.clustering.AttractionClusterRenderer
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap as NativeGoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay

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

    val navBackStackEntry = navController.currentBackStackEntry
    val routeIdsParam = navBackStackEntry?.arguments?.getString("routeIds")

    val mapViewModel: MapViewModel = viewModel(
        factory = MapViewModelFactory(application)
    )

    val uiState by mapViewModel.uiState.collectAsState()

    // ✅ Estado para clustering
    var clusterManager by remember { mutableStateOf<ClusterManager<AttractionClusterItem>?>(null) }
    var mapInstance by remember { mutableStateOf<NativeGoogleMap?>(null) }

    // Centro de Arequipa
    val arequipaCenter = remember { LatLng(-16.3989, -71.5349) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(arequipaCenter, 11f)
    }

    // Cargar ruta si viene de navegación
    LaunchedEffect(routeIdsParam) {
        if (!routeIdsParam.isNullOrBlank()) {
            val ids = routeIdsParam.split(",").filter { it.isNotBlank() }
            if (ids.isNotEmpty()) {
                mapViewModel.loadRouteView(ids)

                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            mapViewModel.updateUserLocation(it.latitude, it.longitude)
                        }
                    }
                }
            }
        }
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

    LaunchedEffect(uiState.filteredAtractivos.size, uiState.searchQuery) {
        // Solo activar si:
        // 1. Hay búsqueda activa (3+ caracteres)
        // 2. Pocos resultados (1-5)
        // 3. NO está en modo ruta
        // 4. NO está enfocado en un atractivo específico
        if (uiState.searchQuery.length >= 3 &&
            uiState.filteredAtractivos.size in 1..5 &&
            !uiState.routeMode &&
            uiState.focusedAttractionId.isNullOrBlank()) {

            // Pequeño delay para mejor UX (esperar a que termine de escribir)
            delay(300)

            val boundsBuilder = LatLngBounds.builder()
            var validPoints = 0

            uiState.filteredAtractivos.forEach {
                if (it.latitud != 0.0 && it.longitud != 0.0) {
                    boundsBuilder.include(LatLng(it.latitud, it.longitud))
                    validPoints++
                }
            }

            if (validPoints > 0) {
                try {
                    when (validPoints) {
                        1 -> {
                            // Un solo resultado: zoom cercano
                            val single = uiState.filteredAtractivos.first()
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(single.latitud, single.longitud),
                                    15f
                                ),
                                durationMs = 800
                            )
                        }
                        else -> {
                            // Múltiples resultados: mostrar todos con bounds
                            val bounds = boundsBuilder.build()
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngBounds(bounds, 150),
                                durationMs = 800
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Si falla el bounds (puntos muy cercanos), zoom al primero
                    val first = uiState.filteredAtractivos.firstOrNull()
                    if (first != null && first.latitud != 0.0 && first.longitud != 0.0) {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(first.latitud, first.longitud),
                                14f
                            ),
                            durationMs = 800
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(mapInstance, uiState.filteredAtractivos, uiState.routeMode) {
        if (mapInstance != null) {
            if (uiState.routeMode) {
                clusterManager?.clearItems()
                clusterManager?.cluster()
                clusterManager = null
            } else if (uiState.filteredAtractivos.isNotEmpty()) {
                setupClustering(
                    context = context,
                    map = mapInstance!!,
                    attractions = uiState.filteredAtractivos,
                    onClusterManagerCreated = { manager ->
                        clusterManager = manager
                    },
                    onMarkerClick = { attraction ->
                        mapViewModel.selectAttraction(attraction)
                    }
                )
            }
        }
    }


    // Animar cámara
    LaunchedEffect(uiState.shouldAnimateCamera, uiState.routeMode, uiState.focusAttraction) {
        if (uiState.shouldAnimateCamera) {
            when {
                uiState.routeMode && uiState.routeAtractivos.isNotEmpty() -> {
                    val boundsBuilder = LatLngBounds.builder()
                    var validPoints = 0

                    uiState.routeAtractivos.forEach {
                        if (it.latitud != 0.0 && it.longitud != 0.0) {
                            boundsBuilder.include(LatLng(it.latitud, it.longitud))
                            validPoints++
                        }
                    }

                    if (validPoints >= 2) {
                        try {
                            val bounds = boundsBuilder.build()
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngBounds(bounds, 150),
                                durationMs = 1000
                            )
                        } catch (e: Exception) {
                            val first = uiState.routeAtractivos.firstOrNull {
                                it.latitud != 0.0 && it.longitud != 0.0
                            }
                            if (first != null) {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(first.latitud, first.longitud),
                                        13f
                                    ),
                                    durationMs = 1000
                                )
                            }
                        }
                    }
                }
                uiState.focusAttraction != null -> {
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
                }
            }
            mapViewModel.onCameraAnimationComplete()
        }
    }

    // BottomSheet
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
                    showingFocusedOnly = !uiState.focusedAttractionId.isNullOrBlank(),
                    routeMode = uiState.routeMode,
                    routeCount = uiState.routeAtractivos.size,
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
                // ✅ Mostrar loading mientras carga el mapa
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Cargando mapa...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(isMyLocationEnabled = true),
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = false,
                            myLocationButtonEnabled = true
                        ),
                        onMapLoaded = {
                            // Mapa cargado
                        }
                    ) {
                        // ✅ SOLO mostrar marcadores de ruta en modo ruta
                        if (uiState.routeMode && uiState.routeAtractivos.isNotEmpty()) {
                            // Polyline de ruta
                            RoutePolyline(
                                routeAtractivos = uiState.routeAtractivos,
                                userLatitude = uiState.userLatitude,
                                userLongitude = uiState.userLongitude
                            )

                            // Marcadores numerados para ruta (con iconos emoji)
                            uiState.routeAtractivos.forEachIndexed { index, atractivo ->
                                val position = LatLng(atractivo.latitud, atractivo.longitud)
                                val markerNumber = index + 1

                                val markerTitle = when (index) {
                                    0 -> "INICIO: ${atractivo.nombre}"
                                    uiState.routeAtractivos.lastIndex -> "FIN: ${atractivo.nombre}"
                                    else -> "Parada $markerNumber: ${atractivo.nombre}"
                                }

                                val markerState = rememberMarkerState(position = position)

                                Marker(
                                    state = markerState,
                                    title = markerTitle,
                                    snippet = "${atractivo.categoria} · ${atractivo.tipo}",
                                    onClick = {
                                        mapViewModel.selectAttraction(atractivo)
                                        false
                                    },
                                    icon = BitmapDescriptorFactory.defaultMarker(
                                        when (index) {
                                            0 -> BitmapDescriptorFactory.HUE_GREEN  // Inicio
                                            uiState.routeAtractivos.lastIndex -> BitmapDescriptorFactory.HUE_RED  // Fin
                                            else -> when(atractivo.categoria.lowercase()) {
                                                "aventura" -> BitmapDescriptorFactory.HUE_BLUE
                                                "cultural" -> BitmapDescriptorFactory.HUE_ORANGE
                                                "natural" -> BitmapDescriptorFactory.HUE_GREEN
                                                "gastronomía" -> BitmapDescriptorFactory.HUE_YELLOW
                                                else -> BitmapDescriptorFactory.HUE_VIOLET
                                            }
                                        }
                                    )
                                )
                            }
                        }

                        // ✅ Capturar instancia del mapa para clustering (SOLO si NO está en modo ruta)
                        if (!uiState.routeMode) {
                            MapEffect(key1 = uiState.filteredAtractivos.size) { map ->
                                mapInstance = map
                            }
                        }
                    }


                    // Badge de resultados
                    if ((uiState.searchQuery.isNotBlank() || uiState.showOnlyFavorites) &&
                        uiState.focusedAttractionId.isNullOrBlank() &&
                        !uiState.routeMode) {
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
}

// ✅ FUNCIÓN DE CONFIGURACIÓN DE CLUSTERING
private fun setupClustering(
    context: Context,
    map: NativeGoogleMap,
    attractions: List<AtractivoTuristico>,
    onClusterManagerCreated: (ClusterManager<AttractionClusterItem>) -> Unit,
    onMarkerClick: (AtractivoTuristico) -> Unit
) {
    // Crear ClusterManager
    val clusterManager = ClusterManager<AttractionClusterItem>(context, map)

    // Renderer personalizado
    val renderer = AttractionClusterRenderer(context, map, clusterManager)
    clusterManager.renderer = renderer

    // Configurar listeners
    map.setOnCameraIdleListener(clusterManager)
    map.setOnMarkerClickListener(clusterManager)

    // Click en marcador individual
    clusterManager.setOnClusterItemClickListener { item ->
        onMarkerClick(item.attraction)
        true
    }

    // Agregar atractivos al cluster
    clusterManager.clearItems()
    attractions.forEach { atractivo ->
        if (atractivo.latitud != 0.0 && atractivo.longitud != 0.0) {
            clusterManager.addItem(
                AttractionClusterItem(
                    attraction = atractivo,
                    position = LatLng(atractivo.latitud, atractivo.longitud)
                )
            )
        }
    }

    clusterManager.cluster()
    onClusterManagerCreated(clusterManager)
}

// ✅ COMPONENTE POLYLINE SEPARADO
@Composable
private fun RoutePolyline(
    routeAtractivos: List<AtractivoTuristico>,
    userLatitude: Double?,
    userLongitude: Double?
) {
    val routePoints = remember(routeAtractivos, userLatitude, userLongitude) {
        mutableListOf<LatLng>().apply {
            if (userLatitude != null && userLongitude != null) {
                add(LatLng(userLatitude, userLongitude))
            }
            routeAtractivos.forEach {
                add(LatLng(it.latitud, it.longitud))
            }
        }
    }

    if (routePoints.size >= 2) {
        Polyline(
            points = routePoints,
            color = Color(0xFF1976D2),
            width = 8f,
            pattern = listOf(Dash(20f), Gap(15f)),
            geodesic = true,
            jointType = JointType.ROUND,
            startCap = RoundCap(),
            endCap = RoundCap()
        )
    }

    if (userLatitude != null && userLongitude != null) {
        val userMarkerState = rememberMarkerState(
            position = LatLng(userLatitude, userLongitude)
        )

        Marker(
            state = userMarkerState,
            title = "Tu ubicación",
            snippet = "Punto de inicio",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    showingFavorites: Boolean,
    onClearFilters: () -> Unit,
    showingFocusedOnly: Boolean = false,
    routeMode: Boolean = false,
    routeCount: Int = 0
) {
    Column {
        TopAppBar(
            title = {
                Text(
                    when {
                        routeMode -> "Mi Ruta ($routeCount puntos)"
                        showingFocusedOnly -> "Mapa - Vista detalle"
                        showingFavorites -> "Mapa - Favoritos"
                        else -> "Mapa"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                if (showingFavorites || showingFocusedOnly || routeMode) {
                    TextButton(onClick = onClearFilters) {
                        Text("Ver todos")
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            )
        )

        if (!showingFocusedOnly && !routeMode) {
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
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
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
