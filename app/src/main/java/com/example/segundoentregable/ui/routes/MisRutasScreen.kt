package com.example.segundoentregable.ui.routes

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.segundoentregable.AppApplication
import com.example.segundoentregable.data.local.entity.RutaEntity
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.data.repository.RutaRepository
import com.example.segundoentregable.data.repository.UserRepository
import com.example.segundoentregable.ui.components.AppBottomBar
import com.example.segundoentregable.utils.RouteOptimizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "MisRutasScreen"

// ========== DATA CLASSES ==========

data class MisRutasUiState(
    val rutas: List<RutaEntity> = emptyList(),
    val rutaSeleccionada: RutaEntity? = null,
    val atractivosDeRuta: List<AtractivoTuristico> = emptyList(),
    val isLoading: Boolean = true,
    val showDeleteDialog: Boolean = false,
    val rutaToDelete: RutaEntity? = null,
    val error: String? = null,
    val successMessage: String? = null,
    val currentUserEmail: String? = null
)

// ========== VIEWMODEL ==========

class MisRutasViewModel(
    private val rutaRepository: RutaRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MisRutasUiState())
    val uiState: StateFlow<MisRutasUiState> = _uiState.asStateFlow()

    init {
        loadUserAndRoutes()
    }

    private fun loadUserAndRoutes() {
        viewModelScope.launch {
            val email = userRepository.getCurrentUserEmail()
            _uiState.update { it.copy(currentUserEmail = email) }
            
            if (email != null) {
                rutaRepository.getUserRoutes(email).collect { rutas ->
                    Log.d(TAG, "Rutas cargadas: ${rutas.size}")
                    _uiState.update { it.copy(rutas = rutas, isLoading = false) }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Inicia sesión para ver tus rutas") }
            }
        }
    }

    fun selectRuta(ruta: RutaEntity) {
        viewModelScope.launch {
            val atractivos = rutaRepository.getAtractivosByRuta(ruta.id)
            Log.d(TAG, "Atractivos de ruta ${ruta.nombre}: ${atractivos.size}")
            _uiState.update { 
                it.copy(
                    rutaSeleccionada = ruta,
                    atractivosDeRuta = atractivos
                ) 
            }
        }
    }

    fun clearSelection() {
        _uiState.update { 
            it.copy(
                rutaSeleccionada = null,
                atractivosDeRuta = emptyList()
            ) 
        }
    }

    fun showDeleteDialog(ruta: RutaEntity) {
        _uiState.update { it.copy(showDeleteDialog = true, rutaToDelete = ruta) }
    }

    fun hideDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false, rutaToDelete = null) }
    }

    fun confirmDelete() {
        val ruta = _uiState.value.rutaToDelete ?: return
        viewModelScope.launch {
            try {
                rutaRepository.deleteUserRoute(ruta.id)
                _uiState.update { 
                    it.copy(
                        showDeleteDialog = false,
                        rutaToDelete = null,
                        rutaSeleccionada = null,
                        atractivosDeRuta = emptyList(),
                        successMessage = "Ruta eliminada"
                    ) 
                }
                Log.d(TAG, "Ruta eliminada: ${ruta.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error eliminando ruta", e)
                _uiState.update { it.copy(error = "Error al eliminar la ruta") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
}

class MisRutasViewModelFactory(
    private val app: AppApplication
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MisRutasViewModel(
            rutaRepository = app.rutaRepository,
            userRepository = app.userRepository
        ) as T
    }
}

// ========== SCREEN ==========

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisRutasScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as AppApplication
    
    val viewModel: MisRutasViewModel = viewModel(
        factory = MisRutasViewModelFactory(app)
    )
    
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar mensajes
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccess()
        }
    }

    // Diálogo de eliminar
    if (uiState.showDeleteDialog && uiState.rutaToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteDialog() },
            title = { Text("Eliminar Ruta") },
            text = { Text("¿Eliminar \"${uiState.rutaToDelete?.nombre}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDelete() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteDialog() }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (uiState.rutaSeleccionada != null) uiState.rutaSeleccionada!!.nombre
                        else "Mis Rutas"
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        when {
                            uiState.rutaSeleccionada != null -> viewModel.clearSelection()
                            else -> navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    if (uiState.rutaSeleccionada != null) {
                        IconButton(onClick = { viewModel.showDeleteDialog(uiState.rutaSeleccionada!!) }) {
                            Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        },
        bottomBar = { AppBottomBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.currentUserEmail == null -> {
                EmptyState(
                    icon = Icons.Default.Person,
                    title = "Inicia Sesión",
                    message = "Debes iniciar sesión para ver tus rutas guardadas",
                    actionText = "Ir a Login",
                    onAction = { navController.navigate("login") },
                    modifier = Modifier.padding(padding)
                )
            }
            uiState.rutaSeleccionada != null -> {
                SimpleRutaDetailView(
                    ruta = uiState.rutaSeleccionada!!,
                    atractivos = uiState.atractivosDeRuta,
                    onVerEnMapa = { ids ->
                        val routeIds = ids.joinToString(",")
                        navController.navigate("mapa?routeIds=$routeIds&origin=mis_rutas")
                    },
                    onIniciarNavegacion = { atractivos ->
                        val destinations = atractivos.map {
                            com.example.segundoentregable.utils.NavigationUtils.Destination(
                                latitude = it.latitud,
                                longitude = it.longitud,
                                name = it.nombre
                            )
                        }
                        com.example.segundoentregable.utils.NavigationUtils.openGoogleMapsWithWaypoints(
                            context = context,
                            destinations = destinations,
                            startFromCurrentLocation = true
                        )
                    },
                    modifier = Modifier.padding(padding)
                )
            }
            uiState.rutas.isEmpty() -> {
                EmptyState(
                    icon = Icons.Default.Map,
                    title = "Sin Rutas",
                    message = "Aún no has guardado ninguna ruta. Crea una desde el planificador.",
                    actionText = "Ir al Planificador",
                    onAction = { navController.navigate("planner") },
                    modifier = Modifier.padding(padding)
                )
            }
            else -> {
                RutasList(
                    rutas = uiState.rutas,
                    onRutaClick = { viewModel.selectRuta(it) },
                    onDeleteClick = { viewModel.showDeleteDialog(it) },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String,
    actionText: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, null, Modifier.size(64.dp), tint = Color.Gray)
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAction) {
            Text(actionText)
        }
    }
}

@Composable
private fun RutasList(
    rutas: List<RutaEntity>,
    onRutaClick: (RutaEntity) -> Unit,
    onDeleteClick: (RutaEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(rutas, key = { it.id }) { ruta ->
            RutaListItem(
                ruta = ruta,
                dateFormat = dateFormat,
                onClick = { onRutaClick(ruta) },
                onDelete = { onDeleteClick(ruta) }
            )
        }
    }
}

@Composable
private fun RutaListItem(
    ruta: RutaEntity,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícono
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Route,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    ruta.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (ruta.descripcion.isNotBlank()) {
                    Text(
                        ruta.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(Modifier.height(4.dp))
                
                // Info row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (ruta.distanciaTotal > 0) {
                        Text(
                            RouteOptimizer.formatDistance(ruta.distanciaTotal.toDouble()),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Text("•", color = Color.Gray)
                    }
                    if (ruta.tiempoEstimadoMinutos > 0) {
                        Text(
                            RouteOptimizer.formatTime(ruta.tiempoEstimadoMinutos),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Text("•", color = Color.Gray)
                    }
                    Text(
                        dateFormat.format(Date(ruta.createdAt)),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
            
            // Delete button
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    "Eliminar",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Vista simplificada del detalle de una ruta guardada
 */
@Composable
private fun SimpleRutaDetailView(
    ruta: RutaEntity,
    atractivos: List<AtractivoTuristico>,
    onVerEnMapa: (List<String>) -> Unit,
    onIniciarNavegacion: (List<AtractivoTuristico>) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Info de ruta
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (ruta.descripcion.isNotBlank()) {
                        Text(
                            ruta.descripcion, 
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Place, 
                            null, 
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "${atractivos.size} paradas",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        if (ruta.distanciaTotal > 0) {
                            Text("•", color = Color.Gray)
                            Text(
                                RouteOptimizer.formatDistance(ruta.distanciaTotal.toDouble()),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        if (ruta.tiempoEstimadoMinutos > 0) {
                            Text("•", color = Color.Gray)
                            Text(
                                RouteOptimizer.formatTime(ruta.tiempoEstimadoMinutos),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // Título
        item {
            Text(
                "Paradas de la Ruta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Lista de atractivos con timeline
        items(atractivos.size) { index ->
            val atractivo = atractivos[index]
            SimpleAtractivoItem(
                index = index,
                atractivo = atractivo,
                isLast = index == atractivos.size - 1
            )
        }

        // Botones de acción
        item {
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Ver en mapa
                OutlinedButton(
                    onClick = { onVerEnMapa(atractivos.map { it.id }) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Map, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Ver Ruta en Mapa")
                }
                
                // Iniciar navegación
                Button(
                    onClick = { onIniciarNavegacion(atractivos) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Navigation, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Iniciar Navegación (${atractivos.size} puntos)")
                }
                
                if (atractivos.size > 10) {
                    Text(
                        "Google Maps permite máx. 10 puntos",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SimpleAtractivoItem(
    index: Int,
    atractivo: AtractivoTuristico,
    isLast: Boolean
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // Timeline
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${index + 1}",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(60.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                )
            }
        }
        
        Spacer(Modifier.width(12.dp))
        
        // Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = atractivo.imagenPrincipal.ifEmpty { "https://via.placeholder.com/60" },
                    contentDescription = null,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        atractivo.nombre,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        atractivo.categoria,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
