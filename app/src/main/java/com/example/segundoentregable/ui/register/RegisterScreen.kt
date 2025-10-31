package com.example.segundoentregable.ui.register

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.segundoentregable.ui.components.ImagePlaceholderCircle
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    registerViewModel: RegisterViewModel = viewModel()
) {
    val uiState by registerViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        registerViewModel.registerSuccessEvent.collect {
            navController.popBackStack()
        }
    }

    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = { Text("Registro") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
            }
        )
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            ImagePlaceholderCircle()
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.name,
                onValueChange = { registerViewModel.onNameChanged(it) },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.errorMessage != null
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { registerViewModel.onEmailChanged(it) },
                label = { Text("Correo") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.errorMessage != null
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { registerViewModel.onPasswordChanged(it) },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                isError = uiState.errorMessage != null
            )

            uiState.errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    registerViewModel.onRegisterClicked()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading // Deshabilitado mientras carga
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Registrar")
                }
            }
        }
    }
}