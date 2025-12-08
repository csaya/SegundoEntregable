package com.example.segundoentregable.ui.detail

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.segundoentregable.ui.components.AppBottomBar
import com.example.segundoentregable.ui.components.RatingBar
import com.example.segundoentregable.ui.components.ReviewCard
import com.example.segundoentregable.ui.components.AttractionImage

@Composable
fun AttractionDetailScreen(
    navController: NavController,
    isUserLoggedIn: Boolean
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val viewModel: AttractionDetailViewModel = viewModel(
        factory = AttractionDetailViewModelFactory(application)
    )

    val uiState by viewModel.uiState.collectAsState()
    val atractivo = uiState.atractivo

    if (uiState.isReviewDialogVisible) {
        AddReviewDialog(
            onDismiss = { viewModel.hideReviewDialog() },
            onSubmit = { rating, comment ->
                viewModel.submitReview(rating, comment)
            }
        )
    }

    Scaffold(
        topBar = { DetailTopBar(navController = navController) },
        bottomBar = { AppBottomBar(navController = navController) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("map") },
                text = { Text("Ver en Mapa") },
                icon = { Icon(Icons.Filled.Map, contentDescription = null) }
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->

        if (atractivo == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                AttractionImage(
                    imageUrl = atractivo.idImagen,
                    contentDescription = atractivo.nombre
                )
            }

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

            item {
                InfoSection(atractivo.horario, "S/ ${atractivo.precio} aprox.")
            }

            item {
                ActionButtonsSection(
                    isFavorito = uiState.isFavorito,
                    onToggleFavorite = {
                        // LÓGICA DE PROTECCIÓN
                        if (isUserLoggedIn) {
                            viewModel.onToggleFavorite()
                        } else {
                            navController.navigate("login")
                        }
                    },
                    onGoToMap = {
                        navController.navigate("map")
                    }
                )
            }

            item {
                Text(
                    "Reseñas",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Reseñas (${uiState.reviews.size})",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    // Botón pequeño para agregar review
                    TextButton(onClick = {
                        if (isUserLoggedIn) {
                            viewModel.showReviewDialog()
                        } else {
                            navController.navigate("login")
                        }
                    }) {
                        Text("Escribir opinión")
                    }
                }
            }

            items(uiState.reviews) { review ->
                ReviewCard(
                    review = review,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailTopBar(navController: NavController) {
    TopAppBar(
        title = { Text("Detalle", fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
private fun InfoSection(horario: String, admision: String) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("Información", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("Horario", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Text(horario.ifEmpty { "09:00 - 18:00" }, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            }
            Column(Modifier.weight(1f)) {
                Text("Precio", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Text(admision, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun ActionButtonsSection(
    isFavorito: Boolean,
    onToggleFavorite: () -> Unit,
    onGoToMap: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedButton(
            onClick = onToggleFavorite,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                if (isFavorito) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                contentDescription = "Guardar",
                modifier = Modifier.size(ButtonDefaults.IconSize),
                tint = if(isFavorito) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(if (isFavorito) "Guardado" else "Guardar")
        }

        Button(
            onClick = onGoToMap,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Filled.Map, contentDescription = null, Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Ver Ubicación")
        }
    }
}

@Composable
fun AddReviewDialog(
    onDismiss: () -> Unit,
    onSubmit: (Float, String) -> Unit
) {
    var rating by remember { mutableFloatStateOf(5f) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Escribe tu reseña") },
        text = {
            Column {
                Text("Calificación:")
                // Slider simple para rating (puedes mejorarlo con estrellas clicables)
                Slider(
                    value = rating,
                    onValueChange = { rating = it },
                    valueRange = 1f..5f,
                    steps = 3
                )
                Text(text = "${rating.toInt()} Estrellas", style = MaterialTheme.typography.bodySmall)

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comentario") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(rating, comment) }) {
                Text("Publicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}