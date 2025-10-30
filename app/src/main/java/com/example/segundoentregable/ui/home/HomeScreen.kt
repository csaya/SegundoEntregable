package com.example.segundoentregable.ui.home

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.segundoentregable.data.model.AtractivoTuristico

// Importamos nuestros componentes reutilizables
import com.example.segundoentregable.ui.components.AppBottomBar
import com.example.segundoentregable.ui.components.CercanoItemRow
import com.example.segundoentregable.ui.components.RecomendacionCard
import com.example.segundoentregable.ui.components.SearchBar

// --- El Composable Principal de la Pantalla ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = viewModel()
) {
    // Observamos el estado del ViewModel
    val uiState by homeViewModel.uiState.collectAsState()
    // El estado de la búsqueda ahora vive en la pantalla
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = { HomeTopBar() }, // Este es específico de Home, se queda aquí
        bottomBar = { AppBottomBar(navController = navController) } // Componente reutilizable
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            // 1. Barra de Búsqueda (Componente reutilizable)
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearchClicked = {
                    // navController.navigate("search_list")
                },
                placeholder = "Buscar atractivos, restaurantes..."
            )

            Spacer(Modifier.height(24.dp))

            // 2. Sección de Recomendaciones
            RecomendacionesSection(
                lista = uiState.recomendaciones,
                onAtractivoClicked = { atractivoId ->
                    // navController.navigate("detail/$atractivoId")
                }
            )

            Spacer(Modifier.height(24.dp))

            // 3. Sección "Cercanos a ti"
            CercanosSection(
                lista = uiState.cercanos,
                onAtractivoClicked = { atractivoId ->
                    // navController.navigate("detail/$atractivoId")
                }
            )

            Spacer(Modifier.height(16.dp)) // Espacio al final del scroll
        }
    }
}

// --- Componentes Privados (Layout) de HomeScreen ---

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
                // Llama al componente reutilizable
                RecomendacionCard(atractivo, onClick = { onAtractivoClicked(atractivo.id) })
            }
        }
    }
}

@Composable
private fun CercanosSection(
    lista: List<AtractivoTuristico>,
    onAtractivoClicked: (String) -> Unit
) {
    Column {
        Text("Cercanos a ti", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            lista.forEach { atractivo ->
                // Llama al componente reutilizable
                CercanoItemRow(atractivo, onClick = { onAtractivoClicked(atractivo.id) })
            }
        }
    }
}