package com.example.segundoentregable.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.segundoentregable.ui.components.ImagePlaceholderCircle
import androidx.compose.material3.CenterAlignedTopAppBar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val uiState by profileViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        profileViewModel.logoutEvent.collect {
            navController.navigate("login") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text("Mi Perfil") })
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(12.dp))
            ImagePlaceholderCircle()
            Spacer(Modifier.height(16.dp))

            // 4. Mostramos los datos desde el uiState
            Text(text = uiState.name, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text(text = uiState.email, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    profileViewModel.onLogoutClicked()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}