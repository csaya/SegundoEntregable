package com.example.segundoentregable.ui.login

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.segundoentregable.ui.components.AppBottomBar // <-- IMPORTANTE
import com.example.segundoentregable.ui.components.ImagePlaceholderCircle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val loginViewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(application)
    )

    val uiState by loginViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        loginViewModel.loginSuccessEvent.collect {
            onLoginSuccess()
            val success = navController.popBackStack()
            if (!success) {
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Iniciar Sesión") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        // AGREGAMOS LA BARRA INFERIOR AQUÍ
        bottomBar = {
            AppBottomBar(navController = navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ImagePlaceholderCircle()
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { loginViewModel.onEmailChanged(it) },
                label = { Text("Correo") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.errorMessage != null
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { loginViewModel.onPasswordChanged(it) },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.errorMessage != null
            )

            uiState.errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { loginViewModel.onLoginClicked() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Ingresar")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = { navController.navigate("register") },
                enabled = !uiState.isLoading
            ) {
                Text("¿No tienes cuenta? Regístrate")
            }
        }
    }
}