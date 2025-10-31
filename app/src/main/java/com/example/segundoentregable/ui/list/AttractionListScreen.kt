package com.example.segundoentregable.ui.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.segundoentregable.ui.components.AppBottomBar
import com.example.segundoentregable.ui.components.AttractionListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttractionListScreen(
    navController: NavController,
    viewModel: AttractionListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { ListTopBar(navController = navController) },
        bottomBar = { AppBottomBar(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // 1. Barra de Búsqueda (¡Esta es funcional!)
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Buscar atractivos") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Buscar") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )

            Spacer(Modifier.height(12.dp))

            // 2. Filtros
            FilterChips(
                categorias = uiState.categoriasDisponibles,
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { viewModel.onCategorySelected(it) }
            )

            Spacer(Modifier.height(12.dp))

            // 3. Lista de Resultados
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp) // Espacio al final
            ) {
                items(uiState.listaFiltrada) { atractivo ->
                    AttractionListItem(
                        atractivo = atractivo,
                        onClick = {
                            navController.navigate("detail/${atractivo.id}")
                        }
                    )
                }
            }
        }
    }
}

// --- Componentes Internos ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListTopBar(navController: NavController) {
    TopAppBar(
        title = { Text("Arequipa", fontWeight = FontWeight.Bold) },
        navigationIcon = {
            // El botón de "Atrás" que vimos en el mockup
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChips(
    categorias: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        // (Simulamos los filtros de Distancia y Precio como desactivados)
        item {
            FilterChip(
                selected = false,
                onClick = { /* TODO */ },
                label = { Text("Distancia") },
                enabled = false
            )
        }
        item {
            FilterChip(
                selected = false,
                onClick = { /* TODO */ },
                label = { Text("Precio") },
                enabled = false
            )
        }

        // Filtros de Categoría (Funcionales)
        items(categorias) { categoria ->
            FilterChip(
                selected = (categoria == selectedCategory),
                onClick = { onCategorySelected(categoria) },
                label = { Text(categoria) }
            )
        }
    }
}