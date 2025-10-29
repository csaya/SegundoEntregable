package com.example.segundoentregable.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.segundoentregable.ui.components.ImagePlaceholderCircle
import com.example.segundoentregable.viewmodel.UserViewModel
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.text.input.PasswordVisualTransformation



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, userVM: UserViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var inProgress by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text("Inicio") })

    }) { innerPadding ->
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
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            errorMsg?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    // basic client-side validation
                    errorMsg = when {
                        email.isBlank() -> "El correo es obligatorio"
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Formato de correo inválido"
                        password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
                        else -> null
                    }
                    if (errorMsg == null && !inProgress) {
                        inProgress = true
                        userVM.login(email, password) { ok ->
                            inProgress = false
                            if (ok) {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                errorMsg = "Credenciales incorrectas"
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ingresar")
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { navController.navigate("register") }) {
                Text("¿No tienes cuenta? Regístrate")
            }
        }
    }
}
