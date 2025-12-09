package com.example.segundoentregable.ui.favorites

import android.app.Application
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.segundoentregable.AppApplication
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.ui.components.AppBottomBar
import com.example.segundoentregable.ui.components.AttractionImage
import com.example.segundoentregable.ui.components.FavoriteListItem
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    isUserLoggedIn: Boolean
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val app = application as AppApplication

    val viewModel: FavoritesViewModel = viewModel(
        factory = FavoritesViewModelFactory(application)
    )

    val uiState by viewModel.uiState.collectAsState()
    
    // Estado de selección múltiple
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }

    // Solo cargamos datos si está logueado
    LaunchedEffect(isUserLoggedIn) {
        if (isUserLoggedIn) {
            viewModel.loadFavorites()
        }
    }

    Scaffold(
        topBar = { 
            FavoritesTopBar(
                isSelectionMode = isSelectionMode,
                selectedCount = selectedIds.size,
                onCancelSelection = {
                    isSelectionMode = false
                    selectedIds = emptySet()
                }
            )
        },
        bottomBar = { AppBottomBar(navController = navController) },
        floatingActionButton = {
            if (isUserLoggedIn && uiState.favoritesList.isNotEmpty()) {
                if (isSelectionMode && selectedIds.isNotEmpty()) {
                    // Modo selección: Crear ruta con seleccionados
                    ExtendedFloatingActionButton(
                        onClick = {
                            // Crear ruta con los favoritos seleccionados
                            viewModel.createRouteFromFavorites(selectedIds.toList(), app.userRouteRepository)
                            isSelectionMode = false
                            selectedIds = emptySet()
                            navController.navigate("planner")
                        },
                        text = { Text("Crear Ruta (${selectedIds.size})") },
                        icon = { Icon(Icons.Filled.Route, contentDescription = null) },
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                } else if (!isSelectionMode) {
                    // Modo normal: Botón para entrar en modo selección
                    ExtendedFloatingActionButton(
                        onClick = { isSelectionMode = true },
                        text = { Text("Crear Ruta") },
                        icon = { Icon(Icons.Filled.AddLocationAlt, contentDescription = null) }
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->

        if (isUserLoggedIn) {
            if (uiState.favoritesList.isEmpty() && !uiState.isLoading) {
                // Estado vacío
                EmptyFavoritesView(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            } else {
                // Lista de favoritos
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // Header de selección
                    if (isSelectionMode) {
                        item {
                            SelectionHeader(
                                selectedCount = selectedIds.size,
                                totalCount = uiState.favoritesList.size,
                                onSelectAll = {
                                    selectedIds = uiState.favoritesList.map { it.id }.toSet()
                                },
                                onDeselectAll = {
                                    selectedIds = emptySet()
                                }
                            )
                        }
                    }

                    items(uiState.favoritesList, key = { it.id }) { atractivo ->
                        if (isSelectionMode) {
                            SelectableFavoriteItem(
                                atractivo = atractivo,
                                isSelected = selectedIds.contains(atractivo.id),
                                onToggleSelection = {
                                    selectedIds = if (selectedIds.contains(atractivo.id)) {
                                        selectedIds - atractivo.id
                                    } else {
                                        selectedIds + atractivo.id
                                    }
                                }
                            )
                        } else {
                            FavoriteListItem(
                                atractivo = atractivo,
                                onItemClick = {
                                    // ✅ CAMBIO: De "detail/${atractivo.id}" a esto:
                                    navController.navigate("detail/${atractivo.id}?origin=favoritos")
                                },
                                onFavoriteClick = {
                                    viewModel.onToggleFavorite(atractivo.id)
                                }
                            )
                        }
                    }
                }
            }
        } else {
            // --- VISTA DE INVITADO (GUEST) ---
            GuestFavoritesView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                onLogin = { navController.navigate("login") }
            )
        }
    }
}

@Composable
private fun EmptyFavoritesView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.FavoriteBorder,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.Gray
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Aún no tienes favoritos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Explora los atractivos y guarda tus lugares preferidos",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun GuestFavoritesView(
    modifier: Modifier = Modifier,
    onLogin: () -> Unit
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.FavoriteBorder,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.Gray
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Inicia sesión para ver tus favoritos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Guarda los lugares que quieres visitar en tu próxima aventura en Arequipa.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onLogin) {
            Text("Iniciar Sesión")
        }
    }
}

@Composable
private fun SelectionHeader(
    selectedCount: Int,
    totalCount: Int,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$selectedCount de $totalCount seleccionados",
                style = MaterialTheme.typography.bodyMedium
            )
            Row {
                TextButton(onClick = onSelectAll) {
                    Text("Todos")
                }
                TextButton(onClick = onDeselectAll) {
                    Text("Ninguno")
                }
            }
        }
    }
}

@Composable
private fun SelectableFavoriteItem(
    atractivo: AtractivoTuristico,
    isSelected: Boolean,
    onToggleSelection: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onToggleSelection)
            .then(
                if (isSelected) Modifier.border(
                    2.dp,
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(12.dp)
                ) else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        colors = if (isSelected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox visual
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelection() }
            )
            
            Spacer(Modifier.width(8.dp))
            
            // Imagen
            AttractionImage(
                imageUrl = atractivo.imagenPrincipal,
                contentDescription = atractivo.nombre,
                modifier = Modifier
                    .size(56.dp)
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
            
            // Indicador de selección
            if (isSelected) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "Seleccionado",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoritesTopBar(
    isSelectionMode: Boolean,
    selectedCount: Int,
    onCancelSelection: () -> Unit
) {
    TopAppBar(
        title = { 
            Text(
                text = if (isSelectionMode) "Seleccionar ($selectedCount)" else "Favoritos",
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            if (isSelectionMode) {
                IconButton(onClick = onCancelSelection) {
                    Icon(Icons.Filled.Close, contentDescription = "Cancelar selección")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}