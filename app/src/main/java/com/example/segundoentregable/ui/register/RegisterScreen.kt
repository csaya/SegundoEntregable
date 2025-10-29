package com.example.segundoentregable.ui.register

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
fun RegisterScreen(navController: NavController, userVM: UserViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var inProgress by remember { mutableStateOf(false) }

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
            ImagePlaceholderCircle()
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

            errorMsg?.let {
                Spacer(Modifier.height(8.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                errorMsg = when {
                    name.isBlank() -> "El nombre es obligatorio"
                    email.isBlank() -> "El correo es obligatorio"
                    !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Formato de correo inválido"
                    password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
                    else -> null
                }
                if (errorMsg == null && !inProgress) {
                    inProgress = true
                    userVM.register(name, email, password) { ok ->
                        inProgress = false
                        if (ok) {
                            // show simple success and go back to login
                            navController.popBackStack()
                        } else {
                            errorMsg = "Correo ya registrado"
                        }
                    }
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Registrar")
            }
        }
    }
}
