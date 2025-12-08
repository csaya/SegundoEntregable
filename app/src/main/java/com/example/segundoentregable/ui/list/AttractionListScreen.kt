package com.example.segundoentregable.ui.list

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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

    // Estados para dialogs
    var showSortDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { 
            ListTopBar(
                navController = navController,
                onSortClick = { showSortDialog = true },
                onFilterClick = { showFilterDialog = true }
            ) 
        },
        bottomBar = { AppBottomBar(navController = navController) }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                // Barra de Búsqueda
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("Buscar atractivos") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Buscar") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(30.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )

                Spacer(Modifier.height(12.dp))

                // Chips de Filtro (Categorías)
                FilterChips(
                    categorias = uiState.categoriasDisponibles,
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = { viewModel.onCategorySelected(it) }
                )

                // Filtros activos
                ActiveFiltersRow(
                    priceFilter = uiState.priceFilter,
                    minRating = uiState.minRating,
                    sortOption = uiState.sortOption,
                    resultCount = uiState.listaFiltrada.size
                )

                Spacer(Modifier.height(8.dp))

                // Lista de Resultados
                if (uiState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.listaFiltrada.isEmpty()) {
                    EmptyStateView(
                        message = "No se encontraron resultados",
                        suggestion = "Intenta con otros filtros o términos de búsqueda"
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(
                            items = uiState.listaFiltrada,
                            key = { it.id }
                        ) { atractivo ->
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

    // Dialog de Ordenamiento
    if (showSortDialog) {
        SortDialog(
            currentSort = uiState.sortOption,
            onSortSelected = { 
                viewModel.onSortOptionChanged(it)
                showSortDialog = false
            },
            onDismiss = { showSortDialog = false }
        )
    }

    // Dialog de Filtros
    if (showFilterDialog) {
        FilterDialog(
            currentPriceFilter = uiState.priceFilter,
            currentMinRating = uiState.minRating,
            onApply = { price, rating ->
                viewModel.onPriceFilterChanged(price)
                viewModel.onMinRatingChanged(rating)
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }
}

// ... ListTopBar y FilterChips se mantienen IGUAL ...
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListTopBar(
    navController: NavController,
    onSortClick: () -> Unit,
    onFilterClick: () -> Unit
) {
    TopAppBar(
        title = { Text("Explorar", fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
            }
        },
        actions = {
            IconButton(onClick = onSortClick) {
                Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Ordenar")
            }
            IconButton(onClick = onFilterClick) {
                Icon(Icons.Filled.FilterList, contentDescription = "Filtrar")
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
@Composable
private fun ActiveFiltersRow(
    priceFilter: PriceFilter,
    minRating: Float,
    sortOption: SortOption,
    resultCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$resultCount lugares",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (priceFilter != PriceFilter.ALL) {
                AssistChip(
                    onClick = { },
                    label = { 
                        Text(
                            when (priceFilter) {
                                PriceFilter.FREE -> "Gratis"
                                PriceFilter.PAID -> "De pago"
                                else -> ""
                            },
                            style = MaterialTheme.typography.labelSmall
                        ) 
                    }
                )
            }
            if (minRating > 0) {
                AssistChip(
                    onClick = { },
                    label = { Text("★ ${minRating.toInt()}+", style = MaterialTheme.typography.labelSmall) }
                )
            }
        }
    }
}

@Composable
private fun EmptyStateView(message: String, suggestion: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🔍", style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(16.dp))
            Text(text = message, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Text(text = suggestion, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
    }
}

@Composable
private fun SortDialog(currentSort: SortOption, onSortSelected: (SortOption) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ordenar por") },
        text = {
            Column {
                SortOption.entries.forEach { option ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = option == currentSort, onClick = { onSortSelected(option) })
                        Spacer(Modifier.width(8.dp))
                        Text(
                            when (option) {
                                SortOption.DEFAULT -> "Por defecto"
                                SortOption.RATING_DESC -> "Mayor rating"
                                SortOption.RATING_ASC -> "Menor rating"
                                SortOption.NAME_ASC -> "Nombre A-Z"
                                SortOption.NAME_DESC -> "Nombre Z-A"
                                SortOption.PRICE_ASC -> "Mas barato"
                                SortOption.PRICE_DESC -> "Mas caro"
                            }
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDialog(
    currentPriceFilter: PriceFilter,
    currentMinRating: Float,
    onApply: (PriceFilter, Float) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPrice by remember { mutableStateOf(currentPriceFilter) }
    var selectedRating by remember { mutableFloatStateOf(currentMinRating) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtros") },
        text = {
            Column {
                Text("Precio", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PriceFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = filter == selectedPrice,
                            onClick = { selectedPrice = filter },
                            label = {
                                Text(when (filter) {
                                    PriceFilter.ALL -> "Todos"
                                    PriceFilter.FREE -> "Gratis"
                                    PriceFilter.PAID -> "De pago"
                                })
                            }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Rating minimo: ${selectedRating.toInt()} ★", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                Slider(value = selectedRating, onValueChange = { selectedRating = it }, valueRange = 0f..5f, steps = 4)
            }
        },
        confirmButton = { Button(onClick = { onApply(selectedPrice, selectedRating) }) { Text("Aplicar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
