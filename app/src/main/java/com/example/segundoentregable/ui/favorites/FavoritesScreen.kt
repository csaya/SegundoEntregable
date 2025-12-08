package com.example.segundoentregable.ui.favorites

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.segundoentregable.ui.components.AppBottomBar
import com.example.segundoentregable.ui.components.FavoriteListItem // Asegúrate que este import es correcto según tu proyecto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    isUserLoggedIn: Boolean // <--- Recibimos estado
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val viewModel: FavoritesViewModel = viewModel(
        factory = FavoritesViewModelFactory(application)
    )

    val uiState by viewModel.uiState.collectAsState()

    // Solo cargamos datos si está logueado
    LaunchedEffect(isUserLoggedIn) {
        if (isUserLoggedIn) {
            viewModel.loadFavorites()
        }
    }

    Scaffold(
        topBar = { FavoritesTopBar() },
        bottomBar = { AppBottomBar(navController = navController) },
        // Ocultamos el FAB si no está logueado para limpiar la UI
        floatingActionButton = {
            if (isUserLoggedIn) {
                ExtendedFloatingActionButton(
                    onClick = { /* TODO: Mapa de favoritos */ },
                    text = { Text("Ver en mapa") },
                    icon = { Icon(Icons.Filled.Map, contentDescription = "Ver en mapa") }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->

        if (isUserLoggedIn) {
            // --- VISTA DE USUARIO ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(uiState.favoritesList) { atractivo ->
                    FavoriteListItem(
                        atractivo = atractivo,
                        onItemClick = {
                            navController.navigate("detail/${atractivo.id}")
                        },
                        onFavoriteClick = {
                            viewModel.onToggleFavorite(atractivo.id)
                        }
                    )
                }

                if (uiState.favoritesList.isEmpty() && !uiState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxWidth()
                                .padding(top = 64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Aún no tienes favoritos", color = Color.Gray)
                        }
                    }
                }
            }
        } else {
            // --- VISTA DE INVITADO (GUEST) ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
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
                Button(onClick = { navController.navigate("login") }) {
                    Text("Iniciar Sesión")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoritesTopBar() {
    TopAppBar(
        title = { Text("Favoritos", fontWeight = FontWeight.Bold) },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}