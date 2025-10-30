package com.example.segundoentregable.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.segundoentregable.ui.components.SimpleCard
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = viewModel()
) {
    val uiState by homeViewModel.uiState.collectAsState()

    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text("Inicio") })
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Bienvenido, ${uiState.userName}",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SimpleCard(title = "Mi perfil", subtitle = "", onClick = { navController.navigate("profile") }, modifier = Modifier.weight(1f))
                SimpleCard(title = "Configuración", subtitle = "", onClick = { /* TODO */ }, modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SimpleCard(title = "Mis datos", subtitle = "", onClick = { /* TODO */ }, modifier = Modifier.weight(1f))
                SimpleCard(title = "Salir", subtitle = "", onClick = {
                    homeViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                }, modifier = Modifier.weight(1f))
            }
        }
    }
}