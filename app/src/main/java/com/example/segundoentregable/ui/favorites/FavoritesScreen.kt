package com.example.segundoentregable.ui.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.segundoentregable.ui.components.AppBottomBar
import com.example.segundoentregable.ui.components.FavoriteListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    viewModel: FavoritesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadFavorites()
    }

    Scaffold(
        topBar = { FavoritesTopBar() },
        bottomBar = { AppBottomBar(navController = navController) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { /* TODO: Navegar al mapa con filtros */ },
                text = { Text("Ver en mapa") },
                icon = { Icon(Icons.Filled.Map, contentDescription = "Ver en mapa") }
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->

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

            if (uiState.favoritesList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(top = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Aún no tienes favoritos",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoritesTopBar() {
    TopAppBar(
        title = {
            Text(
                "Favoritos",
                fontWeight = FontWeight.Bold
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}