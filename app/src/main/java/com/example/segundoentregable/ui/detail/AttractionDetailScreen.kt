package com.example.segundoentregable.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.segundoentregable.ui.components.RatingBar
import com.example.segundoentregable.ui.components.ReviewCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttractionDetailScreen(
    navController: NavController,
    viewModel: AttractionDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val atractivo = uiState.atractivo // El atractivo que estamos viendo

    Scaffold(
        topBar = { DetailTopBar(navController = navController) },
        bottomBar = { AppBottomBar(navController = navController) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { /* TODO */ },
                text = { Text("Añadir a mi ruta") },
                icon = { /* Icono opcional */ }
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->

        // Si el atractivo aún no se carga (o falla), no mostramos nada
        if (atractivo == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        // Usamos LazyColumn para que toda la pantalla sea "scrolleable"
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Imagen Principal
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp) // Imagen grande
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.PhotoCamera, null, Modifier.size(50.dp), Color.Gray)
                }
            }

            // 2. Título, Rating y Descripción
            item {
                Column(Modifier.padding(16.dp)) {
                    Text(atractivo.nombre, style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RatingBar(rating = atractivo.rating)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "(${uiState.reviews.size} reseñas)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(atractivo.descripcionLarga, style = MaterialTheme.typography.bodyLarge)
                }
            }

            // 3. Horario y Entrada
            item {
                InfoSection(atractivo.horario, "S/ ${atractivo.precio} por persona")
            }

            // 4. Botones de Acción
            item {
                ActionButtonsSection(
                    isFavorito = uiState.isFavorito,
                    onToggleFavorite = { viewModel.onToggleFavorite() }
                )
            }

            // 5. Sección de Reseñas
            item {
                Text(
                    "Reseñas",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp)
                )
            }

            items(uiState.reviews) { review ->
                ReviewCard(
                    review = review,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Espacio al final para que el FAB no tape la última reseña
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}


// --- Componentes Internos de la Pantalla ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailTopBar(navController: NavController) {
    TopAppBar(
        title = { Text("Arequipa", fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
private fun InfoSection(horario: String, admision: String) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("Horario y entrada", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("Horario", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Text(horario, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            }
            Column(Modifier.weight(1f)) {
                Text("Admisión", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Text(admision, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun ActionButtonsSection(isFavorito: Boolean, onToggleFavorite: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Botón Guardar (Favorito)
        OutlinedButton(
            onClick = onToggleFavorite,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                if (isFavorito) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                contentDescription = "Guardar",
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(if (isFavorito) "Guardado" else "Guardar")
        }

        // Botón Indicaciones
        Button(
            onClick = { /* TODO */ },
            modifier = Modifier.weight(1f)
        ) {
            Text("Obtener indicaciones")
        }
    }
}