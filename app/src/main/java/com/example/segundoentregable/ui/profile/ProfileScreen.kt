package com.example.segundoentregable.ui.profile

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.segundoentregable.AppApplication
import com.example.segundoentregable.ui.components.AppBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(context.applicationContext as Application)
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = { AppBottomBar(navController = navController) }
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingView(innerPadding)
            !uiState.isLoggedIn -> NotLoggedInView(
                onLoginClick = { navController.navigate("login") },
                onRegisterClick = { navController.navigate("register") },
                modifier = Modifier.padding(innerPadding)
            )
            else -> LoggedInView(
                userName = uiState.userName,
                userEmail = uiState.userEmail,
                onLogout = {
                    viewModel.logout()
                    onLogout()
                },
                onMisRutas = { navController.navigate("mis_rutas") },
                onFavoritos = { navController.navigate("favoritos") },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

// ========== LOADING VIEW ==========

@Composable
private fun LoadingView(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

// ========== NOT LOGGED IN VIEW ==========

@Composable
private fun NotLoggedInView(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        WelcomeHeader()
        Spacer(Modifier.height(32.dp))
        BenefitsCard()
        Spacer(Modifier.height(32.dp))
        AuthButtons(onLoginClick, onRegisterClick)
    }
}

@Composable
private fun WelcomeHeader() {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(60.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }

    Spacer(Modifier.height(32.dp))

    Text(
        "¡Bienvenido a Arequipa!",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Text(
        "Inicia sesión para guardar tus rutas, lugares favoritos y acceder a todas las funciones",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 12.dp)
    )
}

@Composable
private fun BenefitsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Con una cuenta podrás:",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            BenefitItem(Icons.Default.Favorite, "Guardar lugares favoritos")
            BenefitItem(Icons.Default.Route, "Crear y guardar rutas personalizadas")
            BenefitItem(Icons.Default.Star, "Dejar reseñas y calificaciones")
            BenefitItem(Icons.Default.Sync, "Sincronizar entre dispositivos")
        }
    }
}

@Composable
private fun BenefitItem(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun AuthButtons(onLoginClick: () -> Unit, onRegisterClick: () -> Unit) {
    Button(
        onClick = onLoginClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(Icons.Default.Login, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Iniciar Sesión", fontSize = 16.sp)
    }

    Spacer(Modifier.height(12.dp))

    OutlinedButton(
        onClick = onRegisterClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(Icons.Default.PersonAdd, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Crear Cuenta", fontSize = 16.sp)
    }
}

// ========== LOGGED IN VIEW ==========

@Composable
private fun LoggedInView(
    userName: String,
    userEmail: String,
    onLogout: () -> Unit,
    onMisRutas: () -> Unit,
    onFavoritos: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        UserProfileHeader(userName, userEmail)
        Spacer(Modifier.height(32.dp))

        QuickAccessSection(onMisRutas, onFavoritos)
        Spacer(Modifier.height(32.dp))

        AppInfoCard()
        Spacer(Modifier.height(16.dp))

        ProximityNotificationCard()
        Spacer(Modifier.weight(1f))

        LogoutButton(onLogout)
    }
}

@Composable
private fun UserProfileHeader(userName: String, userEmail: String) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = userName.take(1).uppercase(),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }

    Spacer(Modifier.height(16.dp))

    Text(
        userName,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )

    Text(
        userEmail,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun QuickAccessSection(onMisRutas: () -> Unit, onFavoritos: () -> Unit) {
    Text(
        "Accesos Rápidos",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickAccessCard(
            icon = Icons.Default.Route,
            title = "Mis Rutas",
            subtitle = "Ver y editar",
            onClick = onMisRutas,
            modifier = Modifier.weight(1f)
        )
        QuickAccessCard(
            icon = Icons.Default.Favorite,
            title = "Favoritos",
            subtitle = "Lugares guardados",
            onClick = onFavoritos,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickAccessCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AppInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Acerca de la App",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                "Explora Arequipa - Tu guía turística personal",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Versión 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LogoutButton(onLogout: () -> Unit) {
    OutlinedButton(
        onClick = onLogout,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        )
    ) {
        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Cerrar Sesión", fontSize = 16.sp)
    }
}

// ========== PROXIMITY NOTIFICATION CARD ==========

@Composable
private fun ProximityNotificationCard() {
    val context = LocalContext.current
    val app = context.applicationContext as AppApplication
    val prefs = remember { context.getSharedPreferences("proximity_prefs", Context.MODE_PRIVATE) }

    var proximityEnabled by remember {
        mutableStateOf(prefs.getBoolean("monitoring_enabled", false))
    }
    var showPermissionDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }

        if (allGranted) {
            enableProximityService(app, prefs)
            proximityEnabled = true
        } else {
            proximityEnabled = false
            showPermissionDialog = true
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ProximityToggleRow(
                enabled = proximityEnabled,
                onToggle = { enabled ->
                    if (enabled) {
                        handleProximityEnable(context, app, prefs, permissionLauncher) { success ->
                            proximityEnabled = success
                        }
                    } else {
                        disableProximityService(app, prefs)
                        proximityEnabled = false
                    }
                }
            )

            if (proximityEnabled) {
                Spacer(Modifier.height(12.dp))
                ProximityInfoBadge()
            }
        }
    }

    if (showPermissionDialog) {
        PermissionDeniedDialog(onDismiss = { showPermissionDialog = false })
    }
}

@Composable
private fun ProximityToggleRow(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                Icons.Filled.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "Notificaciones de proximidad",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    if (enabled) "Recibirás alertas de atractivos cercanos"
                    else "Activa para recibir alertas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(checked = enabled, onCheckedChange = onToggle)
    }
}

@Composable
private fun ProximityInfoBadge() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Icon(
            Icons.Filled.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "Radio de detección: 500 metros",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PermissionDeniedDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("Permisos requeridos") },
        text = {
            Text(
                "Para recibir notificaciones de atractivos cercanos necesitamos:\n\n" +
                        "• Acceso a tu ubicación\n" +
                        "• Permiso para enviar notificaciones\n\n" +
                        "Puedes activar estos permisos en la configuración de la app."
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Entendido")
            }
        }
    )
}

// ========== HELPER FUNCTIONS ==========

private fun handleProximityEnable(
    context: Context,
    app: AppApplication,
    prefs: android.content.SharedPreferences,
    permissionLauncher: androidx.activity.compose.ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    onResult: (Boolean) -> Unit
) {
    val hasAllPermissions = checkProximityPermissions(context)

    if (hasAllPermissions) {
        enableProximityService(app, prefs)
        onResult(true)
    } else {
        val permissions = buildPermissionList(context)
        permissionLauncher.launch(permissions)
    }
}

private fun checkProximityPermissions(context: Context): Boolean {
    val locationGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val notificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

    return locationGranted && notificationGranted
}

private fun buildPermissionList(context: Context): Array<String> {
    val permissions = mutableListOf<String>()

    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    return permissions.toTypedArray()
}

private fun enableProximityService(app: AppApplication, prefs: android.content.SharedPreferences) {
    app.proximityService.stopMonitoring()
    app.proximityService.clearCooldowns()
    app.proximityService.startMonitoring()
    prefs.edit().putBoolean("monitoring_enabled", true).apply()
}

private fun disableProximityService(app: AppApplication, prefs: android.content.SharedPreferences) {
    app.proximityService.stopMonitoring()
    prefs.edit().putBoolean("monitoring_enabled", false).apply()
}
