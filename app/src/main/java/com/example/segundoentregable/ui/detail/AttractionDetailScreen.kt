package com.example.segundoentregable.ui.detail

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Place
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.segundoentregable.data.model.Actividad
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.data.model.GaleriaFoto
import com.example.segundoentregable.ui.components.AppBottomBar
import com.example.segundoentregable.ui.components.BottomBarScreen
import com.example.segundoentregable.ui.components.OpenStatusBadge
import com.example.segundoentregable.ui.components.RatingBar
import com.example.segundoentregable.ui.components.ReviewCard
import com.example.segundoentregable.ui.components.AttractionImage
import com.example.segundoentregable.ui.components.ExpandableText
import com.example.segundoentregable.ui.components.ZoomableImageViewer
import com.example.segundoentregable.utils.NavigationUtils

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
                onClick = {
                    // CORRECCIÓN: Usar la constante definida en BottomBarScreen
                    navController.navigate(BottomBarScreen.Mapa.route)
                },
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
            // Imagen principal
            item {
                AttractionImage(
                    imageUrl = atractivo.imagenPrincipal,
                    contentDescription = atractivo.nombre
                )
            }

            // Título, rating y descripción
            item {
                Column(Modifier.padding(16.dp)) {
                    // Categoría y tipo
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AssistChip(
                            onClick = { },
                            label = { Text(atractivo.categoria) }
                        )
                        if (atractivo.tipo.isNotEmpty()) {
                            AssistChip(
                                onClick = { },
                                label = { Text(atractivo.tipo) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
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
                    Spacer(Modifier.height(8.dp))
                    // Indicador de Abierto/Cerrado
                    OpenStatusBadge(horario = atractivo.horario)
                    Spacer(Modifier.height(16.dp))
                    // Descripción expandible
                    ExpandableText(
                        text = atractivo.descripcionLarga,
                        style = MaterialTheme.typography.bodyLarge,
                        collapsedMaxLines = 4
                    )
                }
            }

            // Galería de fotos
            if (atractivo.galeria.isNotEmpty()) {
                item {
                    GaleriaSection(galeria = atractivo.galeria)
                }
            }

            // Información detallada
            item {
                InfoSectionEnhanced(atractivo = atractivo)
            }

            // Actividades disponibles
            if (atractivo.actividades.isNotEmpty()) {
                item {
                    ActividadesSection(actividades = atractivo.actividades)
                }
            }

            // Botones de acción
            item {
                ActionButtonsSection(
                    isFavorito = uiState.isFavorito,
                    isInRoute = uiState.isInRoute,
                    onToggleFavorite = {
                        if (isUserLoggedIn) {
                            viewModel.onToggleFavorite()
                        } else {
                            navController.navigate("login")
                        }
                    },
                    onToggleRoute = {
                        viewModel.onToggleRoute()
                    },
                    onGoToPlanner = {
                        navController.navigate("planner")
                    },
                    onGoToMap = {
                        navController.navigate(BottomBarScreen.Mapa.route)
                    },
                    onComoLlegar = {
                        NavigationUtils.openGoogleMapsNavigation(
                            context = context,
                            latitude = atractivo.latitud,
                            longitude = atractivo.longitud,
                            label = atractivo.nombre
                        )
                    }
                )
            }

            // Información adicional (observaciones, estado)
            if (atractivo.observaciones.isNotEmpty() || atractivo.estadoActual.isNotEmpty()) {
                item {
                    InfoAdicionalSection(atractivo = atractivo)
                }
            }

            // Sección de reseñas
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
private fun GaleriaSection(galeria: List<GaleriaFoto>) {
    var showImageViewer by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableStateOf(0) }
    
    // Visor de imágenes a pantalla completa
    if (showImageViewer) {
        ZoomableImageViewer(
            images = galeria.map { it.urlFoto },
            initialPage = selectedImageIndex,
            onDismiss = { showImageViewer = false }
        )
    }
    
    Column(Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Galería de fotos",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                "${galeria.size} fotos",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        Spacer(Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(galeria.size) { index ->
                val foto = galeria[index]
                Card(
                    onClick = {
                        selectedImageIndex = index
                        showImageViewer = true
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    AttractionImage(
                        imageUrl = foto.urlFoto,
                        contentDescription = "Foto ${index + 1}",
                        modifier = Modifier.size(150.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoSectionEnhanced(atractivo: AtractivoTuristico) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("Información", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoCard(
                icon = Icons.Filled.AccessTime,
                title = "Horario",
                content = atractivo.horario.ifEmpty { "Consultar" },
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                icon = Icons.Filled.Payments,
                title = "Precio",
                content = if (atractivo.precio > 0) "S/ ${atractivo.precio}" else "Gratis",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoCard(
                icon = Icons.Filled.Place,
                title = "Ubicación",
                content = atractivo.distrito.ifEmpty { atractivo.ubicacion },
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                icon = Icons.Filled.Landscape,
                title = "Altitud",
                content = "${atractivo.altitud} m.s.n.m.",
                modifier = Modifier.weight(1f)
            )
        }

        if (atractivo.horarioDetallado.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Horario detallado", style = MaterialTheme.typography.labelMedium)
                        Text(atractivo.horarioDetallado, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        if (atractivo.precioDetalle.isNotEmpty() && atractivo.precioDetalle != "Ingreso gratuito") {
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Detalle de precios", style = MaterialTheme.typography.labelMedium)
                        Text(atractivo.precioDetalle, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(icon: ImageVector, title: String, content: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Text(content, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun ActividadesSection(actividades: List<Actividad>) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("Actividades disponibles", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(actividades) { actividad ->
                FilterChip(
                    selected = false,
                    onClick = { },
                    label = { Text(actividad.nombre) },
                    leadingIcon = { Icon(Icons.Filled.LocalActivity, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }
        }
    }
}

@Composable
private fun InfoAdicionalSection(atractivo: AtractivoTuristico) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        if (atractivo.estadoActual.isNotEmpty()) {
            Text("Estado actual", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(atractivo.estadoActual, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(Modifier.height(12.dp))
        }
        if (atractivo.observaciones.isNotEmpty()) {
            Text("Observaciones", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(atractivo.observaciones, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
    }
}

@Composable
private fun ActionButtonsSection(
    isFavorito: Boolean,
    isInRoute: Boolean,
    onToggleFavorite: () -> Unit,
    onToggleRoute: () -> Unit,
    onGoToPlanner: () -> Unit,
    onGoToMap: () -> Unit,
    onComoLlegar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Fila 1: Guardar (favorito) y Añadir a Mi Ruta
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onToggleFavorite,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    if (isFavorito) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = "Guardar",
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    tint = if (isFavorito) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(if (isFavorito) "Guardado" else "Guardar")
            }

            // Botón de Añadir a Mi Ruta
            OutlinedButton(
                onClick = onToggleRoute,
                modifier = Modifier.weight(1f),
                colors = if (isInRoute) {
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                } else {
                    ButtonDefaults.outlinedButtonColors()
                }
            ) {
                Icon(
                    if (isInRoute) Icons.Filled.EditLocationAlt else Icons.Filled.AddLocationAlt,
                    contentDescription = "Mi Ruta",
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    tint = if (isInRoute) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(if (isInRoute) "En Mi Ruta" else "Mi Ruta")
            }
        }

        // Fila 2: Ver en Mapa
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onGoToMap,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Map, contentDescription = null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Ver en Mapa")
            }

            // Si está en la ruta, mostrar botón para ir al planificador
            if (isInRoute) {
                Button(
                    onClick = onGoToPlanner,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Navigation, contentDescription = null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Ver Mi Ruta")
                }
            }
        }
        
        // Fila 3: Botón principal "Cómo Llegar"
        Button(
            onClick = onComoLlegar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Navigation, contentDescription = null, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Cómo Llegar")
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