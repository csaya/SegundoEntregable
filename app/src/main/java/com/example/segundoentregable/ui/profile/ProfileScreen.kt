package com.example.segundoentregable.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.segundoentregable.ui.components.AppBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { ProfileTopBar() },
        bottomBar = { AppBottomBar(navController = navController) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.onDownloadGuideClicked() },
                icon = {
                    Icon(
                        Icons.Filled.Download,
                        contentDescription = "Descargar"
                    )
                },
                text = { Text("Descargar guía de Arequipa") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            ProfileHeader(
                name = uiState.userName,
                email = uiState.userEmail
            )

            Spacer(Modifier.height(32.dp))

            SettingsSection(
                downloadCount = uiState.downloadedGuides,
                notificationsEnabled = uiState.notificationsEnabled,
                onNotificationsToggled = { viewModel.onNotificationsToggled(it) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTopBar() {
    TopAppBar(
        title = {
            Text(
                "Perfil",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
private fun ProfileHeader(name: String, email: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Person,
                contentDescription = "Foto de perfil",
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(name, style = MaterialTheme.typography.headlineSmall)
        Text(email, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
    }
}

@Composable
private fun SettingsSection(
    downloadCount: Int,
    notificationsEnabled: Boolean,
    onNotificationsToggled: (Boolean) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text("Configuración", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        SettingsClickableItem(
            title = "Idioma",
            value = "Español",
            onClick = { /* TODO */ }
        )

        Divider()

        SettingsClickableItem(
            title = "Descargas offline",
            subtitle = "Guías descargadas: $downloadCount",
            onClick = { /* TODO */ }
        )

        Divider()

        SettingsSwitchItem(
            title = "Notificaciones",
            checked = notificationsEnabled,
            onCheckedChange = onNotificationsToggled
        )

        Divider()

        SettingsClickableItem(
            title = "Ayuda y privacidad",
            onClick = { /* TODO */ }
        )
    }
}

@Composable
private fun SettingsClickableItem(
    title: String,
    subtitle: String? = null,
    value: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            subtitle?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }
        value?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}