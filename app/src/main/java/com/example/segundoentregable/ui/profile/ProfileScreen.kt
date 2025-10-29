package com.example.segundoentregable.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.segundoentregable.ui.components.ImagePlaceholderCircle
import com.example.segundoentregable.viewmodel.UserViewModel
import androidx.compose.material3.CenterAlignedTopAppBar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, userVM: UserViewModel) {
    val user = userVM.getCurrentUser()
    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text("Inicio") })

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

            Text(text = user?.name ?: "Invitado", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text(text = user?.email ?: "-", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(24.dp))

            Button(onClick = {
                userVM.logout()
                navController.navigate("login") {
                    popUpTo("login") { inclusive = true }
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Cerrar sesión")
            }
        }
    }
}
