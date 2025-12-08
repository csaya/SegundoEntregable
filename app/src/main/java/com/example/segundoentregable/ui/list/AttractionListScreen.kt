package com.example.segundoentregable.ui.list

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    initialQuery: String = ""
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    // USAMOS EL FACTORY
    val viewModel: AttractionListViewModel = viewModel(
        factory = AttractionListViewModelFactory(application)
    )

    val uiState by viewModel.uiState.collectAsState()
    
    // Aplicar query inicial si viene de la búsqueda en Home
    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotEmpty()) {
            viewModel.onSearchQueryChanged(initialQuery)
        }
    }

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

            // Barra de Búsqueda
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

            // Chips de Filtro (Categorías dinámicas)
            FilterChips(
                categorias = uiState.categoriasDisponibles,
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { viewModel.onCategorySelected(it) }
            )

            Spacer(Modifier.height(12.dp))

            // Lista de Resultados
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
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
}

// ... ListTopBar y FilterChips se mantienen IGUAL ...
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListTopBar(navController: NavController) {
    TopAppBar(
        title = { Text("Arequipa", fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
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
        // Chip de "Todos" o Reset opcional
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(selectedCategory ?: "") }, // Truco para resetear
                label = { Text("Todos") }
            )
        }

        items(categorias) { categoria ->
            FilterChip(
                selected = (categoria == selectedCategory),
                onClick = { onCategorySelected(categoria) },
                label = { Text(categoria) }
            )
        }
    }
}